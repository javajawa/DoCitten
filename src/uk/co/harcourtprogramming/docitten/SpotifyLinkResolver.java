package uk.co.harcourtprogramming.docitten;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import uk.co.harcourtprogramming.internetrelaycats.RelayCat;
import uk.co.harcourtprogramming.logging.LogDecorator;

/**
 * <p>Recursive URL retriever</p>
 *
 * @author Benedict Harcourt / javajawa
 */
public class SpotifyLinkResolver extends Thread
{
	/**
	 * <p>Thread group for running link resolvers in</p>
	 */
	private final static ThreadGroup THREAD_GROUP = new ThreadGroup("SpotifyResolvers")
	{
		@Override
		public void uncaughtException(Thread t, Throwable e)
		{
			LOG.uncaught(t, e);
		}
	};
	/**
	 * <p>Logger shared with {@link LinkService} and with all other LinkResolver
	 * instances</p>
	 */
	private final static LogDecorator LOG = LogDecorator.getLogger("DoCitten.LinkServier");
	/**
	 * <p>Max time to wait for any one hop before giving up</p>
	 */
	private final static int TIMEOUT = 2000;

	private final static JSONParser parser = new JSONParser();

	/**
	 * <p>The original URI that we are retrieving</p>
	 */
	private final URI baseURI;
	/**
	 * <p>IRC connection that the query came from</p>
	 */
	private final RelayCat mess;
	/**
	 * <p>IRC user/channel that the query came from</p>
	 */
	private final String target;

	/**
	 * <p>Creates a link resolver instance, targeted at a specified web address,
	 * which will attempt to send information to a IRC end point via a RelayCat
	 * instance</p>
	 *
	 * @param baseURI the link we're following
	 * @param mess IRC connection that the query came from
	 * @param target IRC user/channel that the query came from
	 */
	public SpotifyLinkResolver(String baseURI, RelayCat mess, String target)
	{
		super(THREAD_GROUP, "LinkResolver [" + baseURI + ']');

		this.baseURI = URI.create("http://ws.spotify.com/lookup/1/?uri=" + baseURI);
		this.mess = mess;
		this.target = target;
		setDaemon(true);
	}

	/**
	 * <p>Runs this LinkResolver</p>
	 */
	@Override
	public void run()
	{
		URL curr;
		try
		{
			curr = baseURI.toURL();
		}
		catch (MalformedURLException ex)
		{
			throw new RuntimeException(ex);
		}
		fetchData(curr);
	}

	/**
	 * <p>Creates an HttpURLConnection to a URL</p>
	 * <p>The connection is set up to:</p>
	 * <ul>
	 *	<li>Not follow redirects (prevents loops)</li>
	 *  <li>Perform only a head request</li>
	 *  <li>Timeout after {@link #TIMEOUT}ms on both connect and read</li>
	 * </ul>
	 * @param url the target URL
	 * @return an ready, but unsent, connection to the URL
	 * @throws IOException
	 */
	private HttpURLConnection createConnection(URL url) throws IOException
	{
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();

		conn.setInstanceFollowRedirects(false);
		conn.setRequestMethod("HEAD");
		conn.setConnectTimeout(TIMEOUT);
		conn.setReadTimeout(TIMEOUT);

		return conn;
	}

	/**
	 * <p>Fetches some simple meta-data about a URL</p>
	 * <p>The resource is accessed with a GET request, not following re-directs.
	 * The Content-type is examined; (x)html-like files are searched for a
	 * {@link #getTitle(java.io.InputStream) title element}. For other types,
	 * the mime type and seize are sent in lieu of a title.</p>
	 * @param url the resource to get meta-data for
	 * @throws RuntimeException on any IO error (caught in {@link #THREAD_GROUP
	 * the thread group})
	 */
	private void fetchData(URL url)
	{
		try
		{
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");
			conn.setInstanceFollowRedirects(false);
			conn.connect();

			final Reader r = new InputStreamReader(conn.getInputStream());
			final JSONObject spotify;
			final JSONObject info;
			final JSONObject data;
			final String type;

			try
			{
				synchronized (parser)
				{
					spotify = (JSONObject)parser.parse(r);
				}
			}
			catch (ParseException ex)
			{
				LOG.warning(ex, "Can not parse spotify response for {0}", baseURI);
				return;
			}

			info = spotify.getObject("info");

			if (info == null)
			{
				LOG.warning("No info block on Spotify JSON response");
				return;
			}

			type = info.getString("type");
			data = spotify.getObject(type);

			if (data == null)
			{
				LOG.warning("No data block on Spotify JSON response");
				return;
			}

			StringBuilder response = new StringBuilder(128);
			Iterable<JSONObject> it;
			JSONObject inner;
			switch (type)
			{
				case "track":
					response.append(data.get("name"));
					it = data.getArrayIterator("artists");
					for (JSONObject artist : it)
					{
						response.append(" - ").append(artist.get("name"));
					}
					inner = data.getObject("album");
					if (inner != null)
					{
						response
							.append(" - ")
							.append(inner.get("name"))
							.append(" (")
							.append(inner.get("released"))
							.append(')');
					}
			}

			mess.message(target, response.toString());

			r.close();
		}
		catch (IOException ex)
		{
			throw new RuntimeException(ex);
		}
	}
}
