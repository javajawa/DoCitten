package uk.co.harcourtprogramming.docitten;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import uk.co.harcourtprogramming.internetrelaycats.Message;
import uk.co.harcourtprogramming.internetrelaycats.TestingRelayCat;

/**
 *
 */
@RunWith(Parameterized.class)
public class ParametisedKittenTest
{

	public static class MessageData
	{
		private final String input;
		private final String[] outputs;
		private final boolean action;
		private final String channel;

		MessageData(Boolean action, String channel, String input, String ... outputs)
		{
			this.action = action;
			this.channel = channel;
			this.input = input;
			this.outputs = outputs;
		}
	}

	/**
	 * @return The parameter data for a paramterised test
	 */
	@Parameterized.Parameters
	public static Collection<MessageData[]> data()
	{
		return Arrays.asList(new MessageData[][]{
			new MessageData[]{new MessageData(Boolean.FALSE, "#flub", "mewwww", "nyaann =^.^=")}
		});
	}

	/**
	 * Parameter data for this test instance
	 */
	private final String input;
	private final String[] outputs;
	private final boolean action;
	private final String channel;

	/**
	 * Create a LinkServiceTest with a given parameter
	 */
	public ParametisedKittenTest(MessageData m)
	{
		action = m.action;
		channel = m.channel;
		input = m.input;
		outputs = m.outputs;
	}

	@Test
	public void testMew() throws UnknownHostException, IOException
	{
		final KittenService instance = new KittenService(new Random() {
			private static final long serialVersionUID = 1L;
			@Override
			public int nextInt(int n)
			{
				return 0;
			}
		});

		final TestingRelayCat c = new TestingRelayCat();
		c.addService(instance);

		final String user = "bob";
		if (action)
		{
			c.inputAction(user, channel, input);
		}
		else
		{
			c.inputMessage(user, channel, input);
		}

		int i = 0;

		for (String line : outputs)
		{
			Message m = c.getOutput();
			++i;
			assertNotNull("Line " + i + " was not generated", m);
			assertEquals("Line " + i + " did not match given value", line, m.getMessage());
		}

		assertNull("Extra lines generated", c.getOutput());
	}
}
