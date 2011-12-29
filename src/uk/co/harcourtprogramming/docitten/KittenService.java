package uk.co.harcourtprogramming.docitten;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Random;
import uk.co.harcourtprogramming.internetrelaycats.MessageService;
import uk.co.harcourtprogramming.internetrelaycats.RelayCat.Message;

public class KittenService extends MessageService
{
	private static final Pattern kitten =
		Pattern.compile(
			"kitte[nh]",
			Pattern.CASE_INSENSITIVE
		);
	private static final Pattern mewls =
		Pattern.compile(
			"(^|\\s)(m+e+w+l*|ny+a+n|m(i+|r+)a*o+w?)",
			Pattern.CASE_INSENSITIVE
		);
	private static final Pattern attention =
		Pattern.compile(
			"(^|\\s)(scritchl?es|pets|cud+les|hugs|feeds|greets|nuz+les)",
			Pattern.CASE_INSENSITIVE
		);

	private static final Random r = new Random();

	public KittenService()
	{
		// Nothing to see here. Move along, citizen!
	}

	public void handle(Message m)
	{
		final String mess = m.getMessage();

		if (m.isAction())
		{
			if (mess.toLowerCase().contains(m.getMyNick().toLowerCase()))
			{
				Matcher attentionMatcher = attention.matcher(mess);
				if (attentionMatcher.find())
				{
					m.act(attend(m.getSender()));
				}
			}
		}

		String reply = "";

		Matcher kittenMatcher = kitten.matcher(mess);
		Matcher mewlsMatcher  = mewls .matcher(mess);
		while (kittenMatcher.find()) reply += mewl();
		while (mewlsMatcher.find())  reply += mewl();

		if (reply.length() != 0)
		{
			m.replyToAll(reply + "=^.^=");
		}
	}

	private String mewl()
	{
		switch (r.nextInt(6))
		{
			case 0: return "nyaann ";
			case 1: return "mraow ";
			default: return "mew ";
		}
	}

	private String attend(String user)
	{
		switch (r.nextInt(6))
		{
			case 0: return "purrs";
			case 1: return "purrrs";
			case 2: return "purrrrs";
			case 3: return "purrrrrs";
			case 4: return "paws at " + user;
			case 5: return "rubs againsts " + user + "'s legs";
			case 6: return null;
			default: return "purrs";
		}
	}

	public void shutdown()
	{
		// Nothing to see here. Move along, citizen!
	}
}

