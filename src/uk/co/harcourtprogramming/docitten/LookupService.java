package uk.co.harcourtprogramming.docitten;

import java.util.List;
import uk.co.harcourtprogramming.internetrelaycats.Message;
import uk.co.harcourtprogramming.internetrelaycats.MessageService;
import uk.co.harcourtprogramming.internetrelaycats.RelayCat;
import uk.co.harcourtprogramming.internetrelaycats.Service;
import uk.co.harcourtprogramming.mewler.MessageTokeniser;

/**
 * <p>Encyclopedic Look-up Service</p>
 *
 * @author Benedict Harcourt / javajawa
 */
public class LookupService extends Service implements MessageService
{
	/**
	 * <p>Create a look-up service instance</p>
	 */
	public LookupService()
	{
		// Nothing to see here. Move along, citizen!
	}

	@Override
	public void handle(Message m)
	{
		MessageTokeniser t = new MessageTokeniser(m.getMessage());
		t.setConsumeWhitespace(true);

		// Check that the service is actually being addressed in some way
		if (!t.consume(m.getNick() + ':') && m.getChannel() != null)
			return;

		if (!t.startsWith("lookup") && !t.startsWith("what is") && !t.startsWith("what are") && !t.startsWith("how do i"))
			return;

		t.consume("how do i");
		t.consume("what is");
		t.consume("what are");
		t.consume("lookup");

		final String question = t.nextToken('?');

		new LookupWorker(question, m, m.getReplyToAllTarget()).start();
	}

	@Override
	protected void startup(RelayCat r)
	{
		List<HelpService> helpServices = r.getServicesByClass(HelpService.class);

		if (!helpServices.isEmpty())
		{
			HelpService.HelpInfo help = new HelpService.HelpInfo(
				"Lookup Service",
				"The LookUp service allows you to look up words and ask questions using DoCitten.");
			helpServices.get(0).addHelp("lookup", help);
		}
	}

	@Override
	public void shutdown()
	{
		// Nothing to see here. Move along, citizen!
	}
}

