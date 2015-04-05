package uk.co.harcourtprogramming.docitten;

import java.util.Arrays;
import java.util.Collection;
import java.util.Random;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import uk.co.harcourtprogramming.internetrelaycats.Message;
import uk.co.harcourtprogramming.internetrelaycats.TestingRelayCat;

@RunWith(Parameterized.class)
public class HelpingServiceTest
{

	private final static TestingRelayCat cat = new TestingRelayCat();
	private final static HelpingService srv = new HelpingService(new Random() {
		private static final long serialVersionUID = 1L;
		@Override
		public double nextDouble()
		{
			return 0.0;
		}
	});

	@BeforeClass
	public static void createTestRelayCat()
	{
		cat.addService(srv);
	}

	@AfterClass
	public static void destroyTestRelayCat()
	{
		cat.shutdown();
	}

	@Parameterized.Parameters
	public static Collection<Object[]> data()
	{
		return Arrays.asList(new Object[][] {
			new String[]{"help me!", null},
			new String[]{"halp!", HelpingService.HELPING},
			new String[]{"halp?", HelpingService.HELPING},
			new String[]{"halp", HelpingService.HELPING},
			new String[]{"i need halp", HelpingService.HELPING},
			new String[]{"bob", null},
			new String[]{"Can I get some assistance with this?", HelpingService.HELPING},
			new String[]{"Can I get some assistence with this?", HelpingService.HELPING},
			new String[]{"hlp", null},
			new String[]{"hp", null},

			new String[]{"happy kitteh", "hitteh"},
			new String[]{"happy kitten :3", "hitten"},
			new String[]{"silly sally", null},
			new String[]{"cat and mouse", "couse"},
			new String[]{"batman and robin", "bobin"},
			new String[]{"Batman and robin", "Bobin"},
			new String[]{"batman and Robin", "bobin"},
			new String[]{"Batman and Robin", "Bobin"},
			new String[]{"batman & robin", "bobin"},
		});
	}

	private final String input;
	private final String output;

	public HelpingServiceTest(String s, String help)
	{
		this.input = s;
		this.output = help;
	}

	/**
	 * For each supplied parameter, test whether the HelpingService generates
	 * the given output (either HelpingService.HELP or none) from a given input
	 * The version sends to a channel, and expects replies to, and only to,
	 * the channel
	 */
	@Test
	public void testMessageViaChannel()
	{
		final String channel = "#doc";
		final String user = "bob";
		cat.inputMessage(user, channel, input);

		Message reply = cat.getOutput();
		if (output == null)
		{
			assertNull("Unexpected reply to " + input, reply);
			return;
		}

		assertNotNull("No reply was generared for " + input, reply);
		assertEquals("Not sent back to channel", channel, reply.getChannel());
		assertEquals("Not correct reply for " + input, output, reply.getMessage());

		assertNull("More than one message generated", cat.getOutput());
	}


	/**
	 * For each supplied parameter, test whether the HelpingService generates
	 * the given output (either HelpingService.HELP or none) from a given input
	 * The version sends to a user, and expects replies to, and only to,
	 * the user
	 */
	@Test
	public void testMessageDirectFromUser()
	{
		final String channel = null;
		final String user = "bob";
		cat.inputMessage(user, channel, input);

		Message reply = cat.getOutput();
		if (output == null)
		{
			assertNull("Unexpected reply to " + input, reply);
			return;
		}

		assertNotNull("No reply was generared for " + input, reply);
		assertEquals("Not sent back to user", user, reply.getChannel());
		assertEquals("Not correct reply for " + input, output, reply.getMessage());

		assertNull("More than one message generated", cat.getOutput());
	}
}
