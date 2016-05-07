package uk.co.harcourtprogramming.docitten;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import uk.co.harcourtprogramming.internetrelaycats.FilterService;
import uk.co.harcourtprogramming.internetrelaycats.Message;
import uk.co.harcourtprogramming.internetrelaycats.MessageService;
import uk.co.harcourtprogramming.internetrelaycats.OutboundMessage;
import uk.co.harcourtprogramming.internetrelaycats.RelayCat;
import uk.co.harcourtprogramming.internetrelaycats.Service;

/**
 * <p>Service for logging DoCitten's interactions to the file system</p>
 *
 * @author Benedict Harcourt / javajawa
 */
public class LogService extends Service implements MessageService, FilterService
{

	/**
	 * <p>Base directory for logs</p>
	 */
	private final File dir;
	/**
	 * <p>Map of output files of the current logs</p>
	 */
	private final Map<String, FileWriter> logs = new HashMap<>(8);
	/**
	 * <p>
	 */
	private final Calendar c = Calendar.getInstance();

	/**
	 *
	 * @param dir
	 */
	public LogService(File dir)
	{
		super();

		if ((dir.canWrite() && dir.isDirectory()) || !dir.exists())
		{
			if (!dir.exists())
			{
				dir.mkdir();
			}

			this.dir = dir;
		}
		else
		{
			throw new IllegalArgumentException("LogService requires a writeable directory");
		}
	}

	public LogService(String dir)
	{
		this(new File(dir));
	}
	
	@Override
	public synchronized void handle(Message m)
	{
		final String log = (m.getChannel() == null ? m.getSender() : m.getChannel());
		final FileWriter w;

		c.setTimeInMillis(System.currentTimeMillis());
		try
		{
			if (!logs.containsKey(log))
			{
				w = new FileWriter(new File(this.dir, log), true);
				logs.put(log, w);
			}
			else
			{
				w = logs.get(log);
			}

			w.write(String.format("[%1$ta %1$td %1$tR %2$s] %3$s\n", c, m.getSender(), m.getMessage()));
			w.flush();
		}
		catch (IOException ex)
		{
			log(Level.WARNING, ex);
		}
	}

	@Override
	public OutboundMessage filter(OutboundMessage m)
	{
		final String log = m.getTarget();
		final FileWriter w;

		c.setTimeInMillis(System.currentTimeMillis());
		try
		{
			if (!logs.containsKey(log))
			{
				w = new FileWriter(new File(this.dir, log), true);
				logs.put(log, w);
			}
			else
			{
				w = logs.get(log);
			}

			w.write(String.format("[%1$ta %1$td %1$tR %2$s] %3$s\n", c, m.getNick(), m.getMessage()));
			w.flush();
		}
		catch (IOException ex)
		{
			log(Level.WARNING, ex);
		}
		return m;
	}

	@Override
	protected void startup(RelayCat r)
	{
	}

	@Override
	protected synchronized void shutdown()
	{
		for (FileWriter w : logs.values())
		{
			try
			{
				w.flush();
				w.close();
			}
			catch (IOException ex)
			{
				log(Level.WARNING, ex);
			}
		}
	}
}
