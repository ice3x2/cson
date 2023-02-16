package com.snoworca.cson;

import java.math.BigDecimal;
import java.util.ArrayDeque;


public class JSONWriter {

	private final static int DEFAULT_BUFFER_SIZE = 512;

	private String keyQuote = "\"";
	private String valueQuote = "\"";
	private String depthTab = "  ";

	private  boolean isPretty = true;
	private boolean isUnprettyArray = true;

	private ArrayDeque<ObjectType> typeStack_ = new ArrayDeque<>();
	private StringBuilder stringBuilder = new StringBuilder(DEFAULT_BUFFER_SIZE);



	private ObjectType removeStack() {
		return typeStack_.removeLast();
	}

	private void changeStack(ObjectType type) {
		typeStack_.removeLast();
		typeStack_.addLast(type);
	}

	private void pushStack(ObjectType type) {
		typeStack_.addLast(type);
	}

	private CharSequence getDepthTab() {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < typeStack_.size(); i++) {
			stringBuilder.append(depthTab);
		}
		return stringBuilder;
	}

	public JSONWriter() {

	}

	private void writeComment(String comment) {
		if(comment != null) {
			String[] commentLines = comment.split("\n");
			for (String commentLine : commentLines) {
				if(commentLine.trim().isEmpty()) {
					stringBuilder.append("\n");
					continue;
				}
				stringBuilder.append("//");
				stringBuilder.append(commentLine);
				stringBuilder.append("\n");
			}
		}
	}

	private void writeBeforeComment(CommentObject commentObject) {
		if(commentObject != null) {
			String beforeComment  = commentObject.getBeforeComment();
			stringBuilder.append("\n");
			writeComment(beforeComment);
		}
	}

	private void writeAfterComment(CommentObject commentObject) {
		if(commentObject != null) {
			String beforeComment  = commentObject.getAfterComment();
			writeComment(beforeComment);
		}
	}

	public JSONWriter key(String key) {
		ObjectType type = typeStack_.getLast();
		if(type != ObjectType.OpenObject) {
			stringBuilder.append(',');
		}
		else {
			changeStack(ObjectType.Object);
		}


		pushStack(ObjectType.ObjectKey);
		if(isPretty) {
			stringBuilder.append("\n");
		}
		stringBuilder.append(getDepthTab());
		stringBuilder.append(keyQuote);
		stringBuilder.append(DataConverter.escapeJSONString(key));
		stringBuilder.append(keyQuote);

		stringBuilder.append(":");
		return this;
	}

	public JSONWriter key(char key) {
		key(String.valueOf(key));
		return this;
	}

	public JSONWriter nullValue() {
		if(typeStack_.getLast() != ObjectType.ObjectKey) {
			throw new CSONWriteException();
		}
		stringBuilder.append("null");
		removeStack();
		return this;
	}


	public JSONWriter value(String value) {
		if(value== null) {
			nullValue();
			return this;
		}
		if(typeStack_.getLast() != ObjectType.ObjectKey) {
			throw new CSONWriteException();
		}
		removeStack();
		stringBuilder.append('"');
		stringBuilder.append(DataConverter.escapeJSONString(value));
		stringBuilder.append('"');
		return this;
	}

	public JSONWriter value(byte[] value) {
		if(value== null) {
			nullValue();
			return this;
		}
		if(typeStack_.getLast() != ObjectType.ObjectKey) {
			throw new CSONWriteException();
		}
		removeStack();
		stringBuilder.append("\"base64,");
		stringBuilder.append(Base64.encode(value));
		stringBuilder.append('"');
		return this;
	}

	public JSONWriter value(Object value) {
		if(value== null) {
			nullValue();
			return this;
		}
		if(typeStack_.getLast() != ObjectType.ObjectKey) {
			throw new CSONWriteException();
		}
		removeStack();
		if(value instanceof CharSequence || value instanceof Character) {
			stringBuilder.append('"');
			stringBuilder.append(DataConverter.escapeJSONString(value.toString()));
			stringBuilder.append('"');
		} else if(value instanceof Number) {
			stringBuilder.append(value);
		} else if(value instanceof Boolean) {
			stringBuilder.append(value);
		} else if(value instanceof byte[]) {
			stringBuilder.append("\"base64,");
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
		if(typeStack_.getLast() != ObjectType.ObjectKey) {
			throw new CSONWriteException();
		}
		removeStack();
		stringBuilder.append(value);
		return this;
	}

	public JSONWriter value(int value) {
		if(typeStack_.getLast() != ObjectType.ObjectKey) {
			throw new CSONWriteException();
		}
		removeStack();
		stringBuilder.append(value);
		return this;
	}

	public JSONWriter value(long value) {
		if(typeStack_.getLast() != ObjectType.ObjectKey) {
			throw new CSONWriteException();
		}
		removeStack();
		stringBuilder.append(value);
		return this;
	}

	public JSONWriter value(short value) {
		if(typeStack_.getLast() != ObjectType.ObjectKey) {
			throw new CSONWriteException();
		}
		removeStack();
		stringBuilder.append(value);
		return this;
	}

	public JSONWriter value(boolean value) {
		if(typeStack_.getLast() != ObjectType.ObjectKey) {
			throw new CSONWriteException();
		}
		removeStack();
		stringBuilder.append(value ? "true" : "false");
		return this;
	}

	public JSONWriter value(char value) {
		if(typeStack_.getLast() != ObjectType.ObjectKey) {
			throw new CSONWriteException();
		}
		removeStack();
		stringBuilder.append(valueQuote);
		stringBuilder.append(value);
		stringBuilder.append(valueQuote);
		return this;
	}

	public JSONWriter value(float value) {
		if(typeStack_.getLast() != ObjectType.ObjectKey) {
			throw new CSONWriteException();
		}
		removeStack();
		stringBuilder.append(value);
		return this;
	}

	public JSONWriter value(double value) {
		if(typeStack_.getLast() != ObjectType.ObjectKey) {
			throw new CSONWriteException();
		}
		removeStack();
		stringBuilder.append(value);
		return this;
	}

	private void checkAndAppendInArray() {
		ObjectType type = typeStack_.getLast();
		if(type != ObjectType.OpenArray) {
			stringBuilder.append(',');
		} else if(type == ObjectType.OpenArray) {
			changeStack(ObjectType.Array);
		}
		else if(type != ObjectType.Array) {
			throw new CSONWriteException();
		}

		if(isPretty && !isUnprettyArray) {
			stringBuilder.append('\n');
			stringBuilder.append(getDepthTab());
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
		stringBuilder.append(DataConverter.escapeJSONString(value));
		stringBuilder.append('"');

		return this;
	}

	public JSONWriter add(byte[] value) {
		if(value== null) {
			addNull();
			return this;
		}
		checkAndAppendInArray();
		stringBuilder.append("\"base64,");
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
		if(value instanceof CharSequence || value instanceof Character) {
			stringBuilder.append('"');
			stringBuilder.append(DataConverter.escapeJSONString(value.toString()));
			stringBuilder.append('"');
		} else if(value instanceof Number) {
			stringBuilder.append(value);
		} else if(value instanceof Boolean) {
			stringBuilder.append(value);
		} else if(value instanceof byte[]) {
			stringBuilder.append("\"base64,");
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
		stringBuilder.append('\'');
		stringBuilder.append(value);
		stringBuilder.append('\'');
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
		if(!typeStack_.isEmpty()) {
			ObjectType type = typeStack_.getLast();
			if (type == ObjectType.OpenArray) {
				changeStack(ObjectType.Array);
				if(isPretty && !isUnprettyArray) {
					stringBuilder.append('\n');
					stringBuilder.append(getDepthTab());
				}
			} else if (type == ObjectType.Array) {
				stringBuilder.append(',');
				if(isPretty && !isUnprettyArray) {
					stringBuilder.append('\n');
					stringBuilder.append(getDepthTab());
				}
			} else if (type != ObjectType.ObjectKey && type != ObjectType.None) {
				throw new CSONWriteException();
			}
		}
		pushStack(ObjectType.OpenArray);
		stringBuilder.append('[');
		return this;
	}

	public JSONWriter closeArray() {
		ObjectType type = typeStack_.getLast();
		if(type != ObjectType.Array && type != ObjectType.OpenArray) {
			throw new CSONWriteException();
		}
		removeStack();
		if(isPretty && !isUnprettyArray && stringBuilder.charAt(stringBuilder.length() - 1) != '[') {
			stringBuilder.append('\n');
			stringBuilder.append(getDepthTab());
		}
		if(typeStack_.isEmpty()) {
			stringBuilder.append(']');
			return this;
		}

		if(typeStack_.getLast() == ObjectType.ObjectKey) {
			removeStack();
		}
		stringBuilder.append(']');
		return this;
	}

	public JSONWriter openObject() {
		ObjectType type = typeStack_.isEmpty() ? null : typeStack_.getLast();
		if(type == ObjectType.Object) {
			throw new CSONWriteException();
		} else if(type == ObjectType.Array) {
			stringBuilder.append(',');
			if(isPretty) {
				stringBuilder.append('\n');
				stringBuilder.append(getDepthTab());
			}
		} else if(type == ObjectType.OpenArray) {
			if(isPretty) {
				stringBuilder.append('\n');
				stringBuilder.append(getDepthTab());
			}
			changeStack(ObjectType.Array);
		}
		stringBuilder.append('{');
		pushStack(ObjectType.OpenObject);
		return this;
	}

	public JSONWriter closeObject() {
		if(typeStack_.getLast() != ObjectType.Object && typeStack_.getLast() != ObjectType.OpenObject) {
			throw new CSONWriteException();
		}
		removeStack();

		if(isPretty && stringBuilder.charAt(stringBuilder.length() - 1) != '{') {
			stringBuilder.append('\n');
			stringBuilder.append(getDepthTab());
		} else {
			stringBuilder.append("");
		}
		stringBuilder.append('}');


		if(!typeStack_.isEmpty() && typeStack_.getLast() == ObjectType.ObjectKey) {
			removeStack();
		}

		return this;
	}

	@Override
	public String toString() {
		if(!typeStack_.isEmpty()) {
			throw new CSONWriteException();
		}
		return stringBuilder.toString();
	}

}
