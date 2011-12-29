package uk.co.harcourtprogramming.docitten;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.co.harcourtprogramming.internetrelaycats.RelayCat;

public class Main
{
	public final static String nick = "DoCitten";

	public static void main(String[] args) throws IOException
	{
		if (args.length < 2)
		{
			System.out.println("Arguments : <host> <#channel> [<#channel> <#channel> ...]");
			System.exit(-1);
		}

		final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		final List<String> channels = Arrays.asList(Arrays.copyOfRange(args, 1, args.length));

		RelayCat inst = new RelayCat(nick, args[0], channels);
		new Thread(inst).start();

		try
		{
			inst.addService(new KittenService());
			inst.addService(new LinkService());
			inst.addService(new GoHomeService(args[1]));
			inst.addService(new MOTDService(new File("/etc/motd.dat"), args[1]));
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
}

