package uk.co.harcourtprogramming.docitten;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import uk.co.harcourtprogramming.docitten.utility.Conversation;
import uk.co.harcourtprogramming.internetrelaycats.Message;
import uk.co.harcourtprogramming.internetrelaycats.TestingRelayCat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * <p>Tests the parsing of messages to find web links</p>
 */
@RunWith(Parameterized.class)
public class CatPicOfTheDayTest
{
	/**
	 * @return The parameter data for a parameterised test
	 */
	@Parameterized.Parameters
	public static List<Conversation[]> data()
	{
		List<Conversation[]> conversations = new ArrayList<>(3);

		conversations.add(new Conversation[] {
			new Conversation("Null Test")
		});

		conversations.add(new Conversation[] {
			new Conversation("Empty Queue Check")
				.send("bob", null, "catpic queue" )
				.recieve(TestingRelayCat.NAME, "bob", "Current queue length: 0")
		});

		conversations.add(new Conversation[] {
			new Conversation("Add to Queue")
				.send("bob", null, "catpic http://i.imgur.com/zZ0iGK5.png" )
				.recieve(TestingRelayCat.NAME, "bob", "http://i.imgur.com/zZ0iGK5.png added")
				.send("bob", null, "catpic queue" )
				.recieve(TestingRelayCat.NAME, "bob", "Current queue length: 1")
				.recieve(TestingRelayCat.NAME, "bob", "http://i.imgur.com/zZ0iGK5.png")
		});

		conversations.add(new Conversation[] {
			new Conversation("Add to Queue in channel")
				.send("bob", "#doc", TestingRelayCat.NAME + ": catpic http://i.imgur.com/zZ0iGK5.png" )
				.recieve(TestingRelayCat.NAME, "bob", "http://i.imgur.com/zZ0iGK5.png added")
				.send("bob", "#doc", TestingRelayCat.NAME + ": catpic queue" )
				.recieve(TestingRelayCat.NAME, "bob", "Current queue length: 1")
				.recieve(TestingRelayCat.NAME, "bob", "http://i.imgur.com/zZ0iGK5.png")
		});

		conversations.add(new Conversation[] {
			new Conversation("Add to Queue in channel")
				.send("bob", "#doc", "catpic http://i.imgur.com/zZ0iGK5.png" )
		});

		conversations.add(new Conversation[] {
			new Conversation("Add twice to Queue")
				.send("bob", null, "catpic http://i.imgur.com/zZ0iGK5.png" )
				.recieve(TestingRelayCat.NAME, "bob", "http://i.imgur.com/zZ0iGK5.png added")
				.send("bob", null, "catpic http://i.imgur.com/zZ0iGK5.png" )
				.send("bob", null, "catpic queue" )
				.recieve(TestingRelayCat.NAME, "bob", "Current queue length: 1")
				.recieve(TestingRelayCat.NAME, "bob", "http://i.imgur.com/zZ0iGK5.png")
		});

		conversations.add(new Conversation[] {
			new Conversation("Add then remove Queue")
				.send("bob", null, "catpic http://i.imgur.com/zZ0iGK5.png" )
				.recieve(TestingRelayCat.NAME, "bob", "http://i.imgur.com/zZ0iGK5.png added")
				.send("bob", null, "catpic remove http://i.imgur.com/zZ0iGK5.png" )
				.recieve(TestingRelayCat.NAME, "bob", "java.util.List.remove returned true")
				.send("bob", null, "catpic queue" )
				.recieve(TestingRelayCat.NAME, "bob", "Current queue length: 0")
		});

		conversations.add(new Conversation[] {
			new Conversation("Remove Non-Existant")
				.send("bob", null, "catpic remove http://i.imgur.com/zZ0iGK5.png" )
				.recieve(TestingRelayCat.NAME, "bob", "java.util.List.remove returned false")
		});

		// From issue #25 (https://github.com/javajawa/DoCitten/issues/25)
		conversations.add(new Conversation[] {
			new Conversation("Add to Queue -- Issue #21, example 1")
				.send("javajawa", null, "catpic add https://vine.co/v/ipJWFHrKTWY/embed/simple?audio=1" )
				.recieve(TestingRelayCat.NAME, "javajawa", "https://vine.co/v/ipJWFHrKTWY/embed/simple?audio=1 added")
				.send("javajawa", null, "catpic queue" )
				.recieve(TestingRelayCat.NAME, "javajawa", "Current queue length: 1")
				.recieve(TestingRelayCat.NAME, "javajawa", "https://vine.co/v/ipJWFHrKTWY/embed/simple?audio=1")
		});

		// From issue #25 (https://github.com/Edgemaster/DoCitten/issues/25)
		conversations.add(new Conversation[] {
			new Conversation("Add to Queue -- Issue #25, exmaple 2")
				.send("Edgemaster", null, "catpic add https://scontent-lhr3-1.xx.fbcdn.net/hphotos-xfp1/v/t1.0-9/fr/cp0/e15/q65/12742637_1687632408151978_4655998892409811438_n.jpg?oh=d97f9b95c50ed8b0c806a3ef34dd2290&oe=57B2ABD7" )
				.recieve(TestingRelayCat.NAME, "Edgemaster", "https://scontent-lhr3-1.xx.fbcdn.net/hphotos-xfp1/v/t1.0-9/fr/cp0/e15/q65/12742637_1687632408151978_4655998892409811438_n.jpg?oh=d97f9b95c50ed8b0c806a3ef34dd2290&oe=57B2ABD7 added")
				.send("Edgemaster", null, "catpic queue" )
				.recieve(TestingRelayCat.NAME, "Edgemaster", "Current queue length: 1")
				.recieve(TestingRelayCat.NAME, "Edgemaster", "https://scontent-lhr3-1.xx.fbcdn.net/hphotos-xfp1/v/t1.0-9/fr/cp0/e15/q65/12742637_1687632408151978_4655998892409811438_n.jpg?oh=d97f9b95c50ed8b0c806a3ef34dd2290&oe=57B2ABD7")
		});

		return conversations;
	}
	/**
	 * <p>Parameter data for this test instance</p>
	 */
	private final Conversation conversation;

	private final TestingRelayCat cat;

	/**
	 * Create a LinkServiceTest with a given parameter
	 *
	 * @param conversation the link data for this test
	 */
	public CatPicOfTheDayTest(Conversation conversation)
	{
		this.conversation = conversation;

		cat = new TestingRelayCat();
		cat.addService(new CatPicOfTheDayService(cat));
	}

	@Test
	public void testConversation()
	{
		Iterator<Conversation.Line> states = conversation.iterator();
		int i = 0;
		Conversation.Line l;

		while (states.hasNext())
		{
			++i;
			l = states.next();

			switch ( l.getType() )
			{
				case Conversation.SEND:
					assertNull("Extra message recieved at " + conversation.getName() + ':' + i, cat.getOutput());

					cat.inputMessage(l.getSource(), l.getTarget(), l.getData());
					break;

				case Conversation.RECIEVE:
					Message message = cat.getOutput();

					assertNotNull("No message recieved at " + conversation.getName() + ':' + i, message);

					assertEquals("Incorrect source at " + conversation.getName() + ':' + i, l.getSource(), message.getSender());
					assertEquals("Incorrect target at " + conversation.getName() + ':' + i, l.getTarget(), message.getChannel());
					assertEquals("Incorrect message at " + conversation.getName() + ':' + i, l.getData(), message.getMessage());

					break;

				case Conversation.WAIT:
					int time = Integer.parseInt( l.getData() );

					try
					{
						Thread.sleep( time );
					}
					catch (InterruptedException ex)
					{
						throw new RuntimeException("", ex);
					}

					break;

				default:
					throw new IllegalStateException();
			}
		}

		assertNull("Extra message recieved at " + conversation.getName() + ':' + i, cat.getOutput());
	}
}
