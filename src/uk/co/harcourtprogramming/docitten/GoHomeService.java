package uk.co.harcourtprogramming.docitten;

import java.util.Calendar;
import java.util.logging.Level;
import uk.co.harcourtprogramming.internetrelaycats.ExternalService;
import uk.co.harcourtprogramming.internetrelaycats.BasicRelayCat;

/**
 * Class that gives warnings for people to leave the laboratories before the
 * doors automatically lock
 */
class GoHomeService extends ExternalService
{
	/**
	 * The channel to inform
	 */
	private final String channel;
	/**
	 * Calendar instance for checking the time
	 */
	private final Calendar c = Calendar.getInstance();

	/**
	 * Create a go home service
	 * @param inst the BasicRelayCat instance
	 * @param channel target name/channel
	 */
	GoHomeService(BasicRelayCat inst, String channel)
	{
		super(inst);
		this.channel = channel;
	}

	/**
	 * Runs the Go Home Service
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
				c.setTimeInMillis(System.currentTimeMillis());

				if (c.get(Calendar.HOUR_OF_DAY) == 22)
				{
					switch (c.get(Calendar.MINUTE))
					{
						case 40:
							getInstance().message(channel,
							  "Ladies and Gentlemen, your attention please: DoC Labs will be closing in 20 minutes");
							Thread.sleep(60000); // Make sure we can't send this twice
							break;
						case 50:
							getInstance().message(channel,
							  "Ladies and Gentlemen, your attention please: DoC Labs will be closing in 10 minutes\n" +
							  "A wry, witty comment should go here!");
							Thread.sleep(60000); // Make sure we can't send this twice
							break;
						case 55:
							getInstance().message(channel,
							  "Ladies and Gentlemen, your attention please: DoC Labs will be closing in 5 minutes\n" +
							  "Please save your work, log off, and try not to get locked in!");
							Thread.sleep(60000); // Make sure we can't send this twice
							break;
					}
				}
				Thread.sleep(30000);
			}
			catch (InterruptedException ex)
			{
			}
		}
	}

	@Override
	public void shutdown()
	{
		// Nothing to see here. Move along, citizen.
	}
}


