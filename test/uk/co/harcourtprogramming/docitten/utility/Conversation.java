package uk.co.harcourtprogramming.docitten.utility;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * <p>
 *	The Conversation class represents a simple state machine
 *  for use in testing Service features which are stateful.
 * </p>
 * <p>
 *  Conversations are made up of multiple 'Line' which are
 *  either sent or received to the IRC connection in the order
 *  supplied, checking that the state is correctly followed.
 * </p>
 */
public class Conversation implements Iterable<Conversation.Line>
{
	public static final int SEND = 0;
	public static final int RECIEVE = 1;
	public static final int WAIT = 2;

	/**
	 * <p>The name of this conversation</p>
	 */
	private final String name;
	/**
	 * <p>The set of lines that are to be run through in the state machine</p>
	 */
	public final List<Conversation.Line> links = new ArrayList<>(16);

	public Conversation(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public Conversation send(String source, String target, String line)
	{
		this.links.add(new Line(SEND, source, target, line));

		return this;
	}

	public Conversation recieve(String source, String target, String line)
	{
		this.links.add(new Line(RECIEVE, source, target, line));

		return this;
	}

	public Conversation wait(int millis)
	{
		this.links.add(new Line(WAIT, null, null, Integer.toString(millis)));

		return this;
	}

	@Override
	public Iterator<Line> iterator()
	{
		return links.iterator();
	}

	public class Line
	{
		private final int type;
		private final String source;
		private final String target;
		private final String data;

		public Line( int type, String source, String target, String data )
		{
			this.type   = type;
			this.source = source;
			this.target = target;
			this.data   = data;
		}

		/**
		 * @return the type
		 */
		public int getType()
		{
			return type;
		}

		/**
		 * @return the source
		 */
		public String getSource()
		{
			return source;
		}

		/**
		 * @return the target
		 */
		public String getTarget()
		{
			return target;
		}

		/**
		 * @return the data
		 */
		public String getData()
		{
			return data;
		}
	}
}
