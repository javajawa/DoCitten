package uk.co.harcourtprogramming.netcat.docitten;

import java.util.logging.Level;
import uk.co.harcourtprogramming.netcat.MessageService;
import uk.co.harcourtprogramming.netcat.NetCat.Message;

public class KittenService extends MessageService
{
	public KittenService()
	{
		// Nothing to see here. Move along, citizen!
	}

	public void handle(Message m)
	{
		String mess = m.getMessage().toLowerCase();
		if (
			mess.contains("kitteh") || 
			mess.contains("kitten") ||
			mess.contains("cat") ||
			mess.contains(" mew") ||
			mess.contains("mew ") ||
			mess.equals("mew") ||
			mess.contains("docitten")
		)
		{
			m.replyToAll("mew =^.^=");
		}
	}

	public void shutdown()
	{
		// Nothing to see here. Move along, citizen!
	}
}

