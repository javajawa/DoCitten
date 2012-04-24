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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import uk.co.harcourtprogramming.internetrelaycats.RelayCat;

/**
 * <p>Recursive URL retriever</p>
 */
public class LinkResolver extends Thread
{

	/**
	 * <p>Thread group for running link resolvers in</p>
	 */
	private final static ThreadGroup THREAD_GROUP = new ThreadGroup("LinkResolvers") {
		@Override
		public void uncaughtException(Thread t, Throwable e)
		{
			LOG.log(Level.SEVERE, "Excpetion in " + t.getName(), e);
		}
	};

	/**
	 * <p>Logger shared with {@link LinkService} and with all other LinkResolver
	 * instances</p>
	 */
	private final static Logger LOG = Logger.getLogger("DoCitten.LinkService");
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
	 * <p>Letters for binary prefixs</p>
	 * <p>kilo, mega, giga, terra, pera, exa, zetta, yotta, hella</p>
	 * <p>Note: hella is my favourite proposal for 10^27. Also, Long.MAX_VALUE
	 * is only about 8 EiB, so it'll be a little while before it gets used</p>
	 */
	private final static String UNIT_PREFIX = "kMGTPEZYH";
	/**
	 * ln(ratio between any two prefixes)
	 */
	private final static double UNIT_SIZE = Math.log(1024);
	/**
	 * <p>Converts a byte count into a 1dp figure of &lt;kMG...&gt;iB
	 * (uses base 1024)</p>
	 * @param bytes the number of bytes
	 * @return formatted value
	 */
	private static String humanReadableByteCount(long bytes) {
		if (bytes < 1024) return bytes + " B";
		int exp = (int) (Math.log(bytes) / UNIT_SIZE);
		return String.format("%.1f %siB", bytes / Math.pow(1024, exp), UNIT_PREFIX.charAt(exp-1));
	}

	/**
	 * The original URI that we are retrieving
	 */
	private final URI baseURI;
	/**
	 * Message that we will be replying to
	 */
	private final RelayCat mess;
	/**
	 * The channel or user to whom we need to reply
	 */
	private final String target;

	/**
	 * Creates a link resolver instance
	 * @param baseURI the link we're following
	 * @param mess the message to replyToAll
	 */
	public LinkResolver(String baseURI, RelayCat mess, String target)
	{
		if (!PROTOCOL.matcher(baseURI).matches())
			this.baseURI = URI.create("http://" + baseURI);
		else
			this.baseURI = URI.create(baseURI);

		this.mess = mess;
		this.target = target;
		setDaemon(true);
	}

	/**
	 * Runs this LinkResolver
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
				LOG.log(Level.FINE,
					String.format("Host %s not found [lookup of %s]",
					curr.getHost(), baseURI.toString()));
				return;
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
					if (conn.getHeaderField("Location") == null) return;
					curr = resolveLocation(curr, conn.getHeaderField("Location"));
					break;

				default:
					return;
			}

			conn.disconnect();

			if (interrupted())
				return;

			if (resolved || ++ hops == MAX_HOPS)
				break;
		}

		if (hops == MAX_HOPS)
		{
			mess.message(target,
				String.format("[%s] (Unresolved after %d hops)", curr.getHost(), MAX_HOPS));
			return;
		}

		fetchData(curr);
	}

	private HttpURLConnection createConnection(URL curr) throws IOException
	{
		HttpURLConnection conn = (HttpURLConnection)curr.openConnection();

		conn.setInstanceFollowRedirects(false);
		conn.setRequestMethod("HEAD");
		conn.setConnectTimeout(TIMEOUT);
		conn.setReadTimeout(TIMEOUT);

		return conn;
	}

	private void fetchData(URL curr)
	{
		try
		{
			HttpURLConnection conn = (HttpURLConnection)curr.openConnection();
			conn.setRequestMethod("GET");
			conn.connect();

			String mime = conn.getContentType();
			if (mime == null) mime = "";
			mime = mime.split(";")[0];

			if (conn.getContentType().matches("(text/.+|.+xhtml.+)"))
			{
				mess.message(target, String.format("[%s] %s", curr.getHost(), getTitle(conn.getInputStream())));
			}
			else
			{
				if (conn.getContentLength() == -1)
				{
					mess.message(target,
						String.format("[%s] %s (size unknown)", curr.getHost(),
						mime));
				}
				else
				{
					mess.message(target,
						String.format("[%s] %s %s", curr.getHost(), mime,
						humanReadableByteCount(conn.getContentLength())));
				}
			}
		}
		catch (IOException ex)
		{
			throw new RuntimeException(ex);
		}
	}

	private String getTitle(InputStream stream) throws IOException
	{
		BufferedReader pageData =
			new BufferedReader(new InputStreamReader(stream));

		String line;
		boolean reading = false;
		String title = "[No Title Set]";

		while (true)
		{
			line = pageData.readLine();
			if (line == null) break;

			if (line.contains("<title>"))
			{
				reading = true;
				line = line.substring(line.indexOf("<title>") + 7);
				title = "";
			}
			if (line.contains("<TITLE>"))
			{
				reading = true;
				line = line.substring(line.indexOf("<TITLE>") + 7);
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

			if (line.contains("</head>") || line.contains("<body>") ||
				line.contains("</HEAD>") || line.contains("<BODY>"))
					break;

			if (reading)
				title += line;
		}

		pageData.close();

		return title.trim().replaceAll("\\s\\s+", " ");
	}

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
