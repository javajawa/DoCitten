package uk.co.harcourtprogramming.docitten.social;

import java.util.List;

/**
 *
 * @author Benedict Harcourt <ben.harcourt@harcourtprogramming.co.uk>
 */
public interface ISocialNetwork
{
	public List<Post> publicSearch( String term );
	public List<Post> publicSearch( String term, long startFrom );
	
	public List<Post> fetchTag( String term );
	public List<Post> fetchTag( String term, long startFrom );

	public List<Post> fetchStream( String term );
	public List<Post> fetchStream( String term, long startFrom );
}
