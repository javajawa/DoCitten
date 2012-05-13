package uk.co.harcourtprogramming.docitten;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Random;
import uk.co.harcourtprogramming.internetrelaycats.MessageService;
import uk.co.harcourtprogramming.internetrelaycats.Message;
import uk.co.harcourtprogramming.internetrelaycats.Service;

public class KittenService extends Service implements MessageService
{
	/**
	 * Words that resolve to the concept of 'kitten'
	 */
	private static final Pattern kitten =
		Pattern.compile(
			"(?:^|\\s)(kitt+(?:y|i?e+[nh]?)s*|cat(?:\\s|$))",
			Pattern.CASE_INSENSITIVE
		);
	/**
	 * Phrases that resolve to the sounds that cats make
	 */
	private static final Pattern mewls =
		Pattern.compile(
			"(?:^|\\s)(m+(?:i+|r+|e+)[oa]*w+l*|ny+a+n)",
			Pattern.CASE_INSENSITIVE
		);
	/**
	 * Phrases that resolve to things that kitten like being done to them
	 */
	private static final Pattern attention =
		Pattern.compile(
			"(?:^|\\s)(scritchl?es|pets|cud+les|paws(?: at)?|hugs|feeds|greets|nuz+les|(?:dangles|gives).+(string|yarn|wool|catnip))",
			Pattern.CASE_INSENSITIVE
		);

	/**
	 * Random number source for randomised responses
	 */
	private final Random r;

	/**
	 * Creates the kitten service
	 */
	public KittenService()
	{
		r = new Random();
	}

	public KittenService(Random r)
	{
		this.r = r;
	}

	@Override
	public void handle(Message m)
	{
		final String mess = m.getMessage();

		if (m.isAction())
		{
			if (mess.toLowerCase().contains(m.getNick().toLowerCase()))
			{
				Matcher attentionMatcher = attention.matcher(mess);
				if (attentionMatcher.find())
				{
					m.act(attend(m.getSender()));
				}
			}
		}

		// Stop DoCitten replying to itself as much
		if (mess.equals("mew =^.^=")) return;

		String reply = "";

		Matcher kittenMatcher = kitten.matcher(mess);
		Matcher mewlsMatcher  = mewls .matcher(mess);

		// Note that mewl() adds a trailing space
		while (kittenMatcher.find()) reply += mewl();
		while (mewlsMatcher.find())  reply += mewl();

		if (reply.length() != 0)
		{
			m.replyToAll(reply + "=^.^=");
		}
	}

	/**e
	 * @return a random(ish) mewling sound with trailing space
	 */
	private String mewl()
	{
		switch (r.nextInt(6))
		{
			case 0: return "nyaann ";
			case 1: return "mraow ";
			default: return "mew ";
		}
	}

	/**
	 * @param user the user attention is given to
	 * @return how the kitten responds to the user, or null if it ignores the user
	 */
	private String attend(String user)
	{
		switch (r.nextInt(9))
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

	@Override
	public void shutdown()
	{
		// Nothing to see here. Move along, citizen!
	}
}

