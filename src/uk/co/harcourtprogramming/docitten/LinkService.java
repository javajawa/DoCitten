package uk.co.harcourtprogramming.docitten;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
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
	private final static Pattern protocolPattern =
		Pattern.compile("https?://.+", Pattern.CASE_INSENSITIVE);

	/**
	 * The logger for the Link Service (
	 */
	private final static Logger log = Logger.getLogger("docitten.LinkService");
	/**
	 * <p>Thread group for running link resolvers in</p.
	 * <p>The group is marked as a Daemon group, and so will be ignored by the
	 * JVM when it comes to determining the nubmer of important running threads</p>
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

	private static final String UNIT_PREFIX = "kMGTPE";
	private static String humanReadableByteCount(long bytes) {
		if (bytes < 1024) return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(1024));
		return String.format("%.1f %siB", bytes / Math.pow(1024, exp), UNIT_PREFIX.charAt(exp-1));
	}

	private class LinkResolver implements Runnable
	{
		private final URL baseURI;
		private final Message mess;

		private LinkResolver(String baseURI, Message mess) throws MalformedURLException
		{
			if (!protocolPattern.matcher(baseURI).matches())
			{
				this.baseURI = new URL("http://" + baseURI);
			}
			else
			{
				this.baseURI = new URL(baseURI);
			}
			this.mess = mess;
		}

		@Override
		public void run()
		{
			try
			{
				URL curr = baseURI;
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
					mess.replyToAll(String.format("[%s] %s", curr.getHost(), "<TODO: Retreive Title>"));
				}
				else
				{
					if (conn.getContentLength() == -1)
					{
						mess.replyToAll(String.format("[%s] %s (size unknown)", curr.getHost(), mime));
					}
					else
					{
						mess.replyToAll(String.format("[%s] %s %s",
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
		for (String uri : uris)
			try
			{
				LinkResolver l = new LinkResolver(uri, m);
				if (THREAD_GROUP.isDestroyed())
					throw new IllegalThreadStateException("Thread Group destoryed whilst trying to start search for " + uri);
				Thread t = new Thread(THREAD_GROUP, l, "Link Resolver: " + uri);
				t.setDaemon(true);
				t.start();
			}
			catch (MalformedURLException ex)
			{
				Logger.getLogger(LinkService.class.getName()).
					log(Level.SEVERE, "Malformed URI: " + uri, ex);
			}
	}

	/**
	 * <p>Finds and returns all matched uris in a given string (message)
	 * @param message the message to scan for links
	 * @return the list of links that are found (may by empty, but not null)
	 */
	public static Set<String> uris(String message)
	{
		final Set<String> r = new HashSet<String>();

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
