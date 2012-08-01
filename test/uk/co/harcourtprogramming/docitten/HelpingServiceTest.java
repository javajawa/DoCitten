package uk.co.harcourtprogramming.docitten;

import java.util.Arrays;
import java.util.Collection;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.junit.Assert.*;
import uk.co.harcourtprogramming.internetrelaycats.Message;
import uk.co.harcourtprogramming.internetrelaycats.TestingRelayCat;

@RunWith(Parameterized.class)
public class HelpingServiceTest
{

	private final static TestingRelayCat cat = new TestingRelayCat();
	private final static HelpingService srv = new HelpingService();

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
			new Object[]{"help me", false},
			new Object[]{"halp!", true},
			new Object[]{"halp?", true},
			new Object[]{"halp", true},
			new Object[]{"i need halp", true},
			new Object[]{"bob", false}
		});
	}

	private final String input;
	private final boolean output;

	public HelpingServiceTest(String s, boolean help)
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
		if (!output)
		{
			assertNull("Unexpected reply with " + input, reply);
			return;
		}

		assertNotNull("No reply was generared for " + input, reply);
		assertEquals("Not sent back to channel", channel, reply.getChannel());
		assertEquals("Not correct reply", HelpingService.HELPING, reply.getMessage());

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
		if (!output)
		{
			assertNull("Unexpected reply with " + input, reply);
			return;
		}

		assertNotNull("No reply was generared for " + input, reply);
		assertEquals("Not sent back to user", user, reply.getChannel());
		assertEquals("Not correct reply", HelpingService.HELPING, reply.getMessage());

		assertNull("More than one message generated", cat.getOutput());
	}
}
