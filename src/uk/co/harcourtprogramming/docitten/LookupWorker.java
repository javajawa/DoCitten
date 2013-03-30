package uk.co.harcourtprogramming.docitten;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import uk.co.harcourtprogramming.internetrelaycats.RelayCat;
import uk.co.harcourtprogramming.logging.LogDecorator;

/**
 * <p>Recursive URL retriever</p>
 *
 * @author Benedict Harcourt / javajawa
 */
public class LookupWorker extends Thread
{

	/**
	 * <p>Thread group for running lookups in</p>
	 */
	private final static ThreadGroup THREAD_GROUP = new ThreadGroup("LookupWorkers")
	{
		@Override
		public void uncaughtException(Thread t, Throwable e)
		{
			LOG.uncaught(t, e);
		}
	};
	/**
	 * <p>Logger shared with {@link LookupService} and with all other LookupWorker
	 * instances</p>
	 */
	private final static LogDecorator LOG = LogDecorator.getLogger("DoCitten.LookupService");

	/**
	 * <p>The Wolfram|Alpha API key to use with Wolfram|Alpha requests</p>
	 */
	private final static String WOLFRAM_KEY = System.getProperty("Lookup.WolframKey");

	private final static DocumentBuilderFactory domParserFactory = DocumentBuilderFactory.newInstance();

	/**
	 * <p>The question that was asked</p>
	 */
	private final String question;
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
	 * @param question The question that has been asked
	 * @param mess IRC connection that the query came from
	 * @param target IRC user/channel that the query came from
	 */
	public LookupWorker(String question, RelayCat mess, String target)
	{
		super(THREAD_GROUP, "LookupWorker [" + question + ']');
		this.question = question;
		this.mess = mess;
		this.target = target;
		setDaemon(true);
	}

	/**
	 * <p>Runs this LookupWorker</p>
	 */
	@Override
	public void run()
	{
		if (this.question.trim().isEmpty())
			return;

		final String uri;
		try
		{
			uri = String.format(
				"http://api.wolframalpha.com/v2/query?format=plaintext&appid=%s&reinterpret=true&input=%s",
				WOLFRAM_KEY,
				URLEncoder.encode(question, "UTF-8")
			);
		}
		catch (UnsupportedEncodingException ex)
		{
			LOG.severe(ex, null);
			return;
		}

		final DocumentBuilder domParser;
		final Document document;
		try
		{
			domParser = domParserFactory.newDocumentBuilder();
		}
		catch (javax.xml.parsers.ParserConfigurationException ex)
		{
			LOG.severe(ex, "Unable to create a document parser");
			return;
		}

		try
		{
			document = domParser.parse(uri);
		}
		catch (IOException|org.xml.sax.SAXException ex)
		{
			LOG.warning(ex, "Error requesting {0}", uri);
			return;
		}

		final NodeList answers = document.getElementsByTagName("plaintext");

		for (int i = 0; i < answers.getLength(); ++i)
		{
			if (i == 3)
				break;
			mess.message(target, answers.item(i).getTextContent());
		}
		mess.message(target, "Via Wolfram|Alpha http://wolframalpha.com");
	}
}

