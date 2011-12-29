package uk.co.harcourtprogramming.docitten;

import java.util.Calendar;
import java.util.logging.Level;
import uk.co.harcourtprogramming.internetrelaycats.ExternalService;
import uk.co.harcourtprogramming.internetrelaycats.BasicRelayCat;

class GoHomeService extends ExternalService
{
	private final String channel;
	private final Calendar c = Calendar.getInstance();
	private boolean dispose = false;

	GoHomeService(BasicRelayCat inst, String channel)
	{
		super(inst);
		this.channel = channel;
	}

	@Override
	@SuppressWarnings({"SleepWhileHoldingLock", "SleepWhileInLoop"})
	public synchronized void run()
	{
		log(Level.INFO, "'GoHomeService' started");
		while (!dispose)
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
							Thread.sleep(60000);
							break;
						case 50:
							getInstance().message(channel,
							  "Ladies and Gentlemen, your attention please: DoC Labs will be closing in 10 minutes\n" +
							  "A wry, witty comment should go here!");
							Thread.sleep(60000);
							break;
						case 55:
							getInstance().message(channel,
							  "Ladies and Gentlemen, your attention please: DoC Labs will be closing in 5 minutes\n" +
							  "Please save your work, log off, and try not to get locked in!");
							Thread.sleep(60000);
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
		dispose = true;
	}
}


