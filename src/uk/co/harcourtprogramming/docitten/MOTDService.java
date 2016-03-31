package uk.co.harcourtprogramming.docitten;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import uk.co.harcourtprogramming.internetrelaycats.ExternalService;
import uk.co.harcourtprogramming.internetrelaycats.InternetRelayCat;
import uk.co.harcourtprogramming.internetrelaycats.MessageService;
import uk.co.harcourtprogramming.internetrelaycats.RelayCat;
import uk.co.harcourtprogramming.mewler.MessageTokeniser;

/**
 * <p>Service for processing the motd.dat files on DoC's file systems, and
 * posting new announcements to the IRC channel</p>
 */
public class MOTDService extends ExternalService implements MessageService
{

	/**
	 * <p>The MOTD.dat file</p>
	 */
	private final File data_file;
	/**
	 * <p>The MOTD file</p>
	 */
	private final File motd_file;
	/**
	 * <p>The channel (or user) to sent information to</p>
	 */
	private final String channel;
	/**
	 * <p>The id of the last message sent</p>
	 */
	private int lastId = 0;
	/**
	 * <p>The last seen modification timestamp for the file</p>
	 */
	private long lastModified = 0;

	/**
	 * <p>Class that stores the attributes of messages as laid out in the
	 * motd.dat file</p>
	 *
	 * @see MOTDService#processFile(boolean) processFile
	 */
	private class Message
	{

		/**
		 * <p>MOTD item id</p>
		 */
		protected Integer id = null;
		/**
		 * <p>If this MOTD item is active</p>
		 */
		protected Boolean active = null;
		/**
		 * <p>Item title</p>
		 */
		protected String title = null;
		/**
		 * <p>Item (HTML) content</p>
		 */
		protected String content = null;
		/**
		 * <p>User account name</p>
		 */
		protected String poster_uid = null;
		/**
		 * <p>User account 'real' name</p>
		 */
		protected String poster_name = null;

		/**
		 * <p>Creates a new message class</p>
		 */
		private Message()
		{
			// Nothing to see here. Move along, citizen!
		}

		/**
		 * <p>Returns this MOTD message in the form:</p>
		 * <pre>#666 Benedict Harcourt (bh308@doc): Important Announcement
		 * DoCitten can now read the Message of the Day data files</pre>
		 * @return formatted MOTD message
		 */
		@Override
		public String toString()
		{
			return String.format("#%1$d *%2$s* (%3$s@doc): %4$s\n%5$s",
			    id, poster_name, poster_uid, title,
			    content.replaceAll("</?(br|BR)( ?/)?>", " ").replaceAll("</?(p|P) ( ?/)?>", "\n")
			);
		}
	}

	/**
	 * <p>Creates an MOTD Service</p>
	 *
	 * @param inst the InternetRelayCat instance this service will be used with
	 * @param data_file the motd.dat file to watch
	 * @param motd_file the motd file to use in response to commands
	 * @param channel the channel (or user) to post new entries to
	 */
	public MOTDService(InternetRelayCat inst, File data_file, File motd_file, String channel)
	{
		super(inst);

		if (!data_file.canRead())
			throw new RuntimeException(new FileNotFoundException("MOTD data file not found"));
		if (!motd_file.canRead())
			throw new RuntimeException(new FileNotFoundException("MOTD file not found"));

		this.channel = channel;
		this.data_file = data_file;
		this.motd_file = motd_file;
		processFile(true); // Pre-process the file - messages will not be sent,
		// thus old MOTD's won't be reposted to the list on Service restart
	}

	/**
	 * <p>Creates an MOTD Service</p>
	 *
	 * @param inst the InternetRelayCat instance this service will be used with
	 * @param data_file the motd.dat file to watch
	 * @param motd_file the motd file to use in response to commands
	 * @param channel the channel (or user) to post new entries to
	 */
	public MOTDService(InternetRelayCat inst, String data_file, String motd_file, String channel)
	{
		this(inst, new File(data_file), new File(motd_file), channel);
	}

	/**
	 * <p>Runs the MOTD Service</p>
	 */
	@Override
	@SuppressWarnings("SleepWhileInLoop")
	public void run()
	{
		while (true)
		{
			if (data_file.lastModified() > lastModified)
			{
				processFile(false);
			}
			try
			{
				Thread.sleep(300000);
			}
			catch (InterruptedException ex)
			{
			}
		}
	}

	/**
	 * <p>Process an MOTD.dat file at the supplied path, look for new entries,
	 * and message the appropriate channel or user.</p>
	 *
	 * @param initial whether this is an initial (non-outputting) pass.
	 */
	private void processFile(boolean initial)
	{
		log(Level.INFO, "Processing MOTD file " + data_file.getPath());

		BufferedReader in = null;
		Message curr = null;
		boolean content = false;

		final LinkedList<Message> stack = new LinkedList<Message>();

		try
		{
			in = new BufferedReader(new FileReader(data_file));

			while (in.ready())
			{
				String line = in.readLine().trim();

				// Ingore commented lines
				if (line.charAt(0) == '#')
					continue;

				// Start of a new message
				if (line.equals("[Message]"))
				{
					if (curr != null)
						stack.push(curr);

					curr = new Message();
					content = false;
					continue;
				}

				// We have not encountered a [Message] line
				if (curr == null)
					continue;

				// If not a key=value line, ignore
				// unless we're reading extended content
				int div = line.indexOf('=');
				if (div == -1)
				{
					if (content)
						curr.content += line;

					continue;
				}

				// End of any continuing content block
				content = false;

				String field = line.substring(0, div).toLowerCase();
				String value = line.substring(div + 1);

				switch (field)
				{
					case "id":
						curr.id = Integer.parseInt(value);
						continue;

					case "active":
						curr.active = Boolean.valueOf(value);
						continue;

					case "title":
						curr.title = value;
						continue;

					case "long":
						curr.content = value;
						content = true;
						continue;

					case "posterid":
						curr.poster_uid = value;
						continue;

					case "postername":
						curr.poster_name = value;
						continue;

					case "short":
					case "from":
					case "to":
					case "showunix":
						continue;

					default:
						log(Level.WARNING, "Unknown Field '" + field + "'");
				}
			}
		}
		catch (IOException ex)
		{
			log(Level.WARNING, ex);
		}
		catch (Throwable ex)
		{
			log(Level.SEVERE, ex.getMessage());
		}
		finally
		{
			try
			{
				in.close();
			}
			catch (IOException ex)
			{
			}
		}

		if (curr != null)
			stack.push(curr);

		lastModified = data_file.lastModified();

		if (initial)
		{
			if (stack.size() > 0)
				lastId = stack.getLast().id;

			return; // Don't output on initial pass
		}

		for (Message m : stack)
		{
			if (m.id > lastId)
			{
				log(Level.INFO, "Sending MOTD Notice #" + m.id);
				getInstance().message(channel, m.toString());
				lastId = m.id;
			}
		}
	}

	@Override
	public void handle(uk.co.harcourtprogramming.internetrelaycats.Message m)
	{
		MessageTokeniser t = new MessageTokeniser(m.getMessage());
		t.setConsumeWhitespace(true);

		// Check that the bot is actually being addressed in some way
		if (!t.consume(m.getNick() + ':') && m.getChannel() != null)
			return;
		// Check the command was 'motd' with no other parameters
		if (!t.consume("motd") || !t.isEmpty())
			return;

		try
		{
			BufferedReader r = new BufferedReader(new FileReader(motd_file));
			StringBuilder s = new StringBuilder(1000);

			while (r.ready())
				s.append(r.readLine()).append('\n');

			m.reply(s.toString());
		}
		catch (IOException ex)
		{
			log(Level.WARNING, "Error whilst reading MOTD for user", ex);
		}
	}

	@Override
	protected void startup(RelayCat r)
	{
		List<HelpService> helpServices = r.getServicesByClass(HelpService.class);

		if (!helpServices.isEmpty())
		{
			HelpService.HelpInfo help = new HelpService.HelpInfo("MOTD Service",
				"The MOTD service exists to broadcast DoC service announcements "
				+ "to the users in the #doc channel. The MOTD data file is "
				+ "checked for changes every 5 minutes.\n"
				+ "The service also offers the 'motd' command, which will send "
				+ "the current MOTD to the user in full.");
			helpServices.get(0).addHelp("motd", help);
		}
	}

	@Override
	public void shutdown()
	{
		// Nothing to see here. Move along, citizen!
	}
}
