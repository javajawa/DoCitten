package uk.co.harcourtprogramming.docitten;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.logging.Level;
import uk.co.harcourtprogramming.internetrelaycats.ExternalService;
import uk.co.harcourtprogramming.internetrelaycats.InternetRelayCat;
import uk.co.harcourtprogramming.internetrelaycats.RelayCat;

/**
 * <p>Service for processing the motd.dat files on DoC's file systems, and
 * posting new announcements to the irc channel</p>
 */
public class MOTDService extends ExternalService
{
	/**
	 * <p>The MOTD.dat file</p>
	 */
	private final File f;
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
	 * @see MOTDService#processFile()
	 */
	private class Message
	{
		private Integer id = null;
		private Boolean active = null;
		private String title = null;
		private String mshort = null;
		private String mlong = null;
		private Long from = null;
		private Long to = null;
		private String poster_uid = null;
		private String poster_name = null;

		/**
		 *
		 */
		private Message()
		{
			// Nothing to see here. Move along, citizen!
		}

		/**
		 * Returns this MOTD message in the form:
		 * <pre>#666 *Benedict Harcourt* (bh308@doc): Important Announcement
		 * DoCitten can now read the Message of the Day data files</pre>
		 * @return formatted MOTD message
		 */
		@Override
		public String toString()
		{
			return String.format("#%1$d *%2$s* (%3$s@doc): %4$s\n%5$s",
			    id, poster_name, poster_uid, title,
			    mlong.replaceAll("</?(br|BR)( ?/)?>", " ").replaceAll("</?(p|P) ( ?/)?>", "\n")
			);
		}
	}

	/**
	 * <p>Creates an MOTD Service</p>
	 * @param inst the InternetRelayCat instance this service will be used with
	 * @param f the motd.dat file to watch
	 * @param channel the channel (or user) to post new entries to
	 */
	public MOTDService(InternetRelayCat inst, File f, String channel)
	{
		super(inst);

		if (!f.canRead())
		{
			throw new RuntimeException(new FileNotFoundException());
		}

		this.channel = channel;
		this.f = f;
		processFile(true); // Pre-process the file - messages will not be sent,
		// thus old MOTD's won't be reposted to the list on Service restart
	}

	/**
	 * <p>Runs the MOTD Service</p>
	 */
	@Override
	public void run()
	{
		while (true)
		{
			if (f.lastModified() > lastModified)
			{
				processFile(false);
			}
			try
			{
				Thread.sleep(60000);
			}
			catch (InterruptedException ex)
			{
			}
		}
	}

	/**
	 * <p>Process an MOTD.dat file at the supplied path, look for new entries,
	 * and message the appropriate channel or user.</p>
	 */
	@SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
	// Accessing final fields of a private inner class that is not exported
	private void processFile(boolean initial)
	{
		log(Level.INFO, "Processing MOTD file " + f.getPath());
		BufferedReader in = null;
		Message curr = null;
		final LinkedList<Message> stack = new LinkedList<Message>();

		try
		{
			in = new BufferedReader(new FileReader(f));

			while (in.ready())
			{
				String line = in.readLine().trim();

				if (line.charAt(0) == '#') continue;
				if (line.equals("[Message]"))
				{
					if (curr != null) stack.push(curr);
					curr = new Message();
					continue;
				}

				if (curr == null) continue;
				int div = line.indexOf('=');
				if (div == -1) continue;

				String field = line.substring(0,div).toLowerCase();
				String value = line.substring(div+1);

				if (field.equals("id"))
				{
					curr.id = Integer.parseInt(value);
					continue;
				}
				if (initial) continue; // We only care for ids in initial pass
				if (field.equals("active"))
				{
					curr.active = Boolean.valueOf(value);
					continue;
				}
				if (field.equals("title"))
				{
					curr.title = value;
					continue;
				}
				if (field.equals("short"))
				{
					curr.mshort = value;
					continue;
				}
				if (field.equals("long"))
				{
					curr.mlong = value;
					continue;
				}
				if (field.equals("from"))
				{
//					curr.from = new Date(value).getTime();
					continue;
				}
				if (field.equals("to"))
				{
//					curr.to = new Date(value).getTime();
					continue;
				}
				if (field.equals("posterid"))
				{
					curr.poster_uid = value;
					continue;
				}
				if (field.equals("postername"))
				{
					curr.poster_name = value;
					continue;
				}
				if (field.equals("showunix")) continue;

				log(Level.WARNING, "Unknown Field '" + field + "'");
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

		if (curr != null) stack.push(curr);
		lastModified = f.lastModified();

		if (initial)
		{
			if (stack.size() > 0) lastId = stack.getLast().id;
			return;
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
	protected void startup(RelayCat r)
	{
		// Nothing to see here. Move along, citizen!
	}

	@Override
	public void shutdown()
	{
		// Nothing to see here. Move along, citizen!
	}
}

