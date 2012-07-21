package uk.co.harcourtprogramming.docitten;

import uk.co.harcourtprogramming.internetrelaycats.ExternalService;
import uk.co.harcourtprogramming.internetrelaycats.InternetRelayCat;
import uk.co.harcourtprogramming.internetrelaycats.Message;
import uk.co.harcourtprogramming.internetrelaycats.MessageService;
import uk.co.harcourtprogramming.internetrelaycats.RelayCat;

/**
 *
 * @author Benedict
 */
public class AdvancedKittenService extends ExternalService implements MessageService
{
	private double hunger;
	private double happiness;
	private double tiredness;
	private boolean asleep;

	public AdvancedKittenService(InternetRelayCat inst)
	{
		super(inst);
	}

	@Override
	protected void startup(RelayCat r)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	protected void shutdown()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void run()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void handle(Message m)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
