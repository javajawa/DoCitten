package uk.co.harcourtprogramming.docitten;

import java.util.regex.Pattern;
import uk.co.harcourtprogramming.internetrelaycats.Message;
import uk.co.harcourtprogramming.internetrelaycats.MessageService;
import uk.co.harcourtprogramming.internetrelaycats.RelayCat;
import uk.co.harcourtprogramming.internetrelaycats.Service;

/**
 * <p>Shouts 'HELPING!' whenever someone asks for help</p>
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
	final static String HELPING = "HELPING!!!";

	/**
	 * <p>Creates a Helping Service instance</p>
	 */
	public HelpingService()
	{
	}

	@Override
	public void handle(Message m)
	{
		if (HELP_PATTERN.matcher(m.getMessage()).find())
			m.replyToAll(HELPING);
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
