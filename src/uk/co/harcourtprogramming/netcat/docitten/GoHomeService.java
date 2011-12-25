package uk.co.harcourtprogramming.netcat.docitten;

import java.util.Calendar;
import java.util.logging.Level;
import uk.co.harcourtprogramming.netcat.Service;

class GoHomeService extends Service implements Runnable
{
	private final Thread t;
	private final Calendar c = Calendar.getInstance();
	private boolean dispose = false;

	private GoHomeService()
	{
		log(Level.INFO, "Starting the 'GoHomeService'");
		t = new Thread(this);
		t.setDaemon(true);
		t.start();
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
//							NetCat.this.message(
//							  "Ladies and Gentlemen, your attention please: DoC Labs will be closing in 20 minutes");
							t.sleep(60000);
							break;
						case 50:
//							NetCat.this.message(
//							  "Ladies and Gentlemen, your attention please: DoC Labs will be closing in 10 minutes\n" +
//							  "A wry, witty comment should go here!");
							t.sleep(60000);
							break;
						case 55:
//							NetCat.this.message(
//							  "Ladies and Gentlemen, your attention please: DoC Labs will be closing in 5 minutes\n" +
//							  "Please save your work, log off, and try not to get locked in!");
							t.sleep(60000);
							break;
					}
				}
				t.sleep(30000);
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


