package uk.co.harcourtprogramming.netcat;

import java.util.logging.Logger;
import java.util.logging.Level;

public abstract class Service
{
	private final static Logger log = Logger.getLogger("NetCat.Service");
	private static int lastId = 0;

	private static synchronized int id()
	{
		return ++lastId;
	}

	private final int id = id();

	public Service()
	{
		// Nothing to see here. Move along, citizen!
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

