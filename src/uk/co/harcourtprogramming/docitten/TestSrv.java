package uk.co.harcourtprogramming.docitten;

import java.util.logging.Level;
import uk.co.harcourtprogramming.internetrelaycats.MessageService;
import uk.co.harcourtprogramming.internetrelaycats.RelayCat.Message;

public class TestSrv extends MessageService
{
	public TestSrv()
	{
		// Nothing to see here. Move along, citizen!
		log(Level.INFO, "TestSrv Starting up");
	}

	@Override
	public void handle(Message m)
	{
		log(Level.INFO, m.getSender() + '\t' + m.getChannel() + '\n' + m.getMessage().substring(0,Math.min(20,m.getMessage().length())));
	}

	@Override
	public void shutdown()
	{
		// Nothing to see here. Move along, citizen!
		log(Level.INFO, "TestSrv shutting down");
	}
}

