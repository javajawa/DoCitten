package uk.co.harcourtprogramming.docitten;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.regex.Pattern;
import javax.net.ssl.SSLHandshakeException;
import uk.co.harcourtprogramming.docitten.utility.HtmlEntities;
import uk.co.harcourtprogramming.internetrelaycats.RelayCat;
import uk.co.harcourtprogramming.logging.LogDecorator;

/**
 * <p>Recursive URL retriever</p>
 *
 * @author Benedict Harcourt / javajawa
 */
public class LinkResolver extends Thread
{

	/**
	 * <p>Thread group for running link resolvers in</p>
	 */
	private final static ThreadGroup THREAD_GROUP = new ThreadGroup("LinkResolvers")
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
	 * <p>Regex pattern to test whether a string has an http or https protocol
	 * section</p>
	 * <p>The URI class in Java requires this in order to create an instance</p>
	 */
	private final static Pattern PROTOCOL = Pattern.compile("^https?://.+");
	/**
	 * <p>Max time to wait for any one hop before giving up</p>
	 */
	private final static int TIMEOUT = 2000;
	/**
	 * <p>The maximum number of redirects to follow</p>
	 */
	private final static int MAX_HOPS = 5;
	/**
	 * <p>Letters for binary prefixes</p>
	 * <p>kilo, mega, giga, terra, pera, exa, zetta, yotta, hella</p>
	 * <p>Note: hella is my favourite proposal for 10^27. Also, Long.MAX_VALUE
	 * is only about 8 EiB, so it'll be a little while before it gets used</p>
	 */
	private final static String UNIT_PREFIX = "kMGTPEZYH";
	/**
	 * <p>ln(ratio between any two prefixes)</p>
	 */
	private final static double UNIT_SIZE = Math.log(1024);

	/**
	 * <p>Converts a byte count into a 1dp figure of &lt;kMG...&gt;iB
	 * (uses base 1024)</p>
	 *
	 * @param bytes the number of bytes
	 * @return formatted value
	 */
	private static String humanReadableByteCount(long bytes)
	{
		if (bytes < 1024)
		{
			return bytes + " B";
		}

		int exp = (int)(Math.log(bytes) / UNIT_SIZE);
		return String.format("%.1f %siB", bytes / Math.pow(1024, exp), UNIT_PREFIX.charAt(exp - 1));
	}
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
	public LinkResolver(String baseURI, RelayCat mess, String target)
	{
		super(THREAD_GROUP, "LinkResolver [" + baseURI + ']');
		if (!PROTOCOL.matcher(baseURI).matches())
		{
			this.baseURI = URI.create("http://" + baseURI);
		}
		else
		{
			this.baseURI = URI.create(baseURI);
		}

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

		HttpURLConnection conn;
		boolean resolved = false;
		int hops = 0;

		while (true)
		{
			int statusCode;

			try
			{
				conn = createConnection(curr);
				conn.connect();
				statusCode = conn.getResponseCode();
			}
			catch (UnknownHostException ex)
			{
				LOG.fine("Host {0} not found [lookup of {1}]", curr.getHost(), baseURI.toString());
				return;
			}
			catch (SSLHandshakeException ex)
			{
				final Throwable inner = ex.getCause();

				if (inner instanceof java.security.cert.CertificateException)
				{
					LOG.fine("No cerficiate {0} not found [lookup of {1}]", curr.getHost(), baseURI.toString());
					return;
				}

				if (inner instanceof java.io.EOFException)
				{
					LOG.fine("Error connecting to {0} [lookup of {1}] {2}", curr.getHost(), baseURI.toString(), inner.getMessage());
					return;
				}

				throw new RuntimeException(ex);
			}
			catch (IOException ex)
			{
				throw new RuntimeException(ex);
			}

			switch (statusCode)
			{
				case HttpURLConnection.HTTP_ACCEPTED:
				case HttpURLConnection.HTTP_CREATED:
				case HttpURLConnection.HTTP_NO_CONTENT:
				case HttpURLConnection.HTTP_OK:
				case HttpURLConnection.HTTP_PARTIAL:
				case HttpURLConnection.HTTP_RESET:
				case HttpURLConnection.HTTP_NOT_MODIFIED:
					resolved = true;
					break;

				case HttpURLConnection.HTTP_MOVED_PERM:
				case HttpURLConnection.HTTP_MOVED_TEMP:
				case HttpURLConnection.HTTP_MULT_CHOICE:
				case HttpURLConnection.HTTP_SEE_OTHER:
					if (conn.getHeaderField("Location") == null)
					{
						return;
					}

					curr = resolveLocation(curr, conn.getHeaderField("Location"));
					break;

				default:
					return;
			}

			conn.disconnect();

			if (interrupted())
			{
				return;
			}

			if (resolved || (++hops == MAX_HOPS))
			{
				break;
			}
		}

		if (hops == MAX_HOPS)
		{
			mess.message(target, String.format(
				"[%s] (Unresolved after %d hops)", curr.getHost(), MAX_HOPS
			));
			return;
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
			conn.setInstanceFollowRedirects(false);
			conn.connect();

			String mime = conn.getContentType();
			if (mime == null)
			{
				mime = "";
			}

			mime = mime.split(";")[0];

			if (conn.getContentType().matches("(text/.+|.+xhtml.+)"))
			{
				mess.message(target, String.format("[%s] %s", url.getHost(), getTitle(conn.getInputStream())));
			}
			else
			{
				if (conn.getContentLength() == -1)
				{
					mess.message(target,
						String.format("[%s] %s (size unknown)", url.getHost(), mime));
				}
				else
				{
					mess.message(target, String.format("[%s] %s %s", url.getHost(), mime,
						humanReadableByteCount(conn.getContentLength())));
				}
			}
		}
		catch (IOException ex)
		{
			throw new RuntimeException(ex);
		}
	}

	/**
	 * <p>Searches for a &lt;title&gt; element in a stream</p>
	 * @param stream source data
	 * @return the title, or "[No Title Set]" is none is found
	 * @throws IOException
	 */
	private String getTitle(InputStream stream) throws IOException
	{
		BufferedReader pageData =
			new BufferedReader(new InputStreamReader(stream));

		String line;

		boolean reading = false;
		int titleTagLength = "<title>".length();

		String title = "[No Title Set]";

		while (true)
		{
			line = pageData.readLine();
			if (line == null)
			{
				break;
			}

			if (line.contains("<title>"))
			{
				reading = true;
				line = line.substring(line.indexOf("<title>") + titleTagLength);
				title = "";
			}
			if (line.contains("<TITLE>"))
			{
				reading = true;
				line = line.substring(line.indexOf("<TITLE>") + titleTagLength);
				title = "";
			}

			if (reading && line.contains("</title>"))
			{
				title += line.substring(0, line.indexOf("</title>"));
				break;
			}
			if (reading && line.contains("</TITLE>"))
			{
				title += line.substring(0, line.indexOf("</TITLE>"));
				break;
			}

			if (line.contains("</head>") || line.contains("<body>")
			 || line.contains("</HEAD>") || line.contains("<BODY>"))
			{
				break;
			}

			if (reading)
			{
				title += line;
			}
		}

		pageData.close();

		return HtmlEntities.decode(title.trim().replaceAll("\\s\\s+", " "));
	}

	/**
	 * <p>Wrapper for {@link URI#resolve(java.lang.String) URI.resolve} for use
	 * with {@link URL URL} objects</p>
	 * @param curr base URL
	 * @param location relative or absolute location to resolve
	 * @return the resolved URL
	 * @throws RuntimeException if any URL is malformed
	 */
	private URL resolveLocation(URL curr, String location)
	{
		try
		{
			return URI.create(curr.toExternalForm()).resolve(location).toURL();
		}
		catch (MalformedURLException ex)
		{
			throw new RuntimeException(ex);
		}
	}
}
