package uk.co.harcourtprogramming.docitten;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 *
 */
@RunWith(Parameterized.class)
public class LinkServiceTest
{
	public static class LinkData
	{
		/**
		 *
		 */
		public final String inputText;
		/**
		 * The set of links that should be found
		 */
		public final Set<String> links;

		/**
		 * Create a link data instance
		 * @param inputText
		 * @param links
		 */
		public LinkData(String inputText,
			String... links)
		{
			this.inputText = inputText;
			// TreeSet will force natural ordering
			this.links = new TreeSet<String>(Arrays.asList(links));
		}

	}

	/**
	 * @return The parameter data for a paramterised test
	 */
	@Parameterized.Parameters
	public static Collection<LinkData[]> data()
	{
		return Arrays.asList(new LinkData[][]{
			new LinkData[]{new LinkData("http://example.com", "http://example.com")},
			new LinkData[]{new LinkData("bob")},
			new LinkData[]{new LinkData("http://t.co", "http://t.co")},
			new LinkData[]{new LinkData("hello com")},
			new LinkData[]{new LinkData("is.gd", "is.gd")},
			new LinkData[]{new LinkData("www/~bh308", "www/~bh308")},
			new LinkData[]{new LinkData("http://www.youtube.com/watch?v=2jzugX2NMnk", "http://www.youtube.com/watch?v=2jzugX2NMnk")},
			new LinkData[]{new LinkData("mewww?")},
			new LinkData[]{new LinkData("www.google.com", "www.google.com")},
			new LinkData[]{new LinkData("example.com/?q=bob", "example.com/?q=bob")},
			new LinkData[]{new LinkData("google.com or bing.com", "google.com", "bing.com")},
			new LinkData[]{new LinkData("https://github.com", "https://github.com")},
			new LinkData[]{new LinkData("  https://github.com", "https://github.com")},
			new LinkData[]{new LinkData("[https://github.com]", "https://github.com")},
			new LinkData[]{new LinkData("  github.com", "github.com")},
			new LinkData[]{new LinkData("[github.com]", "github.com")}
		});
	}

	/**
	 * Parameter data for this test instance
	 */
	private final LinkData ld;
	/**
	 * Create a LinkServiceTest with a given parameter
	 * @param ld the link data for this test
	 */
	public LinkServiceTest(LinkData ld)
	{
		this.ld = ld;
	}

	/**
	 * Test of uris method, of class LinkService.
	 */
	@Test
	public void testUris()
	{
		Set<String> result = LinkService.uris(ld.inputText);

		System.out.println(ld.inputText + " >> " + Arrays.deepToString(result.toArray()));
		assertEquals(ld.links.size(), result.size());
		assertArrayEquals(ld.links.toArray(), result.toArray());
	}
}
