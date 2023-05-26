package com.snoworca.cson;



import java.io.Reader;
import java.math.BigDecimal;
import java.util.*;


public class CSONArray  extends CSONElement  implements Collection<Object>, Cloneable {

	private ArrayList<Object> list = new ArrayList<>();
	private ArrayList<CommentObject> commentObjectList = null;

	private JSONOptions jsonOptions = JSONOptions.json();


	public CSONArray() {
		super(ElementType.Array);
	}

	public CSONArray(int size) {
		super(ElementType.Array);
		this.list.ensureCapacity(size);
	}

	public CSONArray(Collection<?> objects) {
		super(ElementType.Array);
		list.addAll(objects);
	}

	public CSONArray(byte[] buffer) {
		super(ElementType.Array);
		this.list = ((CSONArray)CSONParser.parse(buffer)).list;
	}

	public CSONArray(byte[] buffer,int offset, int len) {
		super(ElementType.Array);
		this.list = ((CSONArray)CSONParser.parse(buffer, offset, len)).list;
	}
	
	@Override
	public int size() {
		return list.size();
	}
	
	@Override
	public boolean isEmpty() {
		return list.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return list.contains(o);
	}

	@Override
	public Iterator<Object> iterator() {
		return list.iterator();
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
		List<Object> results = new ArrayList<Object>(this.list.size());
		for (Object element : this.list) {
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


	public String getComment(int index) {
		CommentObject commentObject = getCommentObject(index);
		if(commentObject == null) return null;
		return commentObject.getComment();
	}


	public CommentObject getCommentObject(int index) {
		if(commentObjectList == null) return null;
		if(index >= commentObjectList.size()) return null;
		return commentObjectList.get(index);
	}

	public CommentObject getCommentObject(int index, boolean createIfNotExists) {
		if(commentObjectList == null) {
			if(!createIfNotExists) return null;
			commentObjectList = new ArrayList<>();
		}
		if(index >= commentObjectList.size()) {
			if(!createIfNotExists) return null;
			ensureCapacityOfCommentObjects();
		}
		return commentObjectList.get(index);
	}


	public void setCommentObject(int index, CommentObject commentObject) {
		if(commentObjectList.size() <= list.size()) {
			ensureCapacityOfCommentObjects();
		}
		if(index >= list.size()) {
			throw new IndexOutOfBoundsException("index: " + index + ", size: " + list.size());
		}
		commentObjectList.set(index, commentObject);
	}


	private void ensureCapacityOfCommentObjects() {
		commentObjectList.ensureCapacity(list.size());
		for (int i = commentObjectList.size(), n = list.size(); i < n; i++) {
			commentObjectList.add(null);
		}
	}



	protected CSONArray(JSONTokener x) throws CSONException {
		super(ElementType.Array);
		this.jsonOptions = x.getJsonOption();
		new JSONParser(x).parseArray(this);
	}

	protected void addAtJSONParsing(Object value) {
		if(value instanceof String && CSONElement.isBase64String((String)value)) {
			value = CSONElement.base64StringToByteArray((String)value);
		}
		list.add(value);
	}


	protected void addCommentObjects(CommentObject commentObject) {
		if(commentObjectList == null) {
			commentObjectList = new ArrayList<>();
		}
		commentObjectList.add(commentObject);

	}

	protected void setCommentObjectToLastIndex(CommentObject commentObjects) {
		commentObjectList.set(commentObjectList.size()-1, commentObjects);
	}

	public CSONArray(Reader stringSource) throws CSONException {
		this(new JSONTokener(stringSource,JSONOptions.json5()));
	}

	public CSONArray(Reader stringSource, JSONOptions options) throws CSONException {
		this(new JSONTokener(stringSource,options));
	}


	public CSONArray(String source) throws CSONException {
		this(new JSONTokener(source,JSONOptions.json5()));
	}

	public CSONArray(String source, JSONOptions options) throws CSONException {
		this(new JSONTokener(source,options));
	}
	
	public CSONArray put(Object e) {
		if(!add(e)) {
			// TODO 넣을 수 없는 타입 에러.
		}
		return this;
	}

	public CSONArray set(int index, Object e) {
		int size = list.size();
		Object value = convert(e);
		if(value == null) {
			// TODO 넣을 수 없는 타입 에러.
		}
		if(index >= size) {
			for(int i = size; i < index; i++) {
				add(null);
			}
			list.add(value);
		} else {
			list.set(index, value);
		}
		return this;
	}


	private Object convert(Object e) {
		if(e == null || e instanceof  NullValue) list.add(new NullValue());
		if(e instanceof Number) {
			return e;
		} else if(e instanceof CharSequence) {
			return e.toString();
		} else if(e instanceof CSONArray) {
			if(e == this) e = ((CSONArray)e).clone();
			return e;
		} else if(e instanceof Character || e instanceof Boolean || e instanceof CSONObject || e instanceof byte[] ) {
			return e;
		}
		return null;
	}


	@Override
	public boolean add(Object e) {
		Object value = convert(e);
		if(value == null) return false;
		list.add(value);
		return true;
	}
	
	
	
	
	
	
	public Object get(int index) {
		Object obj = list.get(index);
		if(obj instanceof NullValue) return null;
		return obj;
	}
	
	public Object opt(int index) {
		try {
			Object obj = list.get(index);
			if(obj instanceof NullValue) return null;
			return obj;
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}
	
	public CSONArray getArray(int index) {
		try {
			return DataConverter.toArray(list.get(index));
		} catch (IndexOutOfBoundsException e) {
			throw new CSONIndexNotFoundException(e);
		}
		
	}

	public CSONArray optArray(int index, CSONArray def) {
		try {
			return DataConverter.toArray(list.get(index));
		} catch (IndexOutOfBoundsException e) {
			return def;
		}
	}

	public CSONArray optArray(int index) {
		return optArray(index, null);
	}

	public CSONArray optArrayWrap(int index) {
		try {
			Object object = list.get(index);
			if (object instanceof CSONArray) {
				return (CSONArray) object;
			} else if (object == null) {
				return new CSONArray();
			}
			return new CSONArray().put(object);
		} catch (IndexOutOfBoundsException e) {
			return new CSONArray();
		}

	}
	
	public CSONObject getObject(int index) {
		try {
			return DataConverter.toObject(list.get(index));
		} catch (IndexOutOfBoundsException e) {
			throw new CSONIndexNotFoundException(e);
		}
	}
	
	public CSONObject optObject(int index) {
		try {
			return DataConverter.toObject(list.get(index));
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}




	@Deprecated
	public int getInteger(int index) {
		return getInt(index);
	}

	public int getInt(int index) {
		try {
			return DataConverter.toInteger(list.get(index));
		} catch (IndexOutOfBoundsException e) {
			throw new CSONIndexNotFoundException(e);
		}
	}

	@Deprecated
	public int optInteger(int index) {
		return optInt(index);
	}

	@Deprecated
	public int optInteger(int index, int def) {
		return optInt(index, def);
	}



	public int optInt(int index) {
		return optInt(index, 0);
	}

	public int optInt(int index, int def) {
		try {
			return DataConverter.toInteger(list.get(index), def);
		} catch (IndexOutOfBoundsException e) {
			return def;
		}
	}

	public long getLong(int index) {
		try {
			return DataConverter.toLong(list.get(index));
		} catch (IndexOutOfBoundsException e) {
			throw new CSONIndexNotFoundException(e);
		}
	}

	public long optLong(int index) {
		return optLong(index, 0);
	}

	public long optLong(int index, long def) {
		try {
			return DataConverter.toLong(list.get(index), def);
		} catch (IndexOutOfBoundsException e) {
			return def;
		}
	}
	
	public short getShort(int index) {
		try {			
			return DataConverter.toShort(list.get(index));
		} catch (IndexOutOfBoundsException e) {
			throw new CSONIndexNotFoundException(e);
		}
	}
	
	public short optShort(int index) {
		return optShort(index, (short)0);
	}
	
	public short optShort(int index, short def) {
		try {			
			return DataConverter.toShort(list.get(index),def);
		} catch (IndexOutOfBoundsException e) {
			return def;
		}
	}

	public char getChar(int index) {
		try {
			return DataConverter.toChar(list.get(index));
		} catch (IndexOutOfBoundsException e) {
			throw new CSONIndexNotFoundException(e);
		}
	}

	public char optChar(int index) {
		return optChar(index, '\0');
	}

	public char optChar(int index, char def) {
		try {
			return DataConverter.toChar(list.get(index),def);
		} catch (IndexOutOfBoundsException e) {
			return def;
		}
	}


	public short getByte(int index) {
		try {
			return DataConverter.toByte(list.get(index));
		} catch (IndexOutOfBoundsException e) {
			throw new CSONIndexNotFoundException(e);
		}
	}

	public byte optByte(int index, byte def) {
		try {
			return DataConverter.toByte(list.get(index), def);
		} catch (IndexOutOfBoundsException e) {
			return def;
		}
	}

	public byte optByte(int index) {
		try {
			return DataConverter.toByte(list.get(index));
		} catch (IndexOutOfBoundsException e) {
			return 0;
		}
	}


	public double getDouble(int index) {
		try {
			return DataConverter.toDouble(list.get(index));
		} catch (IndexOutOfBoundsException e) {
			throw new CSONIndexNotFoundException(e);
		}
	}

	public double optDouble(int index) {
		return optDouble(index, 0);
	}

	public double optDouble(int index, double def) {
		try {
			return DataConverter.toDouble(list.get(index));
		} catch (IndexOutOfBoundsException e) {
			return Double.NaN;
		}
	}
	
	public float getFloat(int index) {
		try {			
			return DataConverter.toFloat(list.get(index));
		} catch (IndexOutOfBoundsException e) {
			throw new CSONIndexNotFoundException(e);
		}
	}
	
	public float optFloat(int index, float def) {
		try {			
			return DataConverter.toFloat(list.get(index));
		} catch (IndexOutOfBoundsException e) {
			return def;
		}
	}
	
	public float optFloat(int index) {
		try {			
			return DataConverter.toFloat(list.get(index));
		} catch (IndexOutOfBoundsException e) {
			return Float.NaN;
		}
	}
	
	
	public String getString(int index) {
		try {			
			return DataConverter.toString(list.get(index));
		} catch (IndexOutOfBoundsException e) {
			throw new CSONIndexNotFoundException(e);
		}
	}
	
	public String optString(int index,String def) {
		try {			
			return DataConverter.toString(list.get(index));
		} catch (IndexOutOfBoundsException e) {
			return def;
		}
	}
	
	public String optString(int index) {
		return optString(index, null);
	}
	
	public byte[] getByteArray(int index) {
		try {			
			byte[] buffer = (byte[]) list.get(index);
			return buffer;
		} catch (Exception e) {
			throw new CSONIndexNotFoundException(e);
		}
	}

	public byte[] optByteArray(int index) {
		try {
			return DataConverter.toByteArray(list.get(index));
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}

	public byte[] optByteArray(int index,byte[] def) {
		try {
			return DataConverter.toByteArray(list.get(index));
		} catch (IndexOutOfBoundsException e) {
			return def;
		}
	}

	
	public boolean getBoolean(int index) {
		try {			
			return DataConverter.toBoolean(list.get(index));
		} catch (IndexOutOfBoundsException e) {
			throw new CSONIndexNotFoundException(e);
		}
	}
	
	public boolean optBoolean(int index, boolean def) {
		try {			
			return DataConverter.toBoolean(list.get(index), def);
		} catch (IndexOutOfBoundsException e) {
			return def;
		}
	}
	
	public boolean optBoolean(int index) {
		return optBoolean(index, false);
	}

	@Override
	public boolean remove(Object o) {
		return list.remove(o);
	}

	@Override
	public boolean containsAll(@SuppressWarnings("rawtypes") Collection c) {
		return list.containsAll(c);
	}


	@Override
	public boolean addAll(@SuppressWarnings("rawtypes") Collection c) {
		return list.addAll(c);
	}

	@Override
	public boolean removeAll(@SuppressWarnings("rawtypes") Collection c) {
		return list.removeAll(c);
	}

	@Override
	public boolean retainAll(@SuppressWarnings("rawtypes") Collection c) {
		return list.retainAll(c);
	}

	@Override
	public void clear() {
		list.clear();
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
		for(int i = 0, n = list.size(); i < n; ++i) {
			Object obj = list.get(i);
			if(obj == null || obj instanceof NullValue) writer.addNull();
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


	protected void write(JSONWriter writer) {
		writer.openArray();
		for(int i = 0, n = list.size(); i < n; ++i) {
			Object obj = list.get(i);
		 	CommentObject commentObject = commentObjectList == null ? null : commentObjectList.get(i);
			 if(commentObject != null && !(obj instanceof CSONElement)) {
				 writer.nextCommentObject(commentObject);
			 }
			if(obj == null || obj instanceof NullValue) writer.addNull();
			else if(obj instanceof CSONElement)  {
				CommentObject objectElementHeadCache = ((CSONElement) obj).getHeadCommentObject();
				CommentObject objectElementTailCache = ((CSONElement) obj).getTailCommentObject();
				try {
					if (null != commentObject) {
						((CSONElement) obj).setHeadComment(commentObject.getBeforeComment());
						((CSONElement) obj).setTailCommentObject(new CommentObject((objectElementTailCache == null ? null : objectElementTailCache.getBeforeComment()),commentObject.getAfterComment()));
					}
					((CSONElement)obj).write(writer);
				} finally {
					((CSONElement)obj).setHeadCommentObject(objectElementHeadCache);
					((CSONElement)obj).setTailCommentObject(objectElementTailCache);
				}
			}
			else if(obj instanceof Byte)	writer.add((byte)obj);
			else if(obj instanceof Short)	writer.add((short)obj);
			else if(obj instanceof Character) writer.add((char)obj);
			else if(obj instanceof Integer) writer.add((int)obj);
			else if(obj instanceof Float) writer.add((float)obj);
			else if(obj instanceof Long) writer.add((long)obj);
			else if(obj instanceof Double) writer.add((double)obj);
			else if(obj instanceof String) writer.add((String)obj);
			else if(obj instanceof byte[]) writer.add((byte[])obj);
			else if(obj instanceof Boolean) writer.add((boolean)obj);
		}
		writer.nextCommentObject(getTailCommentObject());
		writer.closeArray();

	}

	/*
	protected void writeJSONString(StringBuilder strBuilder) {
		strBuilder.append("[");
		for(int i = 0, n = list.size(), np = n - 1; i < n; ++i) {
			Object obj = list.get(i);
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
	}*/


	
	@Override
	public String toString() {
		JSONWriter jsonWriter  = new JSONWriter(jsonOptions);
		write(jsonWriter);
		return jsonWriter.toString();
	}

	public String toString(JSONOptions jsonOptions) {
		JSONWriter jsonWriter  = new JSONWriter(jsonOptions);
		write(jsonWriter);
		return jsonWriter.toString();
	}



	public CSONArray clone() {
		CSONArray array = new CSONArray();
		for(int i = 0, n = list.size(); i < n; ++i) {
			Object obj = list.get(i);
			if(obj instanceof CSONArray) array.add(((CSONArray)obj).clone());
			else if(obj instanceof CSONObject) array.add(((CSONObject)obj).clone());
			else if(obj instanceof CharSequence) array.add(((CharSequence)obj).toString());
			else if(obj instanceof byte[]) {
				byte[] bytes = (byte[])obj;
				byte[] newBytes = new byte[bytes.length];
				System.arraycopy(bytes, 0, newBytes, 0, bytes.length);
				array.add(newBytes);
			}
			else array.add(obj);
		}
		return array;
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof CSONArray)) return false;
		CSONArray csonObject = (CSONArray)obj;
		if(csonObject.size() != size()) return false;

		for(int i = 0, n = list.size(); i < n; ++i) {
			Object compareValue = csonObject.list.get(i);
			Object value = list.get(i);
			if((value == null || value instanceof NullValue) && (compareValue != null && !(compareValue instanceof NullValue)) ) {
				return false;
			}
			else if(value instanceof CharSequence && (!(compareValue instanceof CharSequence) || !value.toString().equals(compareValue.toString())) ) {
				return false;
			}
			else if(value instanceof Boolean && (!(compareValue instanceof Boolean) || (Boolean)value != (Boolean)compareValue)) {
				return false;
			}
			else if(value instanceof Number) {
				boolean valueIsFloat = (value instanceof Float || value instanceof Double);
				boolean compareValueIsFloat = (compareValue instanceof Float || compareValue instanceof Double);
				if(valueIsFloat != compareValueIsFloat) {
					return false;
				}
				BigDecimal v1 = BigDecimal.valueOf(((Number)value).doubleValue());
				BigDecimal v2 = BigDecimal.valueOf(((Number)compareValue).doubleValue());
				if(v1.compareTo(v2) != 0) {
					return false;
				}
			}
			else if(value instanceof CSONArray && (!(compareValue instanceof CSONArray) || !((CSONArray)value).equals(compareValue))) {
				return false;
			}
			else if(value instanceof CSONObject && (!(compareValue instanceof CSONObject) || !((CSONObject)value).equals(compareValue))) {
				return false;
			}
			else if(value instanceof byte[] && (!(compareValue instanceof byte[]) || !Arrays.equals((byte[])value, (byte[])compareValue))) {
				return false;
			}
		}
		return true;
	}

}
