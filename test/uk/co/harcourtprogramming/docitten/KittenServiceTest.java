package uk.co.harcourtprogramming.docitten;

import org.junit.Test;
import static org.junit.Assert.*;
import uk.co.harcourtprogramming.internetrelaycats.Message;
import uk.co.harcourtprogramming.internetrelaycats.TestingRelayCat;

/**
 *
 * @author Benedict
 */
public class KittenServiceTest
{
	public KittenServiceTest()
	{
	}

	@Test
	public void testMew()
	{
		final KittenService instance = new KittenService();

		final TestingRelayCat c = new TestingRelayCat();
		c.addService(instance);

		final String user = "bob";
		final String channel = null;
		c.inputMessage(user, channel, "mew");

		Message m = c.getOutput();

		assertNotNull(m);
		assertEquals("Not replied to user", m.getChannel(), user);

		assertNull(c.getOutput());
	}
}
