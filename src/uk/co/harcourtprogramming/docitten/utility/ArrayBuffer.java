package uk.co.harcourtprogramming.docitten.utility;

import java.lang.reflect.Array;

/**
 * <p>FIFO of typed objects with a fix length backed by an array</p>
 * <p>Elements are displaced when the buffer overflows</p>
 *
 * @author Benedict Harcourt / javajawa
 * @param <T> content type of this buffer
 */
public class ArrayBuffer<T>
{

	/**
	 * <p>Backing array for the buffer</p>
	 */
	private final T[] backing;
	/**
	 * <p>Length of the internal buffer</p>
	 */
	private final int length;
	/**
	 * <p>The current 'oldest' element in the buffer</p>
	 */
	private int pos = 0;

	/**
	 * <p>Create a new ArrayBuffer</p>
	 * @param length the length of the buffer
	 * @param initialValue the value to initialise all elements of the buffer to
	 */
	@SuppressWarnings("unchecked")
	public ArrayBuffer(int length, T initialValue)
	{
		backing = (T[])Array.newInstance(initialValue.getClass(), length);
		this.length = length;

		for (int i = 0; i < length; ++i)
			backing[i] = initialValue;
	}

	/**
	 * <p>Puts a new item into the tail of the buffer</p>
	 * @param value the value to put into the tail
	 * @return the item that was displaced from the head
	 */
	public synchronized T add(T value)
	{
		T displaced = backing[pos];
		backing[pos] = value;
		pos = (pos + 1) % length;
		return displaced;
	}

	/**
	 * <p>Gets the
	 * @param offset
	 * @return the element in this position
	 * @throws ArrayIndexOutOfBoundsException if the offset is less than zero
	 * or greater-than-or-equal to the {@link #length}
	 */
	public synchronized T get(int offset)
	{
		if (offset < 0 || offset >= length)
			throw new ArrayIndexOutOfBoundsException(offset);

		return backing[(pos + offset) % length];
	}

	/**
	 * <p>Gets the length of the buffer</p>
	 * @return the length of the buffer
	 */
	public int getLength()
	{
		return length;
	}
}
