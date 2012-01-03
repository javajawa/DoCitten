package uk.co.harcourtprogramming.docitten;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Calendar;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import uk.co.harcourtprogramming.internetrelaycats.MessageService;
import uk.co.harcourtprogramming.internetrelaycats.Message;
import uk.co.harcourtprogramming.internetrelaycats.RelayCat;
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
		Pattern.compile("(https?://)?(\\w+\\.)*((\\w+\\.(com|net|uk|edu))|(www|is.gd|bit.ly))(/[^\\s]*)*", Pattern.CASE_INSENSITIVE);
	/**
	 * Links will also be matched if they begin with the http or https protocols.
	 * This regex is also used in the {@link LinkResolver} to determine is a protocol
	 * is present (otherwise http will be prefixed)
	 */
	private final static Pattern protocolPattern =
		Pattern.compile("https?://[^\\s]+", Pattern.CASE_INSENSITIVE);

	/**
	 * The logger for the Link Service (
	 */
	private final static Logger log = Logger.getLogger("docitten.LinkService");
	/**
	 * <p>Thread group for running link resolvers in</p.
	 * <p>The group is marked as a Daemon group, and so will be ignored by the
	 * JVM when it comes to determining the number of important running threads</p>
	 */
	private final static ThreadGroup THREAD_GROUP = new ThreadGroup("LinkResolvers") {
		@Override
		public void uncaughtException(Thread t, Throwable e)
		{
			log.log(Level.SEVERE, "Excpetion in " + t.getName(), e);
		}
	};

	static
	{
		Handler h = new ConsoleHandler();
		h.setFormatter(new Formatter()
		{
			@Override
			public String format(LogRecord l)
			{
				Calendar time = Calendar.getInstance();
				time.setTimeInMillis(l.getMillis());

				String mess = String.format("[%2$tR %1$s] %3$s\n",
					l.getLevel().getLocalizedName(), time, formatMessage(l));

				if (l.getThrown() != null)
				{
					Throwable t =  l.getThrown();
					mess += t.getMessage() + '\n';
					for (StackTraceElement ste : t.getStackTrace())
					{
						mess += '\t' + ste.toString() + '\n';
					}
				}
				return mess;
			}
		});
		log.addHandler(h);
		log.setUseParentHandlers(false);
		THREAD_GROUP.setDaemon(false);
	}

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
	 * Converts a byte count into a 1dp figure of <kMG...>iB (uses base 1024)
	 * @param bytes the number of bytes
	 * @return formatted value
	 */
	private static String humanReadableByteCount(long bytes) {
		if (bytes < 1024) return bytes + " B";
		int exp = (int) (Math.log(bytes) / UNIT_SIZE);
		return String.format("%.1f %siB", bytes / Math.pow(1024, exp), UNIT_PREFIX.charAt(exp-1));
	}

	/**
	 * <p>Recursive URL retriever</p>
	 */
	public static class LinkResolver implements Runnable
	{
		/**
		 * The original URI that we are retrieving
		 */
		private final URI baseURI;
		/**
		 * Message that we will be replying to
		 */
		private final RelayCat mess;
		private final String target;

		/**
		 * Creates a link resolver instance
		 * @param baseURI the link we're following
		 * @param mess the message to replyToAll
		 */
		public LinkResolver(String baseURI, RelayCat mess, String target)
		{
			if (!protocolPattern.matcher(baseURI).matches())
			{
				this.baseURI = URI.create("http://" + baseURI);
			}
			else
			{
				this.baseURI = URI.create(baseURI);
			}
			this.mess = mess;
			this.target = target;
		}

		/**
		 * Runs this LinkResolver
		 */
		@Override
		public void run()
		{
			try
			{
				URL curr = baseURI.toURL();
				HttpURLConnection conn;
				boolean resolved = false;

				while (true)
				{
					conn = (HttpURLConnection)curr.openConnection();
					conn.setInstanceFollowRedirects(false);
					conn.setRequestMethod("HEAD");
					conn.connect();

					switch (conn.getResponseCode())
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

						case HttpURLConnection.HTTP_BAD_GATEWAY:
						case HttpURLConnection.HTTP_BAD_METHOD:
						case HttpURLConnection.HTTP_BAD_REQUEST:
						case HttpURLConnection.HTTP_CLIENT_TIMEOUT:
						case HttpURLConnection.HTTP_CONFLICT:
						case HttpURLConnection.HTTP_ENTITY_TOO_LARGE:
						case HttpURLConnection.HTTP_FORBIDDEN:
						case HttpURLConnection.HTTP_GATEWAY_TIMEOUT:
						case HttpURLConnection.HTTP_GONE:
						case HttpURLConnection.HTTP_INTERNAL_ERROR:
						case HttpURLConnection.HTTP_LENGTH_REQUIRED:
						case HttpURLConnection.HTTP_NOT_ACCEPTABLE:
						case HttpURLConnection.HTTP_NOT_AUTHORITATIVE:
						case HttpURLConnection.HTTP_NOT_FOUND:
						case HttpURLConnection.HTTP_NOT_IMPLEMENTED:
						case HttpURLConnection.HTTP_PAYMENT_REQUIRED:
						case HttpURLConnection.HTTP_PRECON_FAILED:
						case HttpURLConnection.HTTP_PROXY_AUTH:
						case HttpURLConnection.HTTP_REQ_TOO_LONG:
						case HttpURLConnection.HTTP_UNAUTHORIZED:
						case HttpURLConnection.HTTP_UNAVAILABLE:
						case HttpURLConnection.HTTP_UNSUPPORTED_TYPE:
						case HttpURLConnection.HTTP_USE_PROXY:
						case HttpURLConnection.HTTP_VERSION:
							return;

						case HttpURLConnection.HTTP_MOVED_PERM:
						case HttpURLConnection.HTTP_MOVED_TEMP:
						case HttpURLConnection.HTTP_MULT_CHOICE:
						case HttpURLConnection.HTTP_SEE_OTHER:
							if (conn.getHeaderField("Location") == null) return;
							curr = URI.create(curr.toExternalForm()).resolve(
								conn.getHeaderField("location")).toURL();
							break;

						default:
							log.log(Level.WARNING, "Unknown HTTP Status code returned: {0}", conn.getResponseCode());
							return;
					}
					conn.disconnect();
					if (resolved) break;
				}

				conn = (HttpURLConnection)curr.openConnection();
				conn.setRequestMethod("GET");
				conn.connect();

				String mime = conn.getContentType();
				if (mime == null) mime = "";
				mime = mime.split(";")[0];

				if (conn.getContentType().matches("(text/.+|.+xhtml.+)")) // A HTML/XHTML file (most likely)
				{
					mess.message(target, String.format("[%s] %s", curr.getHost(), getTitle(conn)));
				}
				else
				{
					if (conn.getContentLength() == -1)
					{
						mess.message(target, String.format("[%s] %s (size unknown)", curr.getHost(), mime));
					}
					else
					{
						mess.message(target, String.format("[%s] %s %s",
							curr.getHost(),
							mime,
							humanReadableByteCount(conn.getContentLength())
						));
					}
				}
			}
			catch (Throwable ex)
			{
				throw new RuntimeException(ex);
			}
		}

		private String getTitle(HttpURLConnection conn) throws IOException
		{
			BufferedReader pageData = new BufferedReader(new InputStreamReader(
				conn.getInputStream()));

			String line;
			boolean reading = false;
			String title = "[No Title Set]";
			while (true)
			{
				line = pageData.readLine();
				if (line == null)
					break;

				if (line.contains("<title>"))
				{
					reading = true;
					line = line.substring(line.indexOf("<title>") + 7);
					title = "";
				}

				if (reading && line.contains("</title>"))
				{
					title += line.substring(0, line.indexOf("</title>"));
					break;
				}

				if (line.contains("</head>")  || line.contains("<body>"))
					break;

				if (reading)
					title += line;
			}

			pageData.close();
			return title;
		}
	}

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
		LinkResolver l; Thread t;

		for (String uri : uris)
		{
			l = new LinkResolver(uri, m, m.getReplyToAllTarget());
			t = new Thread(THREAD_GROUP, l, "Link Resolver: " + uri);
			t.setDaemon(true);
			t.start();
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
			r.add(m.group());

		m = protocolPattern.matcher(message);
		while (m.find())
			r.add(m.group());

		return r;
	}

	@Override
	public void shutdown()
	{
		// TODO: clean up threads
	}
}
