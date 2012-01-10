package uk.co.harcourtprogramming.docitten;

import org.junit.Test;
import static org.junit.Assert.*;
import uk.co.harcourtprogramming.internetrelaycats.RelayCat;

public class TitleFinderTest
{
	private final String uri = "http://www.artima.com/weblogs/viewpost.jsp?thread=142428";
	private final String title = "Java API Design Guidelines";
	private final String site = "www.artima.com";
	private final String expected = String.format("[%s] %s", site, title);

	private String target = null;
	private String message = null;

	@Test
	public void theTest() throws InterruptedException
	{
		final Thread outerThread = Thread.currentThread();
		final String nick = "bob";

		LinkService.LinkResolver r = new LinkService.LinkResolver(uri,
			new RelayCat() {

			@Override
			public void message(String target, String message)
			{
				synchronized (outerThread)
				{
					TitleFinderTest.this.target  = target;
					TitleFinderTest.this.message = message;
					outerThread.notify();
				}
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
		}, nick);

		synchronized(outerThread)
		{
			r.start();
			outerThread.wait();
		}

		assertNotNull(target);
		assertEquals(nick, target);
		assertNotNull(message);
		assertEquals(expected, message);
	}
}
