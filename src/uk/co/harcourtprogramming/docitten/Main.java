package uk.co.harcourtprogramming.docitten;

import static java.util.Arrays.asList;
import static java.util.Arrays.copyOfRange;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.co.harcourtprogramming.internetrelaycats.InternetRelayCat;
import uk.co.harcourtprogramming.logging.LogFormatter;

/**
 * <p>Utility class for main function for DoCitten</p>
 */
public class Main
{
	/**
	 * <p>The nickname that the bot should use</p>
	 */
	public final static String nick = "DoCitten";

	private final static Logger rootLogger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).getParent();
	/**
	 * <p>Runs DoCitten, waiting for 'quit' as a line of stdin before closing</p>
	 * @param args the command line arguments to the program
	 * @throws IOException if there's an error reading from stdin
	 */
	public static void main(String[] args) throws IOException
	{
		if (args.length < 2)
		{
			System.out.println("Arguments : <host> <#channel> [<#channel> <#channel> ...]");
			System.exit(-1);
		}

		rootLogger.getHandlers()[0].setFormatter(new LogFormatter());
		rootLogger.setLevel(Level.FINE);

		final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		final List<String> channels = asList(copyOfRange(args, 1, args.length));

		InternetRelayCat inst = new InternetRelayCat(nick, args[0], channels);
		new Thread(inst, "IRC-Thread").start();

		try
		{
			inst.addService(new HushService());
			inst.addService(new KittenService());
			inst.addService(new LinkService());
			inst.addService(new GoHomeService(inst, args[1]));
			inst.addService(new HelpingService());
			inst.addService(new ReminderService(inst));
			inst.addService(new MOTDService(inst, new File("/etc/motd.dat"), args[1]));
		}
		catch (Throwable ex)
		{
			Logger.getLogger(nick).log(Level.SEVERE, null, ex);
		}

		while ( true )
		{
			String s = in.readLine();
			if ("quit".equalsIgnoreCase(s)) break;
		}
		inst.shutdown();
	}

	/**
	 * Private constructor for utility class
	 */
	private Main()
	{
		throw new RuntimeException("Utility class, not to be created.");
	}
}

