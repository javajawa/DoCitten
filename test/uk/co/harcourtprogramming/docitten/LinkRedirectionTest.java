package uk.co.harcourtprogramming.docitten;

import org.junit.Test;
import static org.junit.Assert.*;
import uk.co.harcourtprogramming.internetrelaycats.RelayCat;

public class LinkRedirectionTest
{
	private final String uri = "http://www.harcourtprogramming.co.uk/redirect.php/1";
	private final String title = "(Unresolved after 5 hops)";
	private final String site = "harcourtprogramming.co.uk";
	private final String expected = String.format("[%s] %s", site, title);

	private String target = null;
	private String message = null;

	private final RelayCat cat = new RelayCat() {
		@Override
		public void message(String target, String message)
		{
			LinkRedirectionTest.this.target  = target;
			LinkRedirectionTest.this.message = message;
		}

		@Override
		public void act(String target, String message)
		{
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public void join(String channel)
		{
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public void leave(String channel)
		{
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public String getNick()
		{
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public String[] names(String channel)
		{
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public String[] channels()
		{
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public boolean isConnected()
		{
			throw new UnsupportedOperationException("Not supported yet.");
		}
	};

	@Test
	@SuppressWarnings("CallToThreadRun")
	public void TestRedirectResolution() throws InterruptedException
	{
		final String nick = "bob";

		LinkResolver r = new LinkResolver(uri, cat, nick);

		r.run();

		assertNotNull(target);
		assertEquals(nick, target);
		assertNotNull(message);
		assertEquals(expected, message);
	}
}
