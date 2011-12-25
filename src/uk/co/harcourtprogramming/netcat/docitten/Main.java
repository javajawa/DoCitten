package uk.co.harcourtprogramming.netcat.docitten;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import uk.co.harcourtprogramming.netcat.NetCat;

public class Main
{
	public static void main(String [] args) throws IOException
	{
		final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		NetCat inst = new NetCat(args[0], args[1]);
		new Thread(inst).start();

		inst.addService(new KittenService());

		while ( true )
		{
			String s = in.readLine();
			if ("quit".equalsIgnoreCase(s)) break;
		}
		inst.shutdown();
	}
}

