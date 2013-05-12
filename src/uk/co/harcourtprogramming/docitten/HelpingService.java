package uk.co.harcourtprogramming.docitten;

import java.util.Random;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import uk.co.harcourtprogramming.internetrelaycats.Message;
import uk.co.harcourtprogramming.internetrelaycats.MessageService;
import uk.co.harcourtprogramming.internetrelaycats.RelayCat;
import uk.co.harcourtprogramming.internetrelaycats.Service;

/**
 * <p>Shouts 'HELPING!' whenever someone asks for help, and occasionally portmanteau two word phrases</p>
 *
 * @author Benedict Harcourt / javajawa
 */
public class HelpingService extends Service implements MessageService
{

	/**
	 * <p>Regex to determine when someone is asking for help</p>
	 */
	private final static Pattern HELP_PATTERN =
	    Pattern.compile("(^|\\s)(ha*l*p|assist(ance|ence)?|aid)([^\\w]|$)", Pattern.CASE_INSENSITIVE);
	/**
	 * <p>What to say when help is asked for</p>
	 */
	protected final static String HELPING = "HELPING!!!";

	/**
	 * <p>Pattern to match two simple words with vowel.</p>
	 */
	private final static Pattern PORT_PATTERN = Pattern.compile("^(?<a>[A-Z]?[b-df-hj-np-tv-z]+)[aeiou][a-z]* (and |or |\\+ |& )?[A-Z]?[b-df-hj-np-tv-z]+(?<b>[aeiou][a-z]*)[.\\?! ]?( :[D\\)3])?$", Pattern.CASE_INSENSITIVE);

	/**
	 * <p>Random number generator for deciding whether to portmanteau</p>
	 */
	private final Random rand;

	/**
	 * <p>Creates a Helping Service instance</p>
	 */
	public HelpingService()
	{
		this(new Random());
	}

	protected HelpingService(Random rand)
	{
		this.rand = rand;
	}

	@Override
	public void handle(Message m)
	{
		if (HELP_PATTERN.matcher(m.getMessage()).find())
			m.replyToAll(HELPING);

		Matcher ma = PORT_PATTERN.matcher(m.getMessage());
		if (ma.matches() && rand.nextDouble() < 0.2)
		{
			final String portmanteau = ma.group("a") + ma.group("b");
			if (m.getMessage().contains(portmanteau))
				return;

			m.replyToAll(portmanteau);
		}

	}

	@Override
	protected void startup(RelayCat r)
	{
		// Nothing to see here. Move along, citizen!
	}

	@Override
	public void shutdown()
	{
		// Nothing to see here. Move along, citizen!
	}
}

