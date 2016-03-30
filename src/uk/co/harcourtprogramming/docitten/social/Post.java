package uk.co.harcourtprogramming.docitten.social;

import java.net.URL;
import java.util.Collections;
import java.util.Set;

/**
 *
 * @author Benedict Harcourt <ben.harcourt@harcourtprogramming.co.uk>
 */
public class Post
{
	private final long     id;
	private final URL      url;
	private final String   title;
	private final String   user;
	private final Set<URL> media;

	Post( long id, URL url, String title, String user, Set<URL> media )
	{
		this.id    = id;
		this.url   = url;
		this.title = title;
		this.user  = user;
		this.media = Collections.unmodifiableSet( media );
	}

	public long getId()
	{
		return id;
	}

	public String getTitle()
	{
		return title;
	}

	public String getUser()
	{
		return user;
	}

	public URL getUrl()
	{
		return url;
	}

	@SuppressWarnings( "ReturnOfCollectionOrArrayField" )
	public Set<URL> getMedia()
	{
		// This is already unmoditifable.
		return media;
	}
}
