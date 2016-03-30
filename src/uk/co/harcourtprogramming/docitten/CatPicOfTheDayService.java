package uk.co.harcourtprogramming.docitten;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import uk.co.harcourtprogramming.internetrelaycats.ExternalService;
import uk.co.harcourtprogramming.internetrelaycats.InternetRelayCat;
import uk.co.harcourtprogramming.internetrelaycats.Message;
import uk.co.harcourtprogramming.internetrelaycats.MessageService;
import uk.co.harcourtprogramming.internetrelaycats.RelayCat;
import uk.co.harcourtprogramming.mewler.MessageTokeniser;

/**
 * <p>Service for create todo list and reminders</p>
 *
 * @author Benedict Harcourt / javajawa
 */
public class CatPicOfTheDayService extends ExternalService implements MessageService
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
		Pattern.compile("https?://[^\\s\\])\"]+", Pattern.CASE_INSENSITIVE);

	/**
	 * <p>Name of the service, for analysing commands</p>
	 */
	private final static String SERVICE_NAME = "catpic";

	private final static String API_KEY = "NTI2MTU";

	private final static String CHANNEL = "#doc";

	/**
	 * <p>List of upcoming CatPics</p>
	 */
	private final Deque<String> queue = new LinkedList<>();

	private final Calendar lastUpdated = Calendar.getInstance( TimeZone.getTimeZone("UTC") );

	/**
	 * <p>Creates a new ReminderService instance</p>
	 *
	 * @param inst the IRC interface to attach to
	 */
	public CatPicOfTheDayService(final InternetRelayCat inst)
	{
		super(inst);

		lastUpdated.setTime( new Date() );
	}

	@Override
	protected void startup(RelayCat r)
	{
		List<HelpService> helpServices = r.getServicesByClass(HelpService.class);

		if (!helpServices.isEmpty())
		{
			HelpService.HelpInfo help = new HelpService.HelpInfo("Cat Pic of the Day Service",
				  "  Cat pics are being supplied to: " + CHANNEL + "\n"
				+ "  \n"
				+ "  add _uri_  Add a pic to the queue\n"
				+ "  next       Change the current CatPic to the next in queue\n"
				+ "  queue      Show the current items in the queue\n"
				+ "  suggest    Suggest a cat pic from the Cat Pics API\n"
			);

			helpServices.get(0).addHelp("catpic", help);
		}

		List<LinkService> linkServices = r.getServicesByClass(LinkService.class);
	}

	@Override
	public void shutdown()
	{
	}

	@Override
	@SuppressWarnings("SleepWhileInLoop")
	public void run()
	{
		final TimeZone tz = TimeZone.getDefault();
		final Calendar now = Calendar.getInstance( tz );
		final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		Date nowDate;

		df.setTimeZone( tz );

		try
		{
			while (true)
			{
				nowDate = new Date();
				now.setTime( nowDate );

				if (
					lastUpdated.get(Calendar.YEAR) != now.get(Calendar.YEAR) ||
					lastUpdated.get(Calendar.DAY_OF_YEAR) != now.get(Calendar.DAY_OF_YEAR)
				)
				{
					String url;

					synchronized( queue )
					{
						url = queue.pollFirst();
					}

					if ( url == null )
					{
						url = getApiCat();
					}

					if ( url != null )
					{
						getInstance().setTopic( CHANNEL, "Cat Pic of the Day (" + df.format( nowDate ) + "): " + url + " | GNU Sir Terry Pratchett, Sir Christopher Lee" );
						lastUpdated.setTime( nowDate );
					}
				}

				synchronized( this )
				{
					this.wait( 300_000 );
				}
			}
		}
		catch (InterruptedException ex)
		{
		}
	}

	@Override
	public void handle(Message m)
	{
		MessageTokeniser tokeniser = new MessageTokeniser(m.getMessage());
		tokeniser.setConsumeWhitespace(true);

		if (!tokeniser.consume(m.getNick() + ':') && m.getChannel() != null)
			return;

		if (!tokeniser.consume(SERVICE_NAME))
			return;

		if ( tokeniser.startsWith( "next" ) )
		{
			lastUpdated.set( Calendar.YEAR, 1971 );
			synchronized( this )
			{
				this.notifyAll();
			}
			return;
		}

		if ( tokeniser.startsWith( "suggest" ) )
		{
			m.replyToAll( getApiCat() );
			return;
		}

		if ( tokeniser.startsWith( "queue" ) )
		{
			m.reply( "Current queue lengtth: " + queue.size() );
			for ( String s : queue )
			{
				m.reply( s );
			}
		}

		if ( tokeniser.startsWith( "remove" ) )
		{
			tokeniser.consume( "remove" );

			synchronized (queue )
			{
				if (queue.remove(tokeniser.toString()))
				{
					m.reply( "java.util.List.remove returned true" );
				}
				else
				{
					m.reply( "java.util.List.remove returned false" );
				}
			}

			return;
		}

		final String message = tokeniser.toString();

		Matcher u = uriPattern.matcher(message);
		Matcher p = protocolPattern.matcher(message);

		synchronized( queue )
		{
			while (u.find())
			{
				if (!queue.contains(u.group(1)) && queue.add(u.group(1)))
				{
					m.reply(u.group(1) + " added");
				}
			}

			while (p.find())
			{
				if (!queue.contains(u.group(0)) && queue.add(p.group(0)))
				{
					m.reply(p.group(0) + " added");
				}
			}
		}
	}

	private String getApiCat()
	{
		final URI uri = URI.create( "http://thecatapi.com/api/images/get?format=src&api_key=" + API_KEY );
		final URL url;
		final HttpURLConnection conn;
		final int statusCode;

		try
		{
			url = uri.toURL();
		}
		catch (MalformedURLException ex)
		{
			log(Level.FINE, "Malformed URL {0}", uri.toString());
			return null;
		}

		try
		{
			conn = (HttpURLConnection)url.openConnection();

			conn.setInstanceFollowRedirects(false);
			conn.setRequestMethod("HEAD");
			conn.setConnectTimeout(3000);
			conn.setReadTimeout(1000);

			conn.connect();
			statusCode = conn.getResponseCode();
		}
		catch (UnknownHostException ex)
		{
			log(Level.FINE, "Host {0} not found [lookup of {1}]", url.getHost(), uri.toString());
			return null;
		}
		catch (IOException ex)
		{
			log(Level.FINE, "IO Error", ex);
			return null;
		}

		switch ( statusCode )
		{
			case HttpURLConnection.HTTP_MOVED_PERM:
			case HttpURLConnection.HTTP_MOVED_TEMP:
			case HttpURLConnection.HTTP_MULT_CHOICE:
			case HttpURLConnection.HTTP_SEE_OTHER:
				return conn.getHeaderField("Location");
		}

		return null;
	}
}
