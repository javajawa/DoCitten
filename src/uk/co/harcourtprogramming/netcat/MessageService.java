package uk.co.harcourtprogramming.netcat;

public abstract class MessageService extends Service
{
	abstract protected void handle(NetCat.Message m);
}

