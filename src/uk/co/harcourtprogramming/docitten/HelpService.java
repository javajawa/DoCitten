package uk.co.harcourtprogramming.docitten;

import java.util.Map;
import java.util.TreeMap;
import uk.co.harcourtprogramming.internetrelaycats.*;

/**
 *
 * @author Benedict
 */
public class HelpService extends Service implements MessageService
{
	private final static String EOL = "\r\n";
	private final static String DOUBLE_EOL = "\r\n \r\n";

	public final static class HelpInfo
	{
		private final Map<String, HelpInfo> children = new TreeMap<String, HelpInfo>();
		private final String title;
		private final String description;

		public HelpInfo(String title, String description)
		{
			this.title = title;
			this.description = description;
		}

		/**
		 *
		 * @param keyword
		 * @param item
		 * @return Any item that was replaced
		 * @see Map#put(java.lang.Object, java.lang.Object)
		 */
		public HelpInfo addChild(String keyword, HelpInfo item)
		{
			return getChildren().put(keyword, item);
		}

		private String toMessage()
		{
			StringBuilder s = new StringBuilder(description.length() + 100);

			s.append(title).append(DOUBLE_EOL);
			s.append(description);

			if (children.isEmpty())
				s.append(EOL);
			else
			{
				s.append(DOUBLE_EOL).append("Sub-Topics").append(EOL);
				for (String child : getChildren().keySet())
				{
					s.append("  ").append(child).append(": ");
					s.append(getChildren().get(child).getTitle()).append(EOL);
				}
			}

			return s.toString();
		}

		public String getTitle()
		{
			return title;
		}

		private Map<String, HelpInfo> getChildren()
		{
			return children;
		}

		public String getDescription()
		{
			return description;
		}

	}

	private final HelpInfo root;

	public HelpService()
	{
		root = new HelpInfo("Help", "This kitten comes equipped with a help module.\r\nFor more information, try 'help help', or one of the other help options below");
		root.addChild("help", new HelpInfo("Help Service", "The help service uses a recusive tree structure, and allows an DoCitten module to add help information."));
	}

	public final HelpInfo addHelp(String key, HelpInfo info)
	{
		return root.addChild(key, info);
	}

	@Override
	public void handle(Message m)
	{
		MessageTokeniser t = new MessageTokeniser(m.getMessage());

		t.setConsumeWhitespace(true);

		// Check that the bot is actually being addressed in some way
		if (!t.consume(m.getNick() + ':') && m.getChannel() != null)
			return;

		if (!t.consume("help"))
			return;

		HelpInfo currentNode = root;

		while (!t.isEmpty())
		{
			String topic = t.nextToken(' ');
			if (currentNode.getChildren().containsKey(topic))
				currentNode = currentNode.getChildren().get(topic);
			else
			{
				m.reply(String.format("Unknown sub-topic: '%1$s'", topic));
				return;
			}
		}

		m.reply(currentNode.toMessage());
	}

	@Override
	protected void startup(RelayCat r)
	{
		// Nothing to see here. Move along, citizen!
	}

	@Override
	protected void shutdown()
	{
		// Nothing to see here. Move along, citizen!
	}
}
