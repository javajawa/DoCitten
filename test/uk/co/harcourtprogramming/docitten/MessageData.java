package uk.co.harcourtprogramming.docitten;

public class MessageData
{
	public final String input;
	public final String[] outputs;
	public final boolean action;
	public final String channel;

	MessageData(Boolean action, String channel, String input, String... outputs)
	{
		this.action = action;
		this.channel = channel;
		this.input = input;
		this.outputs = outputs;
	}

}
