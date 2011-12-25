package uk.co.harcourtprogramming.netcat;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public abstract class ExternalService extends Service implements Runnable
{
	private final Thread t = new Thread(this);
	private NetCat inst = null;

	public ExternalService()
	{
		super();
	}

	protected final Thread getThread()
	{
		return t;
	}

	protected synchronized final void message(String target, String message)
	{
		log(Level.INFO, "External to " + target + ": " + message);
		if (inst == null) return;
		for (String line : message.split("\n"))
		{
			inst.sendMessage(target, line);
		}
	}

	final void setInstance(NetCat i)
	{
		inst = i;
	}
}

