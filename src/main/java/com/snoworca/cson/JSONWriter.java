package com.snoworca.cson;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;


public class JSONWriter {

	private final static int DEFAULT_BUFFER_SIZE = 512;


	private ArrayDeque<ObjectType> typeStack = new ArrayDeque<>();
	private StringBuilder stringBuilder = new StringBuilder(DEFAULT_BUFFER_SIZE);



	public JSONWriter() {

	}


	
	public JSONWriter key(String key) {
		ObjectType type = typeStack.getLast();
		if(type != ObjectType.OpenObject) {
			stringBuilder.append(',');
		}
		else if(type != ObjectType.Object) {
			throw new CSONWriteException();
		}
		typeStack.addLast(ObjectType.ObjectKey);
		stringBuilder.append('"');
		stringBuilder.append(key);
		stringBuilder.append("\":");
		return this;
	 }

	 public JSONWriter key(char key) {
		 key(String.valueOf(key));
		 return this;
	 }

	 public JSONWriter nullValue() {
		 if(typeStack.getLast() != ObjectType.ObjectKey) {
			 throw new CSONWriteException();
		 }
		 stringBuilder.append("null");
		 typeStack.removeLast();
		 return this;
	 }
	 
	 public JSONWriter value(String value) {
		 if(value== null) {
			 nullValue();
			 return this;
		 }
		 if(typeStack.getLast() != ObjectType.ObjectKey) {
			 throw new CSONWriteException();
		 }
		 typeStack.removeLast();
		 stringBuilder.append('"');
		 stringBuilder.append(value);
		 stringBuilder.append('"');
		 return this;
	 }
	 
	 public JSONWriter value(byte[] value) {
		 if(value== null) {
			 nullValue();
			 return this;
		 }
		 if(typeStack.getLast() != ObjectType.ObjectKey) {
			 throw new CSONWriteException();
		 }
		 typeStack.removeLast();
		 stringBuilder.append('"');
		 stringBuilder.append(Base64.encode(value));
		 stringBuilder.append('"');
		 return this;
	 }

	public JSONWriter value(Object value) {
		if(value== null) {
			nullValue();
			return this;
		}
		if(typeStack.getLast() != ObjectType.ObjectKey) {
			throw new CSONWriteException();
		}
		typeStack.removeLast();
		if(value instanceof CharSequence) {
			stringBuilder.append('"');
			stringBuilder.append(value);
			stringBuilder.append('"');
		} else if(value instanceof Number) {
			stringBuilder.append(value);
		} else if(value instanceof Boolean) {
			stringBuilder.append(value);
		} else if(value instanceof byte[]) {
			stringBuilder.append('"');
			stringBuilder.append(Base64.encode((byte[])value));
			stringBuilder.append('"');
		} else if(value instanceof CSONElement) {
			stringBuilder.append(value);
		}  else  {
			stringBuilder.append('"');
			stringBuilder.append(value);
			stringBuilder.append('"');
		}
		return this;
	}
	 
	 public JSONWriter value(byte value) {
		 if(typeStack.getLast() != ObjectType.ObjectKey) {
			 throw new CSONWriteException();
		 }
		 typeStack.removeLast();
		 stringBuilder.append(value);
		 return this;
	 }
	 
	 public JSONWriter value(int value) {
		 if(typeStack.getLast() != ObjectType.ObjectKey) {
			 throw new CSONWriteException();
		 }
		 
		 typeStack.removeLast();
		 stringBuilder.append(value);
		 return this;
	 }
	 
	 public JSONWriter value(long value) {
		 if(typeStack.getLast() != ObjectType.ObjectKey) {
			 throw new CSONWriteException();
		 }
		 typeStack.removeLast();
		 stringBuilder.append(value);
		 return this;
	 }
	 
	 public JSONWriter value(short value) {
		 if(typeStack.getLast() != ObjectType.ObjectKey) {
			 throw new CSONWriteException();
		 }
		 typeStack.removeLast();
		 stringBuilder.append(value);
		 return this;
	 }
	 
	 public JSONWriter value(boolean value) {
		 if(typeStack.getLast() != ObjectType.ObjectKey) {
			 throw new CSONWriteException();
		 }
		 typeStack.removeLast();
		 stringBuilder.append(value ? "true" : "false");
		 return this;
	 }
	 
	 public JSONWriter value(char value) {
		 if(typeStack.getLast() != ObjectType.ObjectKey) {
			 throw new CSONWriteException();
		 }
		 typeStack.removeLast();
		 stringBuilder.append(value);
		 return this;
	 }
	 
	 public JSONWriter value(float value) {
		 if(typeStack.getLast() != ObjectType.ObjectKey) {
			 throw new CSONWriteException();
		 }
		 typeStack.removeLast();
		 stringBuilder.append(value);
		 return this;
	 }
	 
	 public JSONWriter value(double value) {
		 if(typeStack.getLast() != ObjectType.ObjectKey) {
			 throw new CSONWriteException();
		 }
		 typeStack.removeLast();
		 stringBuilder.append(value);
		 return this;
	 }

	 private void checkAndAppendInArray() {
		 ObjectType type = typeStack.getLast();
		 if(type != ObjectType.OpenArray) {
			 stringBuilder.append(',');
		 } else if(type == ObjectType.OpenArray) {
			 typeStack.removeLast();
			 typeStack.addLast(ObjectType.Array);
		 }
		 else if(type != ObjectType.Array) {
			 throw new CSONWriteException();
		 }


	 }
	 
	 ///
	 public JSONWriter addNull() {
		 checkAndAppendInArray();
		 stringBuilder.append("null");
		 return this;
	 }
	 
	 public JSONWriter add(String value) {
		 if(value== null) {
			 addNull();
			 return this;
		 }
		 checkAndAppendInArray();
		 stringBuilder.append('"');
		 stringBuilder.append(value);
		 stringBuilder.append('"');

		 return this;
	 }
	 
	 public JSONWriter add(byte[] value) {
		 if(value== null) {
			 addNull();
			 return this;
		 }
		 checkAndAppendInArray();
		 stringBuilder.append('"');
		 stringBuilder.append(Base64.encode(value));
		 stringBuilder.append('"');
		 return this;
	 }

	public JSONWriter add(BigDecimal value) {
		if(value== null) {
			addNull();
			return this;
		}
		checkAndAppendInArray();
		stringBuilder.append(value);
		return this;
	}
	 
	 
	 public JSONWriter add(byte value) {
		 checkAndAppendInArray();
		 stringBuilder.append(value);
		 return this;
	 }
	 
	 public JSONWriter add(int value) {
		 checkAndAppendInArray();
		 stringBuilder.append(value);
		 return this;
	 }
	 
	 public JSONWriter add(long value) {
		 checkAndAppendInArray();
		 stringBuilder.append(value);
		 return this;
	 }
	 
	 public JSONWriter add(short value) {
		 checkAndAppendInArray();
		 stringBuilder.append(value);
		 return this;
	 }
	 
	 public JSONWriter add(boolean value) {
		 checkAndAppendInArray();
		 stringBuilder.append(value ? "true" : "false");

		 return this;
	 }

	public JSONWriter add(Object value) {
		if(value== null) {
			addNull();
			return this;
		}
		checkAndAppendInArray();
		if(value instanceof CharSequence) {
			stringBuilder.append('"');
			stringBuilder.append(value);
			stringBuilder.append('"');
		} else if(value instanceof Number) {
			stringBuilder.append(value);
		} else if(value instanceof Boolean) {
			stringBuilder.append(value);
		} else if(value instanceof byte[]) {
			stringBuilder.append('"');
			stringBuilder.append(Base64.encode((byte[])value));
			stringBuilder.append('"');
		} else if(value instanceof CSONElement) {
			stringBuilder.append(value);
		}  else  {
			stringBuilder.append('"');
			stringBuilder.append(value);
			stringBuilder.append('"');
		}
		return this;
	}

	 
	 public JSONWriter add(char value) {
		 checkAndAppendInArray();
		 stringBuilder.append(value);
		 return this;
	 }
	 
	 public JSONWriter add(float value) {
		 checkAndAppendInArray();
		 stringBuilder.append(value);
		 return this;
	 }
	 
	 public JSONWriter add(double value) {
		 checkAndAppendInArray();
		 stringBuilder.append(value);
		 return this;
	 }
	 
	 
	 
	 public JSONWriter openArray() {
		 if(typeStack.getLast() != ObjectType.ObjectKey && typeStack.getLast() != ObjectType.Array && typeStack.getLast() != ObjectType.None) {
			 throw new CSONWriteException();
		 }
		 typeStack.addLast(ObjectType.OpenArray);
		 stringBuilder.append('[');
		 return this;
	 }
	 
	 public JSONWriter closeArray() {
		 if(typeStack.getLast() != ObjectType.Array) {
			 throw new CSONWriteException();
		 }
		 typeStack.removeLast();
		 if(typeStack.getLast() == ObjectType.ObjectKey) {
			 typeStack.removeLast();
		 }
		 stringBuilder.append(']');
		 return this;
	 }
	 
	 public JSONWriter openObject() {
		 if(typeStack.getLast() == ObjectType.Object) {
			 throw new CSONWriteException();
		 }
		 typeStack.addLast(ObjectType.Object);
		 stringBuilder.append('{');
		 return this;
	 }
	 
	 public JSONWriter closeObject() {
		 if(typeStack.getLast() != ObjectType.Object) {
			 throw new CSONWriteException();
		 }
		 typeStack.removeLast();
		 if(typeStack.getLast() == ObjectType.ObjectKey) {
			 typeStack.removeLast();
		 }
		 stringBuilder.append('}');
		 return this;
	 }
	 
	 public String toString() {
		 if(typeStack.getLast() != ObjectType.None) {
			 throw new CSONWriteException();
		 }
		 return stringBuilder.toString();
	 }
 
}
