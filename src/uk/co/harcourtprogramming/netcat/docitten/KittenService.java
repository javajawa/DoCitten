package uk.co.harcourtprogramming.netcat.docitten;

import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import uk.co.harcourtprogramming.netcat.MessageService;
import uk.co.harcourtprogramming.netcat.NetCat.Message;

public class KittenService extends MessageService
{
	private static final Pattern kitten = Pattern.compile("kitte[nh]", Pattern.CASE_INSENSITIVE);
	private static final Pattern mewls  = Pattern.compile("(^|\\s)(mew|nya+n|mr+a*o+w)", Pattern.CASE_INSENSITIVE);

	public KittenService()
	{
		// Nothing to see here. Move along, citizen!
	}

	public void handle(Message m)
	{
		final String mess = m.getMessage();
		String reply = "";

		Matcher kittenMatcher = kitten.matcher(mess);
		Matcher mewlsMatcher  = mewls .matcher(mess);
		while (kittenMatcher.find()) reply += mewl();
		while (mewlsMatcher.find())  reply += mewl();

		if (reply.length() != 0)
		{
			m.replyToAll(reply += "=^.^=");
		}
	}

	private String mewl()
	{
		return "mew ";
	}

	public void shutdown()
	{
		// Nothing to see here. Move along, citizen!
	}
}

