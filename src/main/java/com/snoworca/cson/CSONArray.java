package com.snoworca.cson;



import java.util.*;


public class CSONArray  extends CSONElement  implements Collection<Object> {
	
	public CSONArray() {
		super(ElementType.Array);
	}



	public CSONArray(Collection<?> objects) {
		super(ElementType.Array);
		mList.addAll(objects);
	}

	private ArrayList<Object> mList = new ArrayList<Object>();
	
	public CSONArray(byte[] buffer) {
		super(ElementType.Array);
		this.mList = ((CSONArray)CSONParser.parse(buffer)).mList;
	}

	public CSONArray(byte[] buffer,int offset, int len) {
		super(ElementType.Array);
		this.mList = ((CSONArray)CSONParser.parse(buffer, offset, len)).mList;
	}
	
	@Override
	public int size() {
		return mList.size();
	}
	
	@Override
	public boolean isEmpty() {
		return mList.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return mList.contains(o);
	}

	@Override
	public Iterator<Object> iterator() {
		return mList.iterator();
	}

	@Override
	public Object[] toArray() {
		return toList().toArray();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object[] toArray(Object[] a) {
		return toList().toArray(a);
	}

	public List<Object> toList() {
		List<Object> results = new ArrayList<Object>(this.mList.size());
		for (Object element : this.mList) {
			if (element == null) {
				results.add(null);
			} else if (element instanceof CSONArray) {
				results.add(((CSONArray) element).toList());
			} else if (element instanceof CSONObject) {
				results.add(((CSONObject) element).toMap());
			} else {
				results.add(element);
			}
		}
		return results;
	}


	protected CSONArray(JSONTokener x) throws CSONException {
		super(ElementType.Array);
		if (x.nextClean() != '[') {
			throw x.syntaxError("A JSONArray text must start with '['");
		}

		char nextChar = x.nextClean();
		if (nextChar == 0) {
			// array is unclosed. No ']' found, instead EOF
			throw x.syntaxError("Expected a ',' or ']'");
		}
		if (nextChar != ']') {
			x.back();
			for (;;) {
				if (x.nextClean() == ',') {
					x.back();
					this.mList.add(null);
				} else {
					x.back();
					this.mList.add(x.nextValue());
				}
				switch (x.nextClean()) {
					case 0:
						// array is unclosed. No ']' found, instead EOF
						throw x.syntaxError("Expected a ',' or ']'");
					case ',':
						nextChar = x.nextClean();
						if (nextChar == 0) {
							// array is unclosed. No ']' found, instead EOF
							throw x.syntaxError("Expected a ',' or ']'");
						}
						if (nextChar == ']') {
							return;
						}
						x.back();
						break;
					case ']':
						return;
					default:
						throw x.syntaxError("Expected a ',' or ']'");
				}
			}
		}
	}


	public CSONArray(String source) throws CSONException {
		this(new JSONTokener(source));
	}
	
	public CSONArray push(Object e) {
		if(e == null) mList.add(new NullValue());
		else if(e instanceof Number) {
			mList.add(e);	
		} else if(e instanceof String) {
			mList.add(e);
		} else if(e instanceof CharSequence) {
			mList.add(e.toString());
		} else if(e instanceof Character || e instanceof Boolean || e instanceof CSONElement || e instanceof byte[] || e instanceof NullValue) {
			mList.add(e);
		}
		return this;
	}
	
	
	

	@Override
	public boolean add(Object e) {
		if(e instanceof Number) {
			return mList.add(e);	
		} else if(e instanceof CharSequence) {
			return mList.add(e.toString());
		} else if(e instanceof Character || e instanceof Boolean || e instanceof CSONArray || e instanceof CSONObject || e instanceof byte[] ) {
			return mList.add(e);
		}
		return false; 
	}
	
	
	
	
	
	
	public Object get(int index) {
		Object obj = mList.get(index);
		if(obj instanceof NullValue) return null;
		return obj;
	}
	
	public Object opt(int index) {
		try {
			Object obj = mList.get(index);
			if(obj instanceof NullValue) return null;
			return obj;
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}
	
	public CSONArray getArray(int index) {
		try {
			return DataConverter.toArray(mList.get(index));
		} catch (IndexOutOfBoundsException e) {
			throw new CSONIndexNotFoundException(e);
		}
		
	}
	
	public CSONArray optArray(int index) {
		try {
			return DataConverter.toArray(mList.get(index));
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
		
	}
	
	public CSONObject getObject(int index) {
		try {
			return DataConverter.toObject(mList.get(index));
		} catch (IndexOutOfBoundsException e) {
			throw new CSONIndexNotFoundException(e);
		}
	}
	
	public CSONObject optObject(int index) {
		try {
			return DataConverter.toObject(mList.get(index));
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}
	
	public int getInteger(int index) {
		try {
			return DataConverter.toInteger(mList.get(index));
		} catch (IndexOutOfBoundsException e) {
			throw new CSONIndexNotFoundException(e);
		}
	}
	
	public int optInteger(int index) {
		return optInteger(index, 0);
	}
	
	public Integer optInteger(int index, int def) {
		try {
			return DataConverter.toInteger(mList.get(index), def);
		} catch (IndexOutOfBoundsException e) {
			return def;
		}
	}
	
	public short getShort(int index) {
		try {			
			return DataConverter.toShort(mList.get(index));
		} catch (IndexOutOfBoundsException e) {
			throw new CSONIndexNotFoundException(e);
		}
	}
	
	public short optShort(int index) {
		return optShort(index, (short)0);
	}
	
	public short optShort(int index, short def) {
		try {			
			return DataConverter.toShort(mList.get(index),def);
		} catch (IndexOutOfBoundsException e) {
			return def;
		}
	}
	
	public float getFloat(int index) {
		try {			
			return DataConverter.toFloat(mList.get(index));
		} catch (IndexOutOfBoundsException e) {
			throw new CSONIndexNotFoundException(e);
		}
	}
	
	public float optFloat(int index, float def) {
		try {			
			return DataConverter.toFloat(mList.get(index));
		} catch (IndexOutOfBoundsException e) {
			return def;
		}
	}
	
	public float optFloat(int index) {
		try {			
			return DataConverter.toFloat(mList.get(index));
		} catch (IndexOutOfBoundsException e) {
			return Float.NaN;
		}
	}
	
	
	public String getString(int index) {
		try {			
			return DataConverter.toString(mList.get(index));
		} catch (IndexOutOfBoundsException e) {
			throw new CSONIndexNotFoundException(e);
		}
	}
	
	public String optString(int index,String def) {
		try {			
			return DataConverter.toString(mList.get(index));
		} catch (IndexOutOfBoundsException e) {
			return def;
		}
	}
	
	public String optString(int index) {
		return optString(index, null);
	}
	
	public byte[] getBuffer(int index) {
		try {			
			byte[] buffer = (byte[]) mList.get(index);
			return buffer;
		} catch (Exception e) {
			throw new CSONIndexNotFoundException(e);
		}
	}
	
	public boolean getBoolean(int index) {
		try {			
			return DataConverter.toBoolean(mList.get(index));
		} catch (IndexOutOfBoundsException e) {
			throw new CSONIndexNotFoundException(e);
		}
	}
	
	public boolean optBoolean(int index, boolean def) {
		try {			
			return DataConverter.toBoolean(mList.get(index), def);
		} catch (IndexOutOfBoundsException e) {
			return def;
		}
	}
	
	public boolean optBoolean(int index) {
		return optBoolean(index, false);
	}

	@Override
	public boolean remove(Object o) {
		return mList.remove(o);
	}

	@Override
	public boolean containsAll(@SuppressWarnings("rawtypes") Collection c) {
		return mList.containsAll(c);
	}

	@Override
	public boolean addAll(@SuppressWarnings("rawtypes") Collection c) {
		return mList.addAll(c);
	}

	@Override
	public boolean removeAll(@SuppressWarnings("rawtypes") Collection c) {
		return mList.removeAll(c);
	}

	@Override
	public boolean retainAll(@SuppressWarnings("rawtypes") Collection c) {
		return mList.retainAll(c);
	}

	@Override
	public void clear() {
		mList.clear();
	}
	
	public CsonArrayEnumerator enumeration() {
		return new CsonArrayEnumerator(this);
	}
	
	
	public static class CsonArrayEnumerator implements Enumeration<Object>  {
		int index = 0;
		boolean hasMore = false; 
		CSONArray array = null;
		
		private CsonArrayEnumerator(CSONArray array) {
			this.array = array;
		}
		
		@Override
		public Object nextElement() {
			if(hasMoreElements()) {
				return array.get(index++);
			}
			return null;
		}
		
		
		public CSONArray getArray() {
			return array.getArray(index++);
		}
		
		public CSONArray optArray() {
			return array.optArray(index++);
		}
		
		public int getInteger() {
			return array.getInteger(index++);
		}
		
		public int optInteger() {
			return array.optInteger(index++);
		}
		
		public short getShort() {
			return array.getShort(index++);
		}
		
		public int optShort() {
			return array.optShort(index++);
		}
		
		public float getFloat() {
			return array.getFloat(index++);
		}
		
		public float optFloat() {
			return array.optFloat(index++);
		}
		
		public String getString() {
			return array.getString(index++);
		}
		
		public String optString() {
			return array.optString(index++);
		}
		
		public boolean getBoolean() {
			return array.getBoolean(index++);
		}
		
		public boolean optBoolean() {
			return array.optBoolean(index++);
		}


		@Override
		public boolean hasMoreElements() {
			return !(index >= array.size());
		}	
	}
	
	public byte[] toByteArray() {
		CSONWriter writer = new CSONWriter();
		write(writer);
		return writer.toByteArray();
	}
	
	protected void write(CSONWriter writer) { 
		writer.openArray();
		for(int i = 0, n = mList.size(); i < n; ++i) {
			Object obj = mList.get(i);
			if(obj == null || obj instanceof NullValue) writer.nullValue();
			else if(obj instanceof CSONArray)  {
				((CSONArray)obj).write(writer);
			}
			else if(obj instanceof CSONObject)  {
				((CSONObject)obj).write(writer);
			} 
			else if(obj instanceof Byte)	writer.add((Byte)obj);
			else if(obj instanceof Short)	writer.add((Short)obj);
			else if(obj instanceof Character) writer.add((Character)obj);
			else if(obj instanceof Integer) writer.add((Integer)obj);
			else if(obj instanceof Float) writer.add((Float)obj);
			else if(obj instanceof Long) writer.add((Long)obj);
			else if(obj instanceof Double) writer.add((Double)obj);
			else if(obj instanceof String) writer.add((String)obj);
			else if(obj instanceof byte[]) writer.add((byte[])obj);
			else if(obj instanceof Boolean) writer.add((Boolean)obj);
		}
		writer.closeArray();
		
	}

	protected void writeJSONString(StringBuilder strBuilder) {
		strBuilder.append("[");
		for(int i = 0, n = mList.size(),np = n - 1 ; i < n; ++i) {
			Object obj = mList.get(i);
			if(obj == null) strBuilder.append("null");
			else if(obj instanceof Number || obj instanceof Boolean) strBuilder.append(obj);
			else if(obj instanceof Character) strBuilder.append('"').append(obj).append('"');
			else if(obj instanceof String) strBuilder.append('"').append(DataConverter.escapeJSONString((String)obj)).append('"');
			else if(obj instanceof byte[]) strBuilder.append('"').append(DataConverter.toString(obj)).append('"');
			else if(obj instanceof CSONArray) ((CSONArray)obj).writeJSONString(strBuilder);
			else if(obj instanceof CSONObject) ((CSONObject)obj).writeJSONString(strBuilder);
			
			if(i != np) strBuilder.append(',');
		}
		strBuilder.append("]");
	}
	
	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		writeJSONString(stringBuilder);
		return stringBuilder.toString();
	}
	

}
