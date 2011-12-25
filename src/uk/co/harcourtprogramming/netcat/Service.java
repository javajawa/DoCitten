package uk.co.harcourtprogramming.netcat;

import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.LogRecord;
import java.util.logging.Level;

public abstract class Service
{
	private static int lastId = 0;

	private static synchronized int id()
	{
		return ++lastId;
	}

	private final int id = id();
	private final Logger log = Logger.getLogger("NetCat.Service." + id);

	public Service()
	{
		final Handler h = new ConsoleHandler();
		h.setFormatter(new Formatter()
		{
			public String format(LogRecord l)
			{
				StringBuilder b = new StringBuilder();
				b.append('[');
				b.append(new Date(l.getMillis()).toString());
				b.append(':');
				b.append(l.getLevel().getLocalizedName());
				b.append("] Service ");
				b.append(Service.this.getClass().getSimpleName());
				b.append('@');
				b.append(Service.this.getId());
				b.append(" >> ");
				b.append(formatMessage(l));
				b.append('\n');

				return b.toString();
			}
		});
		log.addHandler(h);
		log.setUseParentHandlers(false);
	}

	public void log(Level lvl, String msg, Exception ex)
	{
		log.log(lvl, msg, ex);
	}

	public void log(Level lvl, String msg)
	{
		log.log(lvl, msg);
	}

	public void log(Level lvl, Exception ex)
	{
		log.log(lvl, null, ex);
	}

	protected final int getId()
	{
		return id;
	}

	public abstract void shutdown();
}

