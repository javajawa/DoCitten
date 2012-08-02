package uk.co.harcourtprogramming.docitten;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import uk.co.harcourtprogramming.docitten.utility.ArrayBuffer;
import uk.co.harcourtprogramming.internetrelaycats.FilterService;
import uk.co.harcourtprogramming.internetrelaycats.Message;
import uk.co.harcourtprogramming.internetrelaycats.MessageService;
import uk.co.harcourtprogramming.mewler.MessageTokeniser;
import uk.co.harcourtprogramming.internetrelaycats.OutboundMessage;
import uk.co.harcourtprogramming.internetrelaycats.RelayCat;
import uk.co.harcourtprogramming.internetrelaycats.Service;

/**
 *
 * @author Benedict
 */
public class ContextService extends Service implements MessageService, FilterService
{

	private final Map<String, ArrayBuffer<String>> channelHistories =
		new HashMap<String, ArrayBuffer<String>>();
	private final Calendar c = Calendar.getInstance();

	@Override
	public void handle(Message m)
	{
		MessageTokeniser t = new MessageTokeniser(m.getMessage());

		t.setConsumeWhitespace(true);

		if (m.getChannel() == null)
			return;

		// Check that the bot is being asked for context in a channel
		if (!t.consume(m.getNick() + ": context"))
		{
			synchronized (channelHistories)
			{
				c.setTimeInMillis(System.currentTimeMillis());
				String mess = String.format("[%tR %s] %s", c, m.getSender(), m.getMessage());

				if (!channelHistories.containsKey(m.getChannel()))
				{
					ArrayBuffer<String> buf = new ArrayBuffer<String>(10, "");
					buf.add(mess);
					channelHistories.put(m.getChannel(), buf);
				}
				else
				{
					channelHistories.get(m.getChannel()).add(mess);
				}
			}
		}
		else
		{
			StringBuilder buffer = new StringBuilder();
			synchronized (channelHistories)
			{
				if (channelHistories.containsKey(m.getChannel()))
				{
					ArrayBuffer<String> buf = channelHistories.get(m.getChannel());
					for (int i = 0; i < buf.getLength(); ++i)
						buffer.append(buf.get(i)).append('\n');
				}
				else
				{
					m.reply("No context available for this channel.");
					return;
				}
			}

			m.reply(buffer.toString());
		}
	}

	@Override
	public OutboundMessage filter(OutboundMessage m)
	{
		if (m.getTarget().startsWith("#") || m.getTarget().startsWith("&"))
		{
			synchronized (channelHistories)
			{
				c.setTimeInMillis(System.currentTimeMillis());
				String mess = String.format("[%tR %s] %s", c, m.getNick(), m.getMessage());

				if (!channelHistories.containsKey(m.getTarget()))
				{
					ArrayBuffer<String> buf = new ArrayBuffer<String>(10, "");
					buf.add(mess);
					channelHistories.put(m.getTarget(), buf);
				}
				else
				{
					channelHistories.get(m.getTarget()).add(mess);
				}
			}
		}
		return m;
	}

	@Override
	protected void startup(RelayCat r)
	{
				List<HelpService> helpServices = r.getServicesByClass(HelpService.class);

		if (!helpServices.isEmpty())
		{
			HelpService.HelpInfo help = new HelpService.HelpInfo(
				"Context Service",
				"Get a report of the recent activity in a channel");
			helpServices.get(0).addHelp("context", help);
		}
	}

	@Override
	protected void shutdown()
	{
		// Nothing to see here. Move along, citizen!
	}

}
