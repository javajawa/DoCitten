package uk.co.harcourtprogramming.docitten;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import uk.co.harcourtprogramming.internetrelaycats.Message;
import uk.co.harcourtprogramming.internetrelaycats.MessageService;
import uk.co.harcourtprogramming.internetrelaycats.RelayCat;
import uk.co.harcourtprogramming.internetrelaycats.Service;
import uk.co.harcourtprogramming.mewler.MessageTokeniser;

/**
 * <p>Service which gives a unified interface for help massages for services in
 * DoCitten</p>
 *
 * @author Benedict Harcourt / javajawa
 */
public class HelpService extends Service implements MessageService
{

	/**
	 * <p>Constant representing a new line character</p>
	 */
	private final static char EOL = '\n';
	/**
	 * <p>Constant that inserts a new line, followed by a 'blank' line</p>
	 * <p>For implementation reasons, the 'blank' line consisted of a single
	 * space</p>
	 */
	private final static String DOUBLE_EOL = "\n \n";

	/**
	 * <p>Stores a node in the {@link HelpService} system</p>
	 *
	 * @author Benedict Harcourt / javajawa
	 */
	public final static class HelpInfo
	{

		/**
		 * <p>Container for sub-topics of this help item</p>
		 * <p>The map is of the string used to identify the topic to the topic's
		 * HelpInfo instance</p>
		 *
		 * @see #getChildren()
		 * @see #toString()
		 */
		private final Map<String, HelpInfo> children = new TreeMap<String, HelpInfo>();
		/**
		 * <p>The title of the topic to display at the top of the
		 * {@link #toString() message}, and in any help listings</p>
		 *
		 * @see #getTitle()
		 * @see #toString()
		 */
		private final String title;
		/**
		 * <p>The main content text of this help item.</p>
		 *
		 * @see #getDescription()
		 * @see #toString()
		 */
		private final String content;

		/**
		 * <p>Creates a new help object instance with a given title and content.
		 * </p>
		 *
		 * @param title The title of this item (see {@link #title})
		 * @param content The content for this item (see {@link #content})
		 * @see #toString()
		 */
		public HelpInfo(String title, String content)
		{
			this.title = title;
			this.content = content;
		}

		/**
		 * <p>Adds a sub-item to this help item</p>
		 * <p>The item is then accesses with the keyword supplied, replacing any
		 * previous entry with that keyword.</p>
		 * <p>Note that there are no checks for recursion in the help system.
		 * Having recursion is permitted, but my be confusing to users.</p>
		 *
		 * @param keyword The keyword to use for this sub-item
		 * @param item The help item to map to this keyword to.
		 * @return Any item that was replaced
		 * @see #children
		 * @see Map#put(java.lang.Object, java.lang.Object) Map.put()
		 */
		public HelpInfo addChild(String keyword, HelpInfo item)
		{
			return children.put(keyword, item);
		}

		/**
		 * <p>Outputs the complete help item as a string</p>
		 * <p>Format is:</p>
		 * <ul>
		 * <li>The {@link #title title} of the item</li>
		 * <li>The {@link #content content} of the item</li>
		 * <li>A list of any {@link #children child items}</li>
		 * </ul>
		 * </p>
		 * <p>Blank lines use {@link HelpService#DOUBLE_EOL}</p>
		 *
		 * @return The entire help item as a string
		 * @see #title
		 * @see #content
		 * @see #children
		 */
		@Override
		public String toString()
		{
			StringBuilder s = new StringBuilder(content.length() + 100);

			s.append(title).append(DOUBLE_EOL);
			s.append(content);

			if (children.isEmpty())
				s.append(EOL);
			else
			{
				s.append(DOUBLE_EOL).append("Sub-Topics").append(EOL);
				for (String child : children.keySet())
				{
					s.append("  ").append(child).append(": ");
					s.append(children.get(child).getTitle()).append(EOL);
				}
			}

			return s.toString();
		}

		/**
		 * <p>Gets the {@link #title title} of this item</p>
		 *
		 * @return the {@link #title title} of this item
		 * @see #title
		 */
		public String getTitle()
		{
			return title;
		}

		/**
		 * <p>Gets the {@link #content content} of this item</p>
		 *
		 * @return the {@link #content content} of this item
		 * @see #content
		 */
		public String getDescription()
		{
			return content;
		}

		/**
		 * <p>Returns the sub-items of this item as a map of keywords to
		 * {@link HelpInfo} objects</p>
		 *
		 * @return A map of all sub-items
		 * @see #addChild(java.lang.String,
		 * uk.co.harcourtprogramming.docitten.HelpService.HelpInfo)
		 * addChild(String, HelpInfo)
		 * @see #children
		 */
		public Map<String, HelpInfo> getChildren()
		{
			return Collections.unmodifiableMap(children);
		}
	}
	/**
	 * <p>The root help item in this service</p>
	 */
	private final HelpInfo root;

	/**
	 * <p>Creates a new help service with the default root item</p>
	 * <p>The default root item is configured to use the text:</p>
	 * <pre>This kitten comes equipped with a help module.
	 * For more information, try 'help help', or one of the other help
	 * options below</pre>
	 * <p>and one sub item, which is stored in 'help'</p>
	 * <pre>Help Service
	 *
	 * The help service uses a recursive tree structure, and allows any DoCitten
	 * module to add help information.</pre>
	 */
	public HelpService()
	{
		root = new HelpInfo("Help", "This kitten comes equipped with a help module.\nFor more information, try 'help help', or one of the other help options below");
		root.addChild("help", new HelpInfo("Help Service", "The help service uses a recursive tree structure, and allows any DoCitten module to add help information."));
		root.addChild("about", new HelpInfo("About DoCitten", "DoCitten was originally an irc bot for #doc, Imperial College Department of Computing's IRC channel\r\n \r\nDoCitten is from https://github.com/javajawa/DoCitten"));
	}

	/**
	 * <p>Creates a help service using a custom root item</p>
	 *
	 * @param root the root item to use for this help service
	 * @throws IllegalArgumentException if root is null
	 */
	public HelpService(HelpInfo root)
	{
		if (root == null)
			throw new IllegalArgumentException("Root help item can not be null");

		this.root = root;
	}

	/**
	 * <p>Adds a {@link HelpInfo} instance as a child of the root help item</p>
	 * <p>As well as wrapping root.addChild, this function will also log with
	 * level info that the item was added</p>
	 *
	 * @param key The keyword to use for this sub-item
	 * @param info The help item to map to this keyword to.
	 * @return Any item that was replaced
	 * @see HelpInfo#addChild(java.lang.String,
	 * uk.co.harcourtprogramming.docitten.HelpService.HelpInfo)
	 * HelpInfo.addChild
	 */
	public final HelpInfo addHelp(String key, HelpInfo info)
	{
		log(Level.INFO, "Help added for {0}", new Object[] {key});
		return root.addChild(key, info);
	}

	/**
	 * <p>Handles the message</p>
	 * <p>First, a check is performed to make sure that the bot was directly
	 * addressed for help. In a channel, this means the line begins with
	 * <code>&lt;nick&gt;: help</code>. In a private message, only the keyword
	 * help is needed.</p>
	 * <p>The rest of the message is then treated as a series of child nodes to
	 * descend down to select the item.</p>
	 * <p>If the item is found, it is outputted via {@link HelpInfo#toString()},
	 * and sent directly back to the user.</p>
	 *
	 * @param m The message data
	 */
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
			{
				currentNode = currentNode.getChildren().get(topic);
				if (currentNode == null)
				{
					m.reply(String.format("Unknown sub-topic: '%1$s'", topic));
					return;
				}
			}
			else
			{
				m.reply(String.format("Unknown sub-topic: '%1$s'", topic));
				return;
			}
		}

		m.reply(currentNode.toString());
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
