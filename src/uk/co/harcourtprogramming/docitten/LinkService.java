package uk.co.harcourtprogramming.docitten;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import uk.co.harcourtprogramming.internetrelaycats.Message;
import uk.co.harcourtprogramming.internetrelaycats.MessageService;
import uk.co.harcourtprogramming.internetrelaycats.RelayCat;
import uk.co.harcourtprogramming.internetrelaycats.Service;

/**
 * <p>Link detection/analysis service</p>
 *
 * @author Benedict Harcourt / javajawa
 */
public class LinkService extends Service implements MessageService
{
	/**
	 * <p>Links are detected using this regex</p>
	 * <p>The regex works from detecting a limited subset of the top-level and
	 * selected other domains as a focal point; by looking for subdomains and
	 * protocol before, and path, query and location after, the full uris is
	 * matched.</p>
	 */
	private final static Pattern uriPattern =
		Pattern.compile("(?:^|\\s)\\(?\\[?((?:https?://)?(?:\\w+\\.)*(?:(?:\\w+\\.(com|net|uk|edu))|(?:www|is.gd|bit.ly|t.co))(?::[0-9]+)?(?:/[^\\s\\])]*)?)", Pattern.CASE_INSENSITIVE);
	/**
	 * <p>Links will also be matched if they begin with the http or https
	 * protocols.</p>
	 * <p>This regex is also used in the {@link LinkResolver} to determine is a
	 * protocol is present (otherwise http:// will be prefixed)</p>
	 */
	private final static Pattern protocolPattern =
		Pattern.compile("https?://[^\\s\\])]+", Pattern.CASE_INSENSITIVE);

	/**
	 * <p>Pattern matcher for spotify: uris</p>
	 */
	private final static Pattern spotifyUriPattern =
		Pattern.compile("spotify:[:a-z0-9]+", Pattern.CASE_INSENSITIVE);

	/**
	 * <p>Pattern matcher for giphy search psuedo-uris</p>
	 */
	private final static Pattern giphyUriPattern =
		Pattern.compile("(gif|giphy):([:a-zA-Z0-9]+|\"[^\"]+\")", Pattern.CASE_INSENSITIVE);

	/**
	 * <p>Finds and returns all matched URIs in a given string (message)</p>
	 *
	 * @param message the message to scan for links
	 * @return the list of links that are found (may by empty, but not null)
	 */
	public static Set<String> uris(String message)
	{
		// TreeSet is strongly ordered
		final Set<String> r = new TreeSet<>();

		Matcher m = uriPattern.matcher(message);
		while (m.find())
		{
			r.add(m.group(1));
		}

		m = protocolPattern.matcher(message);
		while (m.find())
		{
			r.add(m.group());
		}

		return r;
	}

	/**
	 * <p>Finds and returns all matched URIs in a given string (message)</p>
	 *
	 * @param message the message to scan for links
	 * @return the list of links that are found (may by empty, but not null)
	 */
	public static Set<String> spotifyUris(String message)
	{
		// TreeSet is strongly ordered
		final Set<String> r = new TreeSet<>();

		Matcher m = spotifyUriPattern.matcher(message);
		while (m.find())
		{
			r.add(m.group());
		}

		return r;
	}

	/**
	 * <p>Finds and returns all matched URIs in a given string (message)</p>
	 *
	 * @param message the message to scan for links
	 * @return the list of links that are found (may by empty, but not null)
	 */
	public static Set<String> giphyUris(String message)
	{
		// TreeSet is strongly ordered
		final Set<String> r = new TreeSet<>();

		Matcher m = giphyUriPattern.matcher(message);
		String matched;

		while (m.find())
		{
			matched = m.group( 2 );
			matched = matched.replaceAll("^\"|\"$", "");

			r.add(matched);
		}

		return r;
	}

	/**
	 * <p>Create a link service instance</p>
	 */
	public LinkService()
	{
		// Nothing to see here. Move along, citizen!
	}

	@Override
	public void handle(Message m)
	{
		for (String uri : uris(m.getMessage()))
		{
			new LinkResolver(uri, m, m.getReplyToAllTarget()).start();
		}
		for (String uri : spotifyUris(m.getMessage()))
		{
			new SpotifyLinkResolver(uri, m, m.getReplyToAllTarget()).start();
		}
		for (String uri : giphyUris(m.getMessage()))
		{
			new GiphyLinkResolver(uri, m, m.getReplyToAllTarget()).start();
		}
	}

	@Override
	protected void startup(RelayCat r)
	{
		List<HelpService> helpServices = r.getServicesByClass(HelpService.class);

		if (!helpServices.isEmpty())
		{
			HelpService.HelpInfo help = new HelpService.HelpInfo(
				"Link Service",
				"The link service scans for links, then reports data about them\r\nFor normal web pages, the title and host (after redirects are resolved) are shown\r\nOther file types get the host, type, and size\r\n \r\nThe following protocols are supported:\r\n  http(s):// - General Web Pages\r\n  spotify: - Information about Spotify media\r\n  giphy: - Search for gifs");
			helpServices.get(0).addHelp("links", help);
		}
	}

	@Override
	public void shutdown()
	{
		// Nothing to see here. Move along, citizen!
	}
}
