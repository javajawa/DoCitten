package uk.co.harcourtprogramming.docitten;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.*;

/**
 * <p>Tests the parsing of messages to find web links</p>
 */
@RunWith(Parameterized.class)
public class LinkFinderTest
{

	/**
	 * <p>Storage class for the test data</p>
	 */
	public static class LinkData
	{

		/**
		 * <p>The emulated input line</p>
		 */
		public final String inputText;
		/**
		 * <p>The set of links that should be found</p>
		 */
		public final Set<String> links;

		/**
		 * Create a link data instance
		 *
		 * @param inputText emulated input line
		 * @param links list of links that should be found
		 */
		LinkData(String inputText, String... links)
		{
			this.inputText = inputText;
			// TreeSet will force natural ordering
			this.links = new TreeSet<String>(Arrays.asList(links));
		}
	}

	/**
	 * @return The parameter data for a parameterised test
	 */
	@Parameterized.Parameters
	public static Collection<Object[]> data()
	{
		return Arrays.asList(new Object[][]{
			new Object[]{new LinkData("http://example.com", "http://example.com")},
			new Object[]{new LinkData("bob")},
			new Object[]{new LinkData("http://t.co", "http://t.co")},
			new Object[]{new LinkData("hello com")},
			new Object[]{new LinkData("is.gd", "is.gd")},
			new Object[]{new LinkData("www/~bh308", "www/~bh308")},
			new Object[]{new LinkData("http://www.youtube.com/watch?v=2jzugX2NMnk", "http://www.youtube.com/watch?v=2jzugX2NMnk")},
			new Object[]{new LinkData("mewww?")},
			new Object[]{new LinkData("www.google.com", "www.google.com")},
			new Object[]{new LinkData("example.com/?q=bob", "example.com/?q=bob")},
			new Object[]{new LinkData("google.com or bing.com", "google.com", "bing.com")},
			new Object[]{new LinkData("https://github.com", "https://github.com")},
			new Object[]{new LinkData("  https://github.com", "https://github.com")},
			new Object[]{new LinkData("[https://github.com]", "https://github.com")},
			new Object[]{new LinkData("  github.com", "github.com")},
			new Object[]{new LinkData("[github.com]", "github.com")},
			new Object[]{new LinkData("example.com:8080", "example.com:8080")},
			new Object[]{new LinkData("http://www.google.com", "http://www.google.com")},
			new Object[]{new LinkData("gif:cat", "cat")},
			new Object[]{new LinkData("giphy:cat", "cat")},
			new Object[]{new LinkData("gif:\"blood sword\"", "blood sword")},
			new Object[]{new LinkData("gif:\"blood\" \"sword\"", "blood")},
			new Object[]{new LinkData("gif:???")},
			new Object[]{new LinkData("gif:\"???\"", "???")},
			new Object[]{new LinkData("gif:google.com", "google")}, // This is a bit of a weird one...
			new Object[]{new LinkData("google.com www", "google.com", "www")},
			new Object[]{new LinkData("gif: cat")},
			new Object[]{new LinkData("gif:cat giphy:dog", "cat", "dog")}
		});
	}
	/**
	 * <p>Parameter data for this test instance</p>
	 */
	private final LinkData ld;

	/**
	 * Create a LinkServiceTest with a given parameter
	 *
	 * @param ld the link data for this test
	 */
	public LinkFinderTest(LinkData ld)
	{
		this.ld = ld;
	}

	/**
	 * <p>Test of uris method, of class LinkService.</p>
	 */
	@Test
	@SuppressWarnings("UseOfSystemOutOrSystemErr") // Additional assertion failure data
	public void testUris()
	{
		Set<String> result = new TreeSet<>();
		result.addAll( LinkService.uris(ld.inputText) );
		result.addAll( LinkService.spotifyUris(ld.inputText) );
		result.addAll( LinkService.giphyUris(ld.inputText) );

		try
		{
			assertEquals("Number of urls for " + ld.inputText + " not correct", ld.links.size(), result.size());
			assertArrayEquals("Mismatch for urls for " + ld.inputText, ld.links.toArray(), result.toArray());
		}
		catch (AssertionError ex)
		{
			System.out.println(ld.inputText + " >> " + Arrays.deepToString(result.toArray()));
			System.out.println(ex.toString());
			throw ex;
		}
	}
}
