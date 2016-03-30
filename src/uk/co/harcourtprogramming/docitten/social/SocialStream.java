package uk.co.harcourtprogramming.docitten.social;

import java.util.Objects;

/**
 *
 * @author Benedict Harcourt <ben.harcourt@harcourtprogramming.co.uk>
 */
public class SocialStream implements Comparable<SocialStream>
{
	private final ISocialNetwork connector;
	private final SearchMethod type;
	private final String search;

	public SocialStream( ISocialNetwork connector, SearchMethod type, String search )
	{
		this.connector = connector; 
		this.type   = type;
		this.search = search;
	}

	@Override
	public int compareTo( SocialStream o )
	{
		if ( ! connector.equals( o.getConnector() ) )
		{
			return connector.toString().compareTo( o.getConnector().toString() );
		}

		int compare = type.compareTo( o.getType() );

		if ( compare != 0 )
		{
			return compare;
		}
		
		return search.compareTo( o.getSearch() );
	}

	@Override
	public boolean equals( Object obj )
	{
		if ( obj instanceof SocialStream )
		{
			SocialStream o = (SocialStream)obj;

			return connector.equals( o.getConnector() ) &&
					type.equals( o.getType() ) &&
					search.equals( o.getSearch() );
		}
		
		return false;
	}

	@Override
	public int hashCode()
	{
		int hash = 7;
		hash = 89 * hash + Objects.hashCode( this.connector );
		hash = 89 * hash + Objects.hashCode( this.type );
		hash = 89 * hash + Objects.hashCode( this.search );
		return hash;
	}

	public String getSearch()
	{
		return search;
	}

	SearchMethod getType()
	{
		return type;
	}

	ISocialNetwork getConnector()
	{
		return connector;
	}
}
