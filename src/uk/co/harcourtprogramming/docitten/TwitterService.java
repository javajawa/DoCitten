/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.harcourtprogramming.docitten;

import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import uk.co.harcourtprogramming.internetrelaycats.ExternalService;
import uk.co.harcourtprogramming.internetrelaycats.InternetRelayCat;
import uk.co.harcourtprogramming.internetrelaycats.RelayCat;

/**
 *
 * @author Benedict Harcourt <ben.harcourt@harcourtprogramming.co.uk>
 */
public class TwitterService extends ExternalService
{
	private final Client hosebirdClient;
	/** Set up your blocking queues: Be sure to size these properly based on expected TPS of your stream */
	private final BlockingQueue<String> msgQueue = new LinkedBlockingQueue<>(100000);

	public TwitterService( InternetRelayCat inst )
	{
		super( inst );

		final StatusesFilterEndpoint hosebirdEndpoint = new StatusesFilterEndpoint();

		// Optional: set up some followings and track terms
		List<Long> followings = new ArrayList<>(2);
		followings.add(1234L);
		followings.add(566788L);

		List<String> terms = new ArrayList<>(2);
		terms.add("twitter");
		terms.add("api");

		hosebirdEndpoint.followings(followings);
		hosebirdEndpoint.trackTerms(terms);
		/** Declare the host you want to connect to, and authentication*/
		Hosts hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);
		Authentication hosebirdAuth = new OAuth1("consumerKey", "consumerSecret", "token", "secret");

		ClientBuilder builder = new ClientBuilder()
		  .name("Twitter Client")
		  .hosts(hosebirdHosts)
		  .authentication(hosebirdAuth)
		  .endpoint(hosebirdEndpoint)
		  .processor(new StringDelimitedProcessor(msgQueue));

		hosebirdClient = builder.build();
	}
	
	@Override
	public void run()
	{
		// Attempts to establish a connection.
		hosebirdClient.connect();
		
		while (!hosebirdClient.isDone())
		{
			String msg;

			try
			{
				msg = msgQueue.poll( 5, TimeUnit.SECONDS );
				getInstance().message( "javajawa", msg );
			}
			catch ( InterruptedException ex )
			{
				hosebirdClient.stop();
				break;
			}
		}
	}

	@Override
	protected void shutdown()
	{
		hosebirdClient.stop();
	}

	@Override
	protected void startup( RelayCat r )
	{
		// Nothing to see here. Move along, citizen!
	}
	
}
