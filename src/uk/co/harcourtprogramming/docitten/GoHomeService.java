package uk.co.harcourtprogramming.docitten;

import java.util.Calendar;
import java.util.logging.Level;
import uk.co.harcourtprogramming.internetrelaycats.ExternalService;
import uk.co.harcourtprogramming.internetrelaycats.InternetRelayCat;
import uk.co.harcourtprogramming.internetrelaycats.RelayCat;

import static java.lang.System.currentTimeMillis;

/**
 * <p>Class that gives warnings for people to leave the laboratories before the
 * doors automatically lock</p>
 */
public class GoHomeService extends ExternalService
{

	/**
	 * <p>The channel to inform</p>
	 */
	private final String channel;
	/**
	 * <p>Calendar instance for checking the time</p>
	 */
	private final Calendar c = Calendar.getInstance();

	/**
	 * <p>Create a go home service</p>
	 *
	 * @param inst the InternetRelayCat instance
	 * @param channel target name/channel
	 */
	public GoHomeService(InternetRelayCat inst, String channel)
	{
		super(inst);
		this.channel = channel;
	}

	/**
	 * <p>Runs the Go Home Service</p>
	 */
	@Override
	@SuppressWarnings({"SleepWhileHoldingLock", "SleepWhileInLoop"})
	public synchronized void run()
	{
		log(Level.INFO, "'GoHomeService' started");
		while (true)
		{
			try
			{
				c.setTimeInMillis(currentTimeMillis());

				if (c.get(Calendar.HOUR_OF_DAY) == 23)
				{
					switch (c.get(Calendar.MINUTE))
					{
						case 30:
							getInstance().message(channel,
							  "Attention denizens of #doc: DoC Labs will be closing in 30 minutes");
							Thread.sleep(60000); // Make sure we can't send this twice
							break;
						case 50:
							getInstance().message(channel,
							  "Attention denizens of #doc: DoC Labs will be closing in 10 minutes\n" +
							  "Please save, commit, and push your work; log off, and try not to get locked in!");
							Thread.sleep(60000); // Make sure we can't send this twice
							break;
						case 55:
							getInstance().message(channel,
							  "Attention denizens of #doc: DoC Labs will be closing in 5 minutes. " +
							  "Go home and sleep and stuff!");
							Thread.sleep(60000); // Make sure we can't send this twice
							break;
					}
				}
				Thread.sleep(30000);
			}
			catch (InterruptedException ex)
			{
				return;
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
		// Nothing to see here. Move along, citizen.
	}
}
