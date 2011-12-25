package uk.co.harcourtprogramming.netcat.docitten;

import java.util.Calendar;
import java.util.logging.Level;
import uk.co.harcourtprogramming.netcat.ExternalService;

class GoHomeService extends ExternalService
{
	private final String channel;
	private final Calendar c = Calendar.getInstance();
	private boolean dispose = false;

	public GoHomeService(String channel)
	{
		super();
		this.channel = channel;
		getThread().setDaemon(true);
	}

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
							message(channel,
							  "Ladies and Gentlemen, your attention please: DoC Labs will be closing in 20 minutes");
							getThread().sleep(60000);
							break;
						case 50:
							message(channel,
							  "Ladies and Gentlemen, your attention please: DoC Labs will be closing in 10 minutes\n" +
							  "A wry, witty comment should go here!");
							getThread().sleep(60000);
							break;
						case 55:
							message(channel,
							  "Ladies and Gentlemen, your attention please: DoC Labs will be closing in 5 minutes\n" +
							  "Please save your work, log off, and try not to get locked in!");
							getThread().sleep(60000);
							break;
					}
				}
				getThread().sleep(30000);
			}
			catch (InterruptedException ex)
			{
			}
		}
	}

	public void shutdown()
	{
		dispose = true;
	}
}


