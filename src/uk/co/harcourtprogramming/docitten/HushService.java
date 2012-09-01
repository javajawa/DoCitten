package uk.co.harcourtprogramming.docitten;

import java.util.HashSet;
import java.util.Set;
import uk.co.harcourtprogramming.internetrelaycats.FilterService;
import uk.co.harcourtprogramming.internetrelaycats.Message;
import uk.co.harcourtprogramming.internetrelaycats.MessageService;
import uk.co.harcourtprogramming.internetrelaycats.OutboundMessage;
import uk.co.harcourtprogramming.internetrelaycats.RelayCat;
import uk.co.harcourtprogramming.internetrelaycats.Service;
import uk.co.harcourtprogramming.mewler.MessageTokeniser;

/**
 * <p>Rough and ready service for making the bots silent</p>
 * @author Benedict Harcourt / javajawa
 */
public class HushService extends Service implements MessageService, FilterService
{
	/**
	 * <p>A list of all channels and users in which DoCitten has been told to be
	 * quiet</p>
	 */
	private final Set<String> hushedTargets = new HashSet<String>();

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

	@Override
	public void handle(Message m)
	{
		MessageTokeniser tokeniser = new MessageTokeniser(m.getMessage());
		tokeniser.setConsumeWhitespace(true);
		String sender;

		if (m.getChannel() != null)
		{
			if (!tokeniser.startsWith(m.getNick()))
				return;

			tokeniser.consume(m.getNick());
			tokeniser.consume(":");
			sender = m.getChannel();
		}
		else
		{
			sender = m.getNick();
		}

		if (tokeniser.toString().matches("hush!*"))
		{
			synchronized(hushedTargets)
			{
				hushedTargets.add(sender);
			}
		}
		else if (tokeniser.toString().matches("speak!*"))
		{
			synchronized(hushedTargets)
			{
				hushedTargets.remove(sender);
			}
		}
	}

	@Override
	public OutboundMessage filter(OutboundMessage m)
	{
		synchronized (hushedTargets)
		{
			if (hushedTargets.contains(m.getTarget()))
				return null;
		}
		return m;
	}

}
