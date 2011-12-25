package uk.co.harcourtprogramming.netcat;

import java.util.Calendar;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.IrcException;

public class NetCat extends PircBot implements Runnable
{
	public static void main(String [] args) throws IOException
	{
		final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		NetCat inst = new NetCat(args[0], args[1]);
		new Thread(inst).start();

		while ( true )
		{
			String s = in.readLine();
			if ("quit".equalsIgnoreCase(s)) break;
		}
		inst.shutdown();
	}

	private class GoHomeService implements Runnable
	{
		final Thread t;
		final Calendar c = Calendar.getInstance();

		private GoHomeService()
		{
			NetCat.this.log.log(Level.INFO, "Starting the 'GoHomeService'");
			t = new Thread(this);
			t.setDaemon(true);
			t.start();
		}

		public synchronized void run()
		{
			NetCat.this.log.log(Level.INFO, "'GoHomeService' started");
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
								NetCat.this.message(
								  "Ladies and Gentlemen, your attention please: DoC Labs will be closing in 20 minutes");
								t.sleep(60000);
								break;
							case 50:
								NetCat.this.message(
								  "Ladies and Gentlemen, your attention please: DoC Labs will be closing in 10 minutes\n" +
								  "A wry, witty comment should go here!");
								t.sleep(60000);
								break;
							case 55:
								NetCat.this.message(
								  "Ladies and Gentlemen, your attention please: DoC Labs will be closing in 5 minutes\n" +
								  "Please save your work, log off, and try not to get locked in!");
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
	}

	private final static Logger log = Logger.getLogger("NetCat");
	private final String host;
	private final String channel;

	public NetCat(String host, String channel)
	{
		this.setName("DoCitten");
		this.host = host;

		if (channel.charAt(0) == '#')
		{
			this.channel = channel;
		}
		else
		{
			this.channel = '#' + channel;
		}

		this.setVerbose(false);
	}

	public synchronized void run()
	{
		try
		{
			log.log(Level.INFO, "Connecting to '" + host + "'");
			this.connect(host);
			log.log(Level.INFO, "Joining '" + channel + "'");
			this.joinChannel(channel);
			log.log(Level.INFO, "Loading services");
			new GoHomeService();
			this.wait();
		}
		catch (IOException ex)
		{
			log.log(Level.SEVERE, null, ex);
		}
		catch (IrcException ex)
		{
			log.log(Level.SEVERE, null, ex);
		}
		catch (InterruptedException ex)
		{
		}
		this.quitServer();
		this.disconnect();
		this.dispose();
	}

	public void onMessage(String channel, String sender, String login, String hostname, String message)
	{
		final String lcmess = message.toLowerCase();
		if (lcmess.contains("kitten") || lcmess.contains("kitteh") || lcmess.contains("cat"))
		{
			sendKitten();
		}
	}

	public void message(String message)
	{
		for (String s : message.split("\n"))
		{
			this.sendMessage(this.channel, s);
		}
	}

	public synchronized void shutdown()
	{
		this.notifyAll();
	}

	private void sendKitten()
	{
		this.sendMessage(channel, "This is a placeholder for a kitten link");
	}
}

