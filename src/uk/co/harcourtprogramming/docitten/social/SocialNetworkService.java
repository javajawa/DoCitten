/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.harcourtprogramming.docitten.social;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import uk.co.harcourtprogramming.internetrelaycats.ExternalService;
import uk.co.harcourtprogramming.internetrelaycats.InternetRelayCat;
import uk.co.harcourtprogramming.internetrelaycats.Message;
import uk.co.harcourtprogramming.internetrelaycats.MessageService;
import uk.co.harcourtprogramming.internetrelaycats.RelayCat;

/**
 *
 * @author Benedict Harcourt <ben.harcourt@harcourtprogramming.co.uk>
 */
public class SocialNetworkService extends ExternalService implements MessageService
{
	private final Timer queue = new Timer( "Social Media Lookups", true );
	private final Map<String, ISocialNetwork> networks = new HashMap<>( 4 );
	private final Map<SocialStream, List<SocialStreamTask>> following = new HashMap<>( 16 );

	public SocialNetworkService( InternetRelayCat inst )
	{
		super( inst );
	}

	public synchronized boolean AddNetwork( String name, ISocialNetwork network )
	{
		return networks.putIfAbsent( name, network ) == null;
	}
	
	public synchronized boolean Follow( String target, String network, SearchMethod type, String search, Duration period )
	{
		if ( target == null )
		{
			throw new IllegalArgumentException( "Target can not be null" );
		}
		if ( network == null )
		{
			throw new IllegalArgumentException( "Network can not be null" );
		}
		if ( type == null )
		{
			throw new IllegalArgumentException( "Type can not be null" );
		}
		if ( search == null )
		{
			throw new IllegalArgumentException( "Search can not be null" );
		}
		if ( search.isEmpty() )
		{
			throw new IllegalArgumentException( "Search can not be empty" );
		}
		if ( period  == null )
		{
			throw new IllegalArgumentException( "Period can not be null" );
		}
		
		final ISocialNetwork net = networks.get( network );
		
		if ( net == null )
		{
			throw new IllegalArgumentException( "Network not found" );
		}

		SocialStream toFollow = new SocialStream( net, type, search );
		List<SocialStreamTask> tasks;
		
		if ( following.containsKey( toFollow ) )
		{
			tasks = following.get( toFollow );

			for ( SocialStreamTask task : tasks )
			{
				if ( task.getTarget().equals( target ) )
				{
					return true;
				}
			}
			
			SocialStreamTask task = new SocialStreamTask( target, toFollow );

			tasks.add( task );
			queue.scheduleAtFixedRate( task, 2000, period.toNanos() );
			
			return true;
		}
		else
		{
			tasks = new ArrayList<>( 4 );
			SocialStreamTask task = new SocialStreamTask( target, toFollow );

			tasks.add( task );
			queue.scheduleAtFixedRate( task, 2000, period.toNanos() );
			
			following.put( toFollow, tasks );
			
			return true;
		}
	}
	public synchronized boolean Unfollow( String target, String network, SearchMethod type, String search )
	{
		if ( target == null )
		{
			throw new IllegalArgumentException( "Target can not be null" );
		}
		if ( network == null )
		{
			throw new IllegalArgumentException( "Network can not be null" );
		}
		if ( type == null )
		{
			throw new IllegalArgumentException( "Type can not be null" );
		}
		if ( search == null )
		{
			throw new IllegalArgumentException( "Search can not be null" );
		}
		if ( search.isEmpty() )
		{
			throw new IllegalArgumentException( "Search can not be empty" );
		}

		final ISocialNetwork net = networks.get( network );
		
		if ( net == null )
		{
			throw new IllegalArgumentException( "Network not found" );
		}

		SocialStream toFollow = new SocialStream( net, type, search );

		if ( ! following.containsKey( toFollow ) )
		{
			return false;
		}

		List<SocialStreamTask> tasks = following.get( toFollow );

		for ( SocialStreamTask task : tasks )
		{
			if ( task.getTarget().equals( target ) )
			{
				task.cancel();
				tasks.remove( task );
				return true;
			}
		}
		
		return false;
	}

	@Override
	public void handle( Message m )
	{
		throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void run()
	{
		try
		{
			Thread.sleep( 10000 );
		}
		catch ( InterruptedException ex )
		{
			// Ignore
		}
	}

	@Override
	protected void shutdown()
	{
		queue.cancel();
		queue.purge();
	}

	@Override
	protected void startup( RelayCat r )
	{
		throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
	}

	private class SocialStreamTask extends TimerTask
	{
		private final String target;
		private final SocialStream stream;
		private long latest;
		
		SocialStreamTask( String target, SocialStream stream )
		{
			this.target = target;
			this.stream = stream;
		}
		
		@Override
		public void run()
		{
			final ISocialNetwork network = stream.getConnector();
			final List<Post> posts;
			
			switch ( stream.getType() )
			{
				case SEARCH:
					posts = network.publicSearch( stream.getSearch(), latest );
					break;
					
				case TAG:
					posts = network.publicSearch( stream.getSearch(), latest );
					break;
					
				case USER:
					posts = network.publicSearch( stream.getSearch(), latest );
					break;

				default:
					throw new RuntimeException( "Invalid Enum State" );
			}
			
			if ( posts.isEmpty() )
			{
				return;
			}
			
			latest = posts.get( 0 ).getId();
			
			final RelayCat irc = SocialNetworkService.this.getInstance();
			final StringBuilder mess = new StringBuilder( 256 );
			
			for ( Post p : posts )
			{
				mess.delete( 0, mess.length() )
					.append( p.getUser() )
					.append( " - " );
				
				if ( p.getTitle().length() > 80 )
				{
					mess.append( p.getTitle(), 0, 60 ).append( '…' );
				}
				else
				{
					mess.append( p.getTitle() );
				}

				mess.append( " [" ).append( p.getUrl() ).append( ']' );

				irc.message( target, mess.toString() );
			}
		}
		
		public String getTarget()
		{
			return target;
		}
	}
}
