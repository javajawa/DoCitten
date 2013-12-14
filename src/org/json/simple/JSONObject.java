/*
 * $Id: JSONObject.java,v 1.1 2006/04/15 14:10:48 platform Exp $
 * Created on 2006-4-10
 */
package org.json.simple;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A JSON object. Key value pairs are unordered. JSONObject supports
 * java.util.Map interface.
 *
 * @author FangYidong<fangyidong@yahoo.com.cn>
 */
public class JSONObject extends HashMap<String, Object> implements JSONAware, JSONStreamAware
{

	private static final long serialVersionUID = -503443796854799292L;

	/**
	 * Encode a map into JSON text and write it to out. If this map is also a
	 * JSONAware or JSONStreamAware, JSONAware or JSONStreamAware specific
	 * behaviours will be ignored at this top level.
	 *
	 * @see org.json.simple.JSONValue#writeJSONString(Object, Writer)
	 *
	 * @param map
	 * @param out
	 */
	public static void writeJSONString(Map<String, Object> map, Writer out) throws IOException
	{
		if (map == null)
		{
			out.write("null");
			return;
		}

		boolean first = true;
		Iterator<Map.Entry<String, Object>> iter = map.entrySet().iterator();

		out.write('{');
		while (iter.hasNext())
		{
			if (first)
			{
				first = false;
			}
			else
			{
				out.write(',');
			}
			Map.Entry<String, Object> entry = iter.next();
			out.write('\"');
			out.write(escape(entry.getKey()));
			out.write('\"');
			out.write(':');
			JSONValue.writeJSONString(entry.getValue(), out);
		}
		out.write('}');
	}

	@Override
	public void writeJSONString(Writer out) throws IOException
	{
		writeJSONString(this, out);
	}

	/**
	 * Convert a map to JSON text. The result is a JSON object. If this map is
	 * also a JSONAware, JSONAware specific behaviours will be omitted at this
	 * top level.
	 *
	 * @see org.json.simple.JSONValue#toJSONString(Object)
	 *
	 * @param map
	 * @return JSON text, or "null" if map is null.
	 */
	public static String toJSONString(Map<String, Object> map)
	{
		if (map == null)
		{
			return "null";
		}

		StringBuffer sb = new StringBuffer();
		boolean first = true;
		Iterator<Map.Entry<String, Object>> iter = map.entrySet().iterator();

		sb.append('{');
		while (iter.hasNext())
		{
			if (first)
			{
				first = false;
			}
			else
			{
				sb.append(',');
			}

			Map.Entry<String, Object> entry = iter.next();
			toJSONString(entry.getKey(), entry.getValue(), sb);
		}
		sb.append('}');
		return sb.toString();
	}

	@Override
	public String toJSONString()
	{
		return toJSONString(this);
	}

	private static String toJSONString(String key, Object value, StringBuffer sb)
	{
		sb.append('\"');
		if (key == null)
		{
			sb.append("null");
		}
		else
		{
			JSONValue.escape(key, sb);
		}
		sb.append('\"').append(':');

		sb.append(JSONValue.toJSONString(value));

		return sb.toString();
	}

	@Override
	public String toString()
	{
		return toJSONString();
	}

	public static String toString(String key, Object value)
	{
		StringBuffer sb = new StringBuffer();
		toJSONString(key, value, sb);
		return sb.toString();
	}

	/**
	 * Escape quotes, \, /, \r, \n, \b, \f, \t and other control characters
	 * (U+0000 through U+001F). It's the same as JSONValue.escape() only for
	 * compatibility here.
	 *
	 * @see org.json.simple.JSONValue#escape(String)
	 *
	 * @param s
	 * @return
	 */
	public static String escape(String s)
	{
		return JSONValue.escape(s);
	}

	public String getString(String key)
	{
		final Object o = get(key);
		if (o instanceof String)
		{
			return (String) o;
		}
		return null;
	}

	public Number getNumber(String key)
	{
		final Object o = get(key);
		if (o instanceof Number)
		{
			return (Number) o;
		}
		return null;
	}

	public Boolean getBoolean(String key)
	{
		final Object o = get(key);
		if (o instanceof Boolean)
		{
			return (Boolean) o;
		}
		return null;
	}

	public JSONObject getObject(String key)
	{
		final Object o = get(key);
		if (o instanceof JSONObject)
		{
			return (JSONObject) o;
		}
		return null;
	}

	public JSONArray getArray(String key)
	{
		final Object o = get(key);
		if (o instanceof JSONArray)
		{
			return (JSONArray) o;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public <T> Iterable<T> getArrayIterator(String key)
	{
		final Object o = get(key);
		try
		{
			return (Iterable<T>) o;
		}
		catch (ClassCastException ex)
		{
			return null;
		}
	}
}
