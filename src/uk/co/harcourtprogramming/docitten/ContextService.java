package uk.co.harcourtprogramming.docitten;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import uk.co.harcourtprogramming.docitten.utility.ArrayBuffer;
import uk.co.harcourtprogramming.internetrelaycats.FilterService;
import uk.co.harcourtprogramming.internetrelaycats.Message;
import uk.co.harcourtprogramming.internetrelaycats.MessageService;
import uk.co.harcourtprogramming.internetrelaycats.OutboundMessage;
import uk.co.harcourtprogramming.internetrelaycats.RelayCat;
import uk.co.harcourtprogramming.internetrelaycats.Service;
import uk.co.harcourtprogramming.mewler.MessageTokeniser;

import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;

/**
 * <p>Service for suppling context to users for when they join a channel</p>
 * <p>Intended for use for ping timeouts, dodgy networks, etc. Longer term
 * context should be handled by an IRC bouncer.</p>
 *
 * @author Benedict Harcourt / javajawa
 */
public class ContextService extends Service implements MessageService, FilterService
{

	/**
	 * <p>Storage for the history of attached channels</p>
	 */
	private final Map<String, ArrayBuffer<String>> channelHistories =
		new HashMap<String, ArrayBuffer<String>>(10);
	/**
	 * <p>Instance of {@link Calendar} for generating timestamps on the stored
	 * messages</p>
	 */
	private final Calendar c = Calendar.getInstance();

	/**
	 * <p>Creates a new Content Service instance</p>
	 */
	public ContextService()
	{
		// Nothing to seet here. Move along, citizen!
	}

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
				c.setTimeInMillis(currentTimeMillis());
				String mess = format("[%tR %s] %s", c, m.getSender(), m.getMessage());

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
			synchronized (channelHistories)
			{
				if (channelHistories.containsKey(m.getChannel()))
				{
					ArrayBuffer<String> hist = channelHistories.get(m.getChannel());
					StringBuilder buffer = new StringBuilder(50 * hist.getLength());

					for (int i = 0; i < hist.getLength(); ++i)
						buffer.append(hist.get(i)).append('\n');

					m.reply(buffer.toString());
				}
				else
				{
					m.reply("No context available for this channel.");
				}
			}
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
				"Get a report of the recent activity in a channel\r\n \r\nIn a channel, use 'DoCitten: context', and you will be sent the context");
			helpServices.get(0).addHelp("context", help);
		}
	}

	@Override
	protected void shutdown()
	{
		// Nothing to see here. Move along, citizen!
	}
}
