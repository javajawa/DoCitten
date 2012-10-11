package uk.co.harcourtprogramming.docitten;

import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import uk.co.harcourtprogramming.internetrelaycats.Message;
import uk.co.harcourtprogramming.internetrelaycats.MessageService;
import uk.co.harcourtprogramming.internetrelaycats.RelayCat;
import uk.co.harcourtprogramming.internetrelaycats.Service;
import uk.co.harcourtprogramming.mewler.MessageTokeniser;

/**
 * <p>Service that emulates some kitten-like behaviour in order to keep channels
 * relaxed and happy.</p>
 *
 * @author Benedict Harcourt / javajawa
 */
public class KittenService extends Service implements MessageService
{

	/**
	 * <p>Words that resolve to the concept of 'kitten'</p>
	 */
	private static final Pattern kitten =
		Pattern.compile(
			"(?:^|\\s)(kitt+(?:y|i?e+[nh]?)s*|cat(?:\\s|$))",
			Pattern.CASE_INSENSITIVE
		);
	/**
	 * <p>Phrases that resolve to the sounds that cats make</p>
	 */
	private static final Pattern mewls =
		Pattern.compile(
			"(?:^|\\s)(m+(?:i+|r+|e+)[oa]*w+l*|ny+a+n)",
			Pattern.CASE_INSENSITIVE
		);
	/**
	 * <p>Phrases that resolve to things that kitten like being done to them</p>
	 */
	private static final Pattern attention =
		Pattern.compile(
			"(?:^|\\s)(s[ck]ritchl?es|strokes|pets|cud+les|paws(?: at)?|hugs|feeds|greets|nuz+les|(?:dangles|gives).+(?:string|yarn|wool|catnip))",
			Pattern.CASE_INSENSITIVE
		);

	/**
	 * <p>Entropy source for randomised responses</p>
	 */
	private final Random r;

	/**
	 * <p>Creates an instance the kitten service</p>
	 */
	public KittenService()
	{
		r = new Random();
	}

	/**
	 * <p>Creates an instance of the kitten service using a specified entropy
	 * source.</p>
	 *
	 * @param r Entropy source for this kitten
	 */
	public KittenService(Random r)
	{
		this.r = r;
	}

	@Override
	public void handle(Message m)
	{
		final String mess = m.getMessage();

		if (m.getChannel() == null || m.getMessage().startsWith(m.getNick()))
		{
			MessageTokeniser token = new MessageTokeniser(m.getMessage());
			token.setConsumeWhitespace(true);

			token.consume(m.getNick());
			token.consume(":");

			if (token.consume("play with"))
			{
				String nick = token.nextToken(' ');
				String chan = m.getChannel();

				if ("me".equals(nick))
					nick = m.getSender();

				if (chan == null)
				{
					if (token.consume("in"))
						m.act(token.nextToken(' '), play(nick));
					else
						m.act(nick, play(nick));
				}
				else
				{
					m.act(play(nick));
				}
			}
		}

		if (m.isAction() && mess.toLowerCase().contains(m.getNick().toLowerCase()))
		{
			Matcher attentionMatcher = attention.matcher(mess);
			if (attentionMatcher.find())
				m.act(attend(m.getSender()));
		}

		// Stop DoCitten replying to itself as much
		if (mess.equals("mew =^.^="))
			return;

		StringBuilder reply = new StringBuilder(100);

		// Find things we need to mewl at
		Matcher kittenMatcher = kitten.matcher(mess);
		while (kittenMatcher.find())
			reply.append(mewl()).append(' ');

		Matcher mewlsMatcher = mewls.matcher(mess);
		while (mewlsMatcher.find())
			reply.append(mewl()).append(' ');

		if (reply.length() != 0)
		{
			reply.append("=^.^=");
			m.replyToAll(reply.toString());
		}
	}

	/**
	 * <p>Generates a 'mewl' based on the entropy source of this kitten</p>
	 * @return a random(ish) mewling sound
	 */
	private String mewl()
	{
		switch (r.nextInt(6))
		{
			case 0: return "nyaann";
			case 1: return "mraow";
			default: return "mew";
		}
	}

	/**
	 * <p>Get a string to be used as an action which indicates that the kitten
	 * is paying attention to something that just happened.</p>
	 * <p>The kitten may choose to attend in general, or pay attention to a
	 * given specific user.</p>
	 *
	 * @param user the user attention is given to. If null, "nice people" is
	 * substituted.
	 * @return how the kitten responds to the user, or null if it ignores the
	 * user
	 */
	private String attend(String user)
	{
		if (user == null)
			user = "nice people";
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

	/**
	 * <p>Get a string to be used as an action which indicates that the kitten
	 * is playing with someone.</p>
	 *
	 * @param user the user to play with. If null, "nice people" is
	 * substituted.
	 * @return how the kitten play with to the user
	 */
	private String play(String user)
	{
		switch (r.nextInt(6))
		{
			case 0:  return "paws at " + user;
			case 1:  return "curls up by " + user;
			case 2:  return "climbs onto " + user + "'s lap";
			case 3:  return "rubs againsts " + user + "'s legs";
			default: return "plays with " + user;
		}
	}

	@Override
	protected void startup(RelayCat r)
	{
		List<HelpService> helpServices = r.getServicesByClass(HelpService.class);

		if (!helpServices.isEmpty())
		{
			HelpService.HelpInfo help = new HelpService.HelpInfo("Kitten Service", "Kittens do not need your help. They do appreciate being looked after. But, there are plenty of other expendable humans to do that...\n \nIf you find a user who looks like they coudl use cheering up, you can ask the kitten to play with them.\r\n  play with <nick> [in <channel>]");
			helpServices.get(0).addHelp("kitten", help);
			helpServices.get(0).addHelp("kittens", help);
		}
	}

	@Override
	public void shutdown()
	{
		// Nothing to see here. Move along, citizen!
	}
}
