package uk.co.harcourtprogramming.docitten;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.co.harcourtprogramming.internetrelaycats.ExternalService;
import uk.co.harcourtprogramming.internetrelaycats.InternetRelayCat;
import uk.co.harcourtprogramming.internetrelaycats.Service;
import uk.co.harcourtprogramming.logging.LogDecorator;
import uk.co.harcourtprogramming.logging.LogFormatter;
import uk.co.harcourtprogramming.mewler.MessageTokeniser;


/**
 * <p>Utility class for main function for DoCitten</p>
 */
public class DoCitten
{

	private final static String[] CONFIGS = {
		"./docittenrc",
		"/etc/docittenrc"
	};
	private final static LogDecorator LOG = LogDecorator.getLogger("InternetRelayCats");

	static
	{
		final Logger ROOT_LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).getParent();
		
		ROOT_LOGGER.getHandlers()[0].setFormatter(new LogFormatter());
		ROOT_LOGGER.setLevel(Level.FINE);
	}

	/**
	 * <p>Runs DoCitten, waiting for 'quit' as a line of stdin before closing</p>
	 * @param args the command line arguments to the program
	 */
	@SuppressWarnings( "UseOfSystemOutOrSystemErr" )
	public static void main(String[] args)
	{
		if (args.length > 1)
		{
			LOG.warning("DoCitten no longer supports specifying host/channel on the command line");
		}

		String configPath = null;
		
		if (args.length == 1)
		{
			configPath = args[0];
		}

		DoCitten bot = new DoCitten();
		bot.run(configPath);
	}
		
	private final List<String> channels = new ArrayList<>(8);
	
	private InternetRelayCat inst;
	
	private String nick;

	private DoCitten()
	{
		// Nothing to see here. Move along, citizen!
	}

	private void run(String configPath)
	{
		File configFile = getConfigFile(configPath);
		BufferedReader in;

		if ( configFile == null )
		{
			LOG.severe( "Unable to find any configuration file" );
			return;
		}

		try
		{
			in = new BufferedReader(new FileReader(configFile));
		}
		catch ( FileNotFoundException ex )
		{
			LOG.severe( ex, "Unable to read configuration file" );
			return;
		}

		if ( ! processCommands( in ) )
		{
			return;
		}

		in = new BufferedReader(new InputStreamReader(System.in));
		
		processCommands( in );

		if ( inst != null )
		{
			inst.shutdown();
		}
	}
	
	private File getConfigFile(String configPath)
	{
		File configFile = null;
		
		if ( configPath != null )
		{
			configFile = new File( configPath );
		
			if ( ! configFile.isFile() )
			{
				LOG.warning( "Specified configuration file {} is not a file", configPath );
			}

			if ( ! configFile.canRead() )
			{
				LOG.warning( "Specified configuration file {} is not readable", configPath );
			}
		}

		if (configFile == null || ! configFile.isFile() || ! configFile.canRead())
		{
			for ( String config : CONFIGS )
			{
				configFile = new File( config );
				
				if ( configFile.isFile() && configFile.canRead() )
				{
					break;
				}
			}
		}
		
		if (configFile == null || ! configFile.isFile() || ! configFile.canRead())
		{
			return null;
		}

		return configFile;
	}

	private boolean processCommands(BufferedReader in)
	{
		MessageTokeniser tokeniser;

		while (true)
		{
			String s;

			try
			{
				s = in.readLine();
			}
			catch ( IOException ex )
			{
				LOG.warning( "Error reading command stream", ex );
				return false;
			}

			// NULL means EOF, so we finished the file sucessfully
			if ( s == null )
			{
				return true;
			}

			tokeniser = new MessageTokeniser(s);

			try
			{
				switch (tokeniser.nextToken())
				{
					case "#":
						break;

					case "quit":
						return false;

					case "log":
						LOG.info( tokeniser.toString() );
						break;

					case "loglevel":
						commandLogLevel( tokeniser );
						break;

					case "connect":
						commandConnect( tokeniser );
						break;

					case "nick":
						commandNick( tokeniser.toString() );
						break;

					case "join":
						commandJoin( tokeniser );
						break;

					case "part":
						commandPart( tokeniser );
						break;

					case "load":
						commandLoad( tokeniser );
						break;
				}
			}
			catch ( Exception ex )
			{
				
			}
		}
	}
	
	private void commandConnect( MessageTokeniser tokeniser )
	{
		if ( inst != null )
		{
			LOG.warning( "Already Connected" );
			return;
		}

		if ( nick == null )
		{
			LOG.severe( "Can not connect - Nick not set" );
			return;
		}

		boolean ssl = false;
		String host = tokeniser.nextToken();
		String port_s;
		int port;

		if ( "ssl".equalsIgnoreCase( host ) )
		{
			ssl = true;
			host = tokeniser.nextToken();
		}

		port_s = tokeniser.nextToken();

		if ( port_s != null )
		{
			try
			{
				port = Integer.valueOf( port_s );
			}
			catch ( NumberFormatException ex )
			{
				LOG.severe( "Unable to parse port number {0}", port_s );
				return;
			}
		}
		else
		{
			port = ssl ? 6667 : 6697;
		}

		LOG.info("Connect to {0}:{1} (SSL={2})", new Object[]{host, port, ssl});

		inst = new InternetRelayCat( nick, host, channels );
		new Thread(inst, "IRC-Thread").start();
	}

	private void commandNick( String nick )
	{
		LOG.info("Setting nick to {0}", nick);

		this.nick = nick;
		
		if ( inst != null )
		{
			// TODO: inst.setNick( nick );
		}
	}
	
	private void commandJoin( MessageTokeniser tokeniser )
	{
		String channel;
					
		while ( ! tokeniser.isEmpty() )
		{
			channel = tokeniser.nextToken();
			
			LOG.info("Joining channel {0}", channel);
			
			channels.add( channel );

			if ( inst != null )
			{
				inst.join( channel );
			}
		}
	}
	
	private void commandPart( MessageTokeniser tokeniser )
	{
		String channel;
					
		while ( ! tokeniser.isEmpty() )
		{
			channel = tokeniser.nextToken();
			
			LOG.info("Parting channel {0}", channel);
			
			channels.remove( channel );

			if ( inst != null )
			{
				inst.leave( channel );
			}
		}
	}

	private void commandLogLevel( MessageTokeniser tokeniser )
	{
		String l = tokeniser.nextToken();
		Level level;

		try
		{
			level = Level.parse( l );
		}
		catch ( IllegalArgumentException ex )
		{
			LOG.warning("Unknown log level {0}", l );
			return;
		}
		
		// If not logger specified
		if ( tokeniser.isEmpty() )
		{
			// Operate at the highest logger level
			Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).getParent().setLevel( level );
		}
		else
		{
			while ( ! tokeniser.isEmpty() )
			{
				Logger.getLogger( tokeniser.nextToken() ).setLevel( level );
			}
		}
	}

	@SuppressWarnings( "unchecked" )
	private void commandLoad( MessageTokeniser tokeniser )
	{
		if ( inst == null )
		{
			LOG.warning("Not yet connected - Can not add services");
			return;
		}
		
		final Class<Service> serviceClass = getServiceClass( tokeniser.nextToken() );

		if ( serviceClass == null )
		{
			return;
		}
		
		LinkedList<Object> params = new LinkedList<>();
		LinkedList<Class<? extends Object>> types = new LinkedList<>();
		
		while ( ! tokeniser.isEmpty() )
		{
			params.add( tokeniser.nextToken() );
			types.add( String.class );
		}

		if ( ExternalService.class.isAssignableFrom( serviceClass ) )
		{
			params.addFirst( inst );
			types.addFirst( inst.getClass() );
		}
		
		final Constructor<Service> constructor;

		try
		{
			constructor = serviceClass.getConstructor( types.toArray( new Class<?>[ types.size() ] ) );
		}
		catch ( NoSuchMethodException ex )
		{
			LOG.warning("No constructor for {0} with {1} parameters", new Object[]{serviceClass.getName(), params.size()});
			return;
		}
		
		final Service service;
		
		try
		{
			if ( params.isEmpty() )
			{
				service = constructor.newInstance();
			}
			else
			{
				service = constructor.newInstance( params.toArray() );
			}
		}
		catch ( InstantiationException|IllegalAccessException|IllegalArgumentException|InvocationTargetException ex )
		{
			LOG.severe( ex, "Error attempting to construct {0}", serviceClass.getName());
			return;
		}
		
		inst.addService( service );
	}
	
	@SuppressWarnings( "unchecked" )
	private Class<Service> getServiceClass( String className )
	{
		Class<? extends Object> clazz;
		
		if ( className == null )
		{
			LOG.warning( "No class speciifed to load" );
			return null;
		}
		
		try
		{
			clazz = Class.forName( className );
		}
		catch ( ClassNotFoundException ignore )
		{
			try
			{
				clazz = Class.forName( "uk.co.harcourtprogramming.docitten." + className );
			}
			catch ( ClassNotFoundException ex )
			{
				LOG.severe("{0} not found", className);
				return null;
			}
		}

		if ( ! Service.class.isAssignableFrom( clazz ) )
		{
			LOG.severe("{0} is not a Service", className );
			return null;
		}
		
		return (Class<Service>)clazz;
	}
}

