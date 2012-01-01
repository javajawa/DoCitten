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

/**
 *
 * @author Benedict
 */
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
			new Object[]{"help me", true},
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
	 * Test of handle method, of class HelpingService.
	 */
	@Test
	public void testMessageChannel()
	{
		final String channel = "#doc";
		cat.inputMessage("bob", channel, input);

		Message reply = cat.getOutput();
		if (!output)
		{
			assertNull("Unexpected reply with " + input, reply);
			return;
		}

		assertNotNull("No reply was generared for " + input, reply);
		assertEquals("Not sent back to channel", channel, reply.getChannel());
		assertEquals("Nto correct reply", HelpingService.HELPING, reply.getMessage());

		assertNull("More than one message generated", cat.getOutput());
	}
}
