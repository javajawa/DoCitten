package uk.co.harcourtprogramming.docitten;

import java.util.HashSet;
import java.util.Set;
import uk.co.harcourtprogramming.internetrelaycats.FilterService;
import uk.co.harcourtprogramming.internetrelaycats.Message;
import uk.co.harcourtprogramming.internetrelaycats.MessageService;
import uk.co.harcourtprogramming.internetrelaycats.MessageTokeniser;
import uk.co.harcourtprogramming.internetrelaycats.RelayCat;
import uk.co.harcourtprogramming.internetrelaycats.OutboundMessage;
import uk.co.harcourtprogramming.internetrelaycats.Service;

public class HushService extends Service implements MessageService, FilterService
{
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

		if (m.getChannel() != null)
		{
			if (!tokeniser.startsWith(m.getNick()))
				return;

			tokeniser.consume(m.getNick());
			tokeniser.consume(":");

			if (tokeniser.toString().matches("hush!*"))
			{
				synchronized(hushedTargets)
				{
					hushedTargets.add(m.getChannel());
				}
			}
			else if (tokeniser.toString().matches("speak!*"))
			{
				synchronized(hushedTargets)
				{
					hushedTargets.remove(m.getChannel());
				}
			}
		}
		else
		{
			if (tokeniser.toString().matches("hush!*"))
			{
				synchronized(hushedTargets)
				{
					hushedTargets.add(m.getSender());
				}
			}
			else if (tokeniser.toString().matches("speak!*"))
			{
				synchronized(hushedTargets)
				{
					hushedTargets.remove(m.getChannel());
				}
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
