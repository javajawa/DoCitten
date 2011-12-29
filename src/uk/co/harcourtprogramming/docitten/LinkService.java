package uk.co.harcourtprogramming.docitten;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import uk.co.harcourtprogramming.internetrelaycats.MessageService;
import uk.co.harcourtprogramming.internetrelaycats.RelayCat.Message;
import uk.co.harcourtprogramming.internetrelaycats.Service;

public class LinkService extends Service implements MessageService
{
	private final static Pattern uriPattern =
		Pattern.compile("(https?://)?(\\w+\\.)+(com|net|uk|edu|is.gd|bit.ly)(/[\\w.#?%=+-~]*)*", Pattern.CASE_INSENSITIVE);

	public LinkService()
	{
		// Nothing to see here. Move along, citizen!
	}

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
			System.out.println("" + uri(l).size() + '\t' + l);
		}

		List<String> s = uri("Hello there hello.com and example.com/?q=bob are spam sites");
		System.out.println("Hello there hello.com and example.com/?q=bob are spam sites");
		for (String t : s)
		{
			System.out.println(" >> " + t);
		}
	}

	@Override
	public void handle(Message m)
	{
		List<String> uris = uri(m.getMessage());
		for (String uri : uris)
			m.replyToAll("Link Detected: " + uri);
	}

	public static List<String> uri(String message)
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

