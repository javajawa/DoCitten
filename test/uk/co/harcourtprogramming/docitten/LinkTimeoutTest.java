package uk.co.harcourtprogramming.docitten;

import java.net.SocketTimeoutException;
import org.junit.Test;
import uk.co.harcourtprogramming.docitten.LinkService.LinkResolver;

public class LinkTimeoutTest
{
	@Test(timeout=4000,expected=SocketTimeoutException.class)
	@SuppressWarnings("CallToThreadRun")
	public void linkTimeoutTest() throws Throwable
	{
		LinkResolver linkResolver = new LinkService.LinkResolver("http://example.com:8080/", null, null);
		try
		{
			linkResolver.run();
		}
		catch (RuntimeException ex)
		{
			throw ex.getCause();
		}
	}
}
