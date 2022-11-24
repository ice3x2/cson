package com.snoworca.cson;



import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;

public class CSONObject extends CSONElement {

	private LinkedHashMap<String, Object> mDataMap = new LinkedHashMap<>();

	public CSONObject(byte[] buffer) {
		super(ElementType.Object);
		CSONObject csonObject = (CSONObject)CSONParser.parse(buffer);
		this.mDataMap = csonObject.mDataMap;
	}

	public CSONObject(byte[] buffer, int offset, int length) {
		super(ElementType.Object);
		CSONObject csonObject = (CSONObject)CSONParser.parse(buffer, offset, length);
		this.mDataMap = csonObject.mDataMap;
	}



	protected Set<Entry<String, Object>> entrySet() {
		return this.mDataMap.entrySet();
	}

	public Map<String, Object> toMap() {
		Map<String, Object> results = new HashMap<String, Object>();
		for (Entry<String, Object> entry : this.entrySet()) {
			Object value;
			if (entry.getValue() == null) {
				value = null;
			} else if (entry.getValue() instanceof CSONObject) {
				value = ((CSONObject) entry.getValue()).toMap();
			} else if (entry.getValue() instanceof CSONArray) {
				value = ((CSONArray) entry.getValue()).toList();
			} else {
				value = entry.getValue();
			}
			results.put(entry.getKey(), value);
		}
		return results;
	}

	public CSONObject(String json) {
		this(new JSONTokener(json));


	}

	public CSONObject() {
		super(ElementType.Object);
	}

	public CSONObject put(String key, Object value) {
		if(value == null) {
			mDataMap.put(key, new NullValue());
			return this;
		}
		else if(value instanceof Number) {
			mDataMap.put(key, value);
		} else if(value instanceof String) {
			mDataMap.put(key, value);
		} else if(value instanceof CharSequence) {
			mDataMap.put(key, value);
		} else if(value instanceof Character || value instanceof Boolean || value instanceof CSONElement || value instanceof byte[] || value instanceof NullValue) {
			mDataMap.put(key, value);
		}
		return this;
	}


	public String optString(String key) {
		Object obj = mDataMap.get(key);
		return DataConverter.toString(obj);
	}

	public String optString(String key, String def) {
		Object obj = mDataMap.get(key);
		if(obj == null) return def;
		return DataConverter.toString(obj);
	}

	public Set<String> keySet() {
		return this.mDataMap.keySet();
	}

	public String getString(String key) {
		Object obj = mDataMap.get(key);
		if(obj == null) throw new CSONIndexNotFoundException();
		return DataConverter.toString(obj);
	}

	public Object opt(String key) {
		Object obj = mDataMap.get(key);
		if(obj instanceof NullValue) return null;
		return obj;
	}

	public Object get(String i) {
		Object obj =  mDataMap.get(i);
		if(obj instanceof NullValue) return null;
		else if(obj == null) throw new CSONIndexNotFoundException();
		return obj;

	}

	public Object opt(String key, Object def) {
		Object result = mDataMap.get(key);
		if(result instanceof NullValue) return null;
		else if(result == null) return def;
		return result;
	}

	public int optInteger(String key, int def) {
		Object obj = mDataMap.get(key);
		if(obj == null) return def;
		return DataConverter.toInteger(obj,def);
	}

	public int optInteger(String key) {
		return optInteger(key, 0);
	}

	public long optLong(String key, long def) {
		Object obj = mDataMap.get(key);
		if(obj == null) return def;
		return DataConverter.toLong(obj,def);
	}

	public boolean isEmpty() {
		return mDataMap.isEmpty();
	}

	public long optLong(String key) {
		return optLong(key, 0);

	}

	public int getInt(String key) {
		return getInteger(key);
	}

	public int optInt(String key) {
		return optInteger(key);
	}

	public int optInt(String key,int def) {
		return optInteger(key, def);
	}

	public int getInteger(String key) {
		Object obj = mDataMap.get(key);
		if(obj == null) throw new CSONIndexNotFoundException();
		return DataConverter.toInteger(obj);
	}

	public float optFloat(String key) {
		return optFloat(key, Float.NaN);
	}

	public float optFloat(String key, float def) {
		Object obj = mDataMap.get(key);
		if(obj == null) return def;
		return DataConverter.toFloat(obj);
	}

	public float getFloat(String key) {
		Object obj = mDataMap.get(key);
		if(obj == null) throw new CSONIndexNotFoundException();
		return DataConverter.toFloat(obj);
	}

	public boolean optBoolean(String key) {
		return optBoolean(key, false);
	}

	public boolean optBoolean(String key, boolean def) {
		Object obj = mDataMap.get(key);
		if(obj  == null) return def;
		return DataConverter.toBoolean(obj);
	}

	public boolean getBoolean(String key) {
		Object obj = mDataMap.get(key);
		if(obj == null) throw new CSONIndexNotFoundException();
		return DataConverter.toBoolean(obj);
	}

	protected CSONObject(JSONTokener x) throws CSONException {
		super(ElementType.Object);
		char c;
		String key;

		if (x.nextClean() != '{') {
			throw x.syntaxError("A JSONObject text must begin with '{'");
		}
		for (;;) {
			char prev = x.getPrevious();
			c = x.nextClean();
			switch (c) {
				case 0:
					throw x.syntaxError("A JSONObject text must end with '}'");
				case '}':
					return;
				case '{':
				case '[':
					if(prev=='{') {
						throw x.syntaxError("A JSON Object can not directly nest another JSON Object or JSON Array.");
					}
					// fall through
				default:
					x.back();
					key = x.nextValue().toString();
			}

			// The key is followed by ':'.

			c = x.nextClean();
			if (c != ':') {
				throw x.syntaxError("Expected a ':' after a key");
			}

			// Use syntaxError(..) to include error location

			if (key != null) {
				// Check if key exists
				if (this.opt(key) != null) {
					// key already exists
					throw x.syntaxError("Duplicate key \"" + key + "\"");
				}
				// Only add value if non-null
				Object value = x.nextValue();
				if (value!=null) {
					this.put(key, value);
				}
			}

			// Pairs are separated by ','.

			switch (x.nextClean()) {
				case ';':
				case ',':
					if (x.nextClean() == '}') {
						return;
					}
					x.back();
					break;
				case '}':
					return;
				default:
					throw x.syntaxError("Expected a ',' or '}'");
			}
		}
	}

	public long getLong(String key) {
		Object obj = mDataMap.get(key);
		if(obj == null) throw new CSONIndexNotFoundException();
		return DataConverter.toLong(obj);
	}

	public double getDouble(String key) {
		Object obj = mDataMap.get(key);
		if(obj == null) throw new CSONIndexNotFoundException();
		return DataConverter.toDouble(obj);
	}

	public char getChar(String key) {
		Object obj = mDataMap.get(key);
		if(obj == null) throw new CSONIndexNotFoundException();
		return DataConverter.toChar(obj);
	}


	public CSONArray optArray(String key) {
		Object obj = mDataMap.get(key);
		if(obj instanceof CSONArray) {
			return (CSONArray)obj;
		}
		return null;
	}


	public CSONArray getArray(String key) {
		Object obj = mDataMap.get(key);
		if(obj instanceof CSONArray) {
			return (CSONArray)obj;
		}
		throw new CSONIndexNotFoundException();
	}

	public CSONObject optObject(String key) {
		Object obj = mDataMap.get(key);
		if(obj instanceof CSONObject) {
			return (CSONObject)obj;
		}
		return null;
	}

	public CSONObject getObject(String key) {
		Object obj = mDataMap.get(key);
		if(obj instanceof CSONObject) {
			return (CSONObject)obj;
		}
		throw new CSONIndexNotFoundException();
	}

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		writeJSONString(stringBuilder);
		return stringBuilder.toString();
	}

	public byte[] toBytes() {
		CSONWriter writer = new CSONWriter();
		write(writer);
		return writer.toByteArray();
	}

	protected void write(CSONWriter writer) {
		Iterator<Entry<String, Object>> iter = mDataMap.entrySet().iterator();
		writer.openObject();
		while(iter.hasNext()) {
			Entry<String, Object> entry = iter.next();
			String key = entry.getKey();
			Object obj = entry.getValue();
			if(obj == null || obj instanceof NullValue) writer.key(key).nullValue();
			else if(obj instanceof CSONArray)  {
				writer.key(key);
				((CSONArray)obj).write(writer);
			}
			else if(obj instanceof CSONObject)  {
				writer.key(key);
				((CSONObject)obj).write(writer);
			}
			else if(obj instanceof Byte)	writer.key(key).value((Byte)obj);
			else if(obj instanceof Short)	writer.key(key).value((Short)obj);
			else if(obj instanceof Character) writer.key(key).value((Character)obj);
			else if(obj instanceof Integer) writer.key(key).value((Integer)obj);
			else if(obj instanceof Float) writer.key(key).value((Float)obj);
			else if(obj instanceof Long) writer.key(key).value((Long)obj);
			else if(obj instanceof Double) writer.key(key).value((Double)obj);
			else if(obj instanceof String) writer.key(key).value((String)obj);
			else if(obj instanceof Boolean) writer.key(key).value((Boolean)obj);
			else if(obj instanceof BigDecimal) writer.key(key).value(((BigDecimal)obj));
			else if(obj instanceof byte[]) writer.key(key).value((byte[])obj);
		}
		writer.closeObject();
	}

	protected void writeJSONString(StringBuilder strBuilder) {

		strBuilder.append("{");

		Iterator<Entry<String, Object>> iter = mDataMap.entrySet().iterator();
		while(iter.hasNext()) {
			Entry<String, Object> entry = iter.next();
			String key = entry.getKey();
			Object obj = entry.getValue();
			strBuilder.append('"').append(key).append("\":");
			if(obj == null || obj instanceof NullValue) strBuilder.append("null");
			else if(obj instanceof Number || obj instanceof Boolean) strBuilder.append(obj);
			else if(obj instanceof Character) strBuilder.append('"').append(obj).append('"');
			else if(obj instanceof String) strBuilder.append('"').append(DataConverter.escapeJSONString((String)obj)).append('"');
			else if(obj instanceof byte[]) strBuilder.append('"').append(DataConverter.toString(obj)).append('"');
			else if(obj instanceof CSONArray) ((CSONArray)obj).writeJSONString(strBuilder);
			else if(obj instanceof CSONObject) ((CSONObject)obj).writeJSONString(strBuilder);

			if(iter.hasNext()) strBuilder.append(',');

		}
		strBuilder.append("}");
	}



}
