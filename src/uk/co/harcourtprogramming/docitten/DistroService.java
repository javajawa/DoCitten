package uk.co.harcourtprogramming.docitten;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import org.eclipse.jgit.util.StringUtils;
import uk.co.harcourtprogramming.docitten.utility.GitRepoTracker;
import uk.co.harcourtprogramming.internetrelaycats.ExternalService;
import uk.co.harcourtprogramming.internetrelaycats.InternetRelayCat;
import uk.co.harcourtprogramming.internetrelaycats.Message;
import uk.co.harcourtprogramming.internetrelaycats.MessageService;
import uk.co.harcourtprogramming.internetrelaycats.RelayCat;
import uk.co.harcourtprogramming.mewler.MessageTokeniser;

public class DistroService extends ExternalService implements MessageService
{

	/**
	 * An OS distro root directory, eg "/vol/linux/ubuntu"
	 */
	private final File root;

	/**
	 * Collection of known distro repos at last check
	 */
	private final Collection<File> distros = new ArrayList<>(6);

	/**
	 * Map of repos that we're tracking, indexed by name returned from
	 * getDistroName()
	 */
	private final Map<String, GitRepoTracker> tracking = new ConcurrentHashMap<>(6);

	/**
	 * Channel we're reporting to
	 */
	private String channel;

	public DistroService(InternetRelayCat inst, File root, String channel)
	{
		super(inst);

		this.root = root;
		this.channel = channel;

		try
		{
			this.checkNewDistros();
			this.watchDistro(this.getDefaultDistro());
		}
		catch (Exception e)
		{
			this.log(Level.WARNING, "Could not initialise distro watcher", e);
		}
	}

	public DistroService(InternetRelayCat inst, String root, String channel)
	{
		this(inst, new File(root), channel);
	}

	@Override
	@SuppressWarnings("SleepWhileInLoop")
	public void run()
	{
		while (true)
		{
			checkUpdates();
			try
			{
				Thread.sleep(300000);
			}
			catch (InterruptedException e)
			{
			}
		}
	}

	@Override
	public void handle(Message m)
	{
		MessageTokeniser t = new MessageTokeniser(m.getMessage());
		t.setConsumeWhitespace(true);

		// Check that the bot is actually being addressed in some way
		if ( !t.consume(m.getNick() + ':') && m.getChannel() != null )
		{
			return;
		}
		// Check the command was 'distro'
		if ( !t.consume("distro") )
		{
			return;
		}

		if ( t.consume("list") )
		{
			m.reply(StringUtils.join(this.tracking.keySet(), " "));
		}
		else if ( t.consume("available") )
		{
			m.reply(this.distrosToString(this.distros, " "));
		}
		else if ( t.consume("latest") )
		{
			String distro = t.nextToken();

			try
			{
				GitRepoTracker git = this.tracking.get(distro);
				for (String s : git.fetchStringUpdates("HEAD^"))
				{
					m.replyToAll(s, true);
				}
			}
			catch (Exception e)
			{
				m.reply("Failed");
			}
		}
		else if ( t.consume("start") )
		{
			String distro = t.nextToken();

			try
			{
				Path rootPath = this.root.toPath();
				Path distroP = rootPath.getParent().resolve(distro);
				Path abs = distroP.toRealPath();
				if ( abs.startsWith(rootPath) )
				{
					this.watchDistro(abs.toFile());
					m.reply("OK");
				}
				else
				{
					throw new Exception("Location outside of root requested");
				}
			}
			catch (Exception e)
			{
				m.reply("Failed");
				log(Level.INFO, "Distro failed to add " + distro, e);
			}
		}
		else if ( t.consume("stop") )
		{
			String distro = t.nextToken();
			try
			{
				if ( this.tracking.remove(distro) != null )
				{
					m.reply("OK");
					return;
				}
			}
			catch (NullPointerException e)
			{
			}
			m.reply("Failed");
		}
	}


	/**
	 * Add a distro to the watch list
	 *
	 * @throws IOException If the requested distro is not found or readable etc.
	 */
	private void watchDistro(File distro) throws IOException
	{
		String name = this.getDistroName(distro);
		GitRepoTracker git = new GitRepoTracker(distro, name);
		tracking.put(name, git);
	}

	/**
	 * Fetches semantically 'latest' distro available
	 *
	 * @returns Distro directory
	 */
	private File getDefaultDistro()
	{
		float greatest = 0;
		File defDistro = null;
		for (File distro : this.distros)
		{
			try
			{
				float curr = Float.parseFloat(distro.getName());
				if ( curr > greatest )
				{
					greatest = curr;
					defDistro = distro;
				}
			}
			catch (NumberFormatException e)
			{
			}
		}
		return defDistro;
	}

	private void checkUpdates()
	{
		try
		{
			Collection<File> newDistros = checkNewDistros();
			if ( !newDistros.isEmpty() )
			{
				StringBuilder sb = new StringBuilder("Discovered a new CSG distribution: ");
				sb.append(this.distrosToString(newDistros, ", "));
				this.getInstance().message(this.channel, sb.toString());
			}
		}
		catch (Exception e)
		{
			this.log(Level.WARNING, "Could not check for new distros", e);
		}

		for (GitRepoTracker repo : this.tracking.values())
		{
			try
			{
				for (String update : repo.fetchStringUpdates())
				{
					StringBuilder sb = new StringBuilder(100);

					sb.append("CSG updated ")
						.append(repo.getName())
						.append(": ")
						.append(update);

					this.getInstance().message(this.channel, sb.toString());
				}
			}
			catch (Exception e)
			{
				this.log(Level.WARNING, "Could not check distro gitlog", e);
			}
		}
	}

	private Collection<File> checkNewDistros() throws Exception
	{
		File[] repoArr = this.root.listFiles(new FileFilter()
		{
			@Override
			public boolean accept(File pathname)
			{
				return new File(pathname, ".git").isDirectory();
			}
		});

		if ( repoArr.length == 0 )
		{
			throw new Exception("No distros located in " + this.root);
		}

		List<File> repoList = Arrays.asList(repoArr);
		List<File> newRepos = new LinkedList<>(repoList);

		distros.addAll(repoList);
		newRepos.removeAll(this.distros);

		return newRepos;
	}

	private String getDistroName(File distro)
	{
		return distro.getParentFile().getName() + "/" + distro.getName();
	}

	private Collection<String> distrosToStrings(Collection<File> distros)
	{
		Collection<String> strs = new ArrayList<>(distros.size());
		for (File distro : distros)
		{
			strs.add(this.getDistroName(distro));
		}
		return strs;
	}

	private String distrosToString(Collection<File> distros, String sep)
	{
		Collection<String> strs = this.distrosToStrings(distros);
		return StringUtils.join(strs, sep);
	}

	@Override
	protected void shutdown()
	{
	}

	@Override
	protected void startup(RelayCat r)
	{
		List<HelpService> helpServices = r.getServicesByClass(HelpService.class);

		if ( !helpServices.isEmpty() )
		{
			HelpService.HelpInfo help = new HelpService.HelpInfo("Distro Service",
				"The distro service broadcasts configuration updates to the " +
				"standard DoC Ubuntu distribution. The distro config repo is " +
				"checked for changes every 5 minutes."
			);
			help.addChild("list", new HelpService.HelpInfo("Lists monitored distributions", ""));
			help.addChild("available", new HelpService.HelpInfo("Lists distributions available to monitor", ""));
			help.addChild("latest", new HelpService.HelpInfo("Broadcast latest update for a given distribution", "Usage: distro latest <distro>"));
			help.addChild("start", new HelpService.HelpInfo("Start monitoring a distribution", "Usage: distro start <distro>"));
			help.addChild("stop", new HelpService.HelpInfo("Stop monitoring a distribution", "Usage: distro stop <distro>"));

			helpServices.get(0).addHelp("distro", help);
		}
	}
}
