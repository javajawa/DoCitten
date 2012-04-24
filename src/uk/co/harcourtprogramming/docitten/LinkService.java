package uk.co.harcourtprogramming.docitten;

import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import uk.co.harcourtprogramming.internetrelaycats.MessageService;
import uk.co.harcourtprogramming.internetrelaycats.Message;
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
		Pattern.compile("(?:^|\\s)\\(?\\[?((?:https?://)?(?:\\w+\\.)*(?:(?:\\w+\\.(com|net|uk|edu))|(?:www|is.gd|bit.ly))(?::[0-9]+)?(?:/[^\\s\\])]*)?)", Pattern.CASE_INSENSITIVE);
	/**
	 * Links will also be matched if they begin with the http or https protocols.
	 * This regex is also used in the {@link LinkResolver} to determine is a protocol
	 * is present (otherwise http will be prefixed)
	 */
	private final static Pattern protocolPattern =
		Pattern.compile("https?://[^\\s\\])]+", Pattern.CASE_INSENSITIVE);

	/**
	 * Create a link server
	 */
	public LinkService()
	{
		// Nothing to see here. Move along, citizen!
	}

	@Override
	public void handle(Message m)
	{
		Set<String> uris = uris(m.getMessage());
		LinkResolver l;

		for (String uri : uris)
		{
			l = new LinkResolver(uri, m, m.getReplyToAllTarget());
			l.start();
		}
	}

	/**
	 * <p>Finds and returns all matched uris in a given string (message)
	 * @param message the message to scan for links
	 * @return the list of links that are found (may by empty, but not null)
	 */
	public static Set<String> uris(String message)
	{
		// TreeSet is strongly ordered
		final Set<String> r = new TreeSet<String>();

		Matcher m = uriPattern.matcher(message);
		while (m.find())
			r.add(m.group(1));

		m = protocolPattern.matcher(message);
		while (m.find())
			r.add(m.group());

		return r;
	}

	@Override
	public void shutdown()
	{
		// Nothing to see here. Move along, citizen!
	}
}
