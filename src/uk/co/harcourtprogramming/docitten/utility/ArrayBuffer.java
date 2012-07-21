package uk.co.harcourtprogramming.docitten.utility;

import java.lang.reflect.Array;

public class ArrayBuffer<T>
{
	private final T[] backing;
	private final int length;
	private int pos = 0;

	@SuppressWarnings("unchecked")
	public ArrayBuffer(int length, T initialValue)
	{
		backing = (T[])Array.newInstance(initialValue.getClass(), length);
		this.length = length;

		for (int i = 0; i < length; ++i)
			backing[i] = initialValue;
	}

	public synchronized T add(T value)
	{
		T displaced = backing[pos];
		backing[pos] = value;
		pos = (pos + 1) % length;
		return displaced;
	}

	public synchronized T get(int offset)
	{
		if (offset < 0 || offset >= length)
			throw new ArrayIndexOutOfBoundsException(offset);

		return backing[(pos + offset) % length];
	}

	public int getLength()
	{
		return length;
	}

}
