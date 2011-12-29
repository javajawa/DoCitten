package uk.co.harcourtprogramming.docitten;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import uk.co.harcourtprogramming.internetrelaycats.MessageService;
import uk.co.harcourtprogramming.internetrelaycats.BasicRelayCat.Message;
import uk.co.harcourtprogramming.internetrelaycats.Service;

/**
 * Link detection/analysis service
 */
public class LinkService extends Service implements MessageService
{
	/**
	 * <p>Links are detected using this regex</p>
	 * <p>The regex works from detecting a limited subset of the top-level and
	 * selected other domains as a focal point; by looking for subdomains and
	 * protocol before, and path, query and location after, the full uris is
	 * matched.
	 */
	private final static Pattern uriPattern =
		Pattern.compile("(https?://)?(\\w+\\.)+(com|net|uk|edu|is.gd|bit.ly)(/[\\w.#?%=+-~]*)*", Pattern.CASE_INSENSITIVE);

	/**
	 * Create a link server
	 */
	public LinkService()
	{
		// Nothing to see here. Move along, citizen!
	}

	/**
	 * Code for testing the class/regex from the command line
	 * @param args the cli arguments
	 * @todo Migrate this to a JUnit test
	 */
	public static void main(String[] args)
	{
		String[] tests = new String[] {
			"http://example.com",
			"http://example.com/",
			"example.com",
			"example.com/",
			"hello com",
			"bob",
			"example.com/index.html",
			"example.com/?q=bob",
			"http://www.youtube.com/watch?v=2jzugX2NMnk"
		};

		for (String l : tests)
		{
			System.out.println("" + uris(l).size() + '\t' + l);
		}

		List<String> s = uris("Hello there hello.com and example.com/?q=bob are spam sites");
		System.out.println("Hello there hello.com and example.com/?q=bob are spam sites");
		for (String t : s)
		{
			System.out.println(" >> " + t);
		}
	}

	@Override
	public void handle(Message m)
	{
		List<String> uris = uris(m.getMessage());
		for (String uri : uris)
			m.replyToAll("Link Detected: " + uri);
	}

	/**
	 * <p>Finds and returns all matched uris in a given string (message)
	 * @param message the message to scan for links
	 * @return the list of links that are found (may by empty, but not null)
	 */
	public static List<String> uris(String message)
	{
		Matcher m = uriPattern.matcher(message);
		final List<String> r = new ArrayList<String>();

		while (m.find())
		{
			r.add(m.group());
		}
		return r;
	}

	@Override
	public void shutdown()
	{
		// Nothing to see here. Move along, citizen!
	}
}

