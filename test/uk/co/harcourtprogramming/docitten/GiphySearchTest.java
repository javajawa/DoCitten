package uk.co.harcourtprogramming.docitten;

import java.util.List;
import org.junit.Test;
import uk.co.harcourtprogramming.internetrelaycats.RelayCat;
import uk.co.harcourtprogramming.internetrelaycats.Service;

import static org.junit.Assert.*;

public class GiphySearchTest
{
	private final String uri = "funny cat";
	private final String expected = "http://giphy.com/embed/ivelTBasMg67S";

	private String target = null;
	private String message = null;

	private final RelayCat cat = new RelayCat() {
		@Override
		public void message(String target, String message)
		{
			GiphySearchTest.this.target  = target;
			GiphySearchTest.this.message = message;
		}

		@Override
		public void act(String target, String message)
		{
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public void setTopic(String target, String topic)
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

		@Override
		public <Clazz extends Service> List<Clazz> getServicesByClass(Class<Clazz> clazz)
		{
			throw new UnsupportedOperationException("Not supported yet.");
		}
	};

	@Test
	@SuppressWarnings("CallToThreadRun")
	public void TestRedirectResolution() throws InterruptedException
	{
		final String nick = "bob";

		GiphyLinkResolver r = new GiphyLinkResolver(uri, cat, nick);

		r.run();

		assertNotNull(target);
		assertEquals(nick, target);
		assertNotNull(message);
		assertEquals(expected, message);
	}
}
