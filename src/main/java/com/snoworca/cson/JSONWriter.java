package com.snoworca.cson;

import java.math.BigDecimal;
import java.util.ArrayDeque;


public class JSONWriter {

	private final static int DEFAULT_BUFFER_SIZE = 512;

	private JSONOptions jsonOptions = JSONOptions.json();

	private final static int COMMENT_BEFORE_KEY = 1;

	private final static int COMMENT_SLASH_START = 4;


	private final static int COMMENT_BEFORE_ARRAY_VALUE = 5;
	private final static int COMMENT_COMMA_AND_SLASH_START = 6;

	private boolean isComment = false;
	private boolean isAllowLineBreak = false;
	private boolean isAllowUnquoted = false;
	private boolean isPretty = false;
	private boolean isUnprettyArray = false;
	private String depthSpace = "  ";

	private String keyQuote = "\"";
	private String valueQuote = "\"";


	//private String commentStringAfterObjectValue = null;
	private ArrayDeque<CommentObject> keyValueCommentObjects = new ArrayDeque<>();
	//private ArrayDeque<CommentObject> objectCommentObjects = new ArrayDeque<>();


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
			stringBuilder.append(depthSpace);
		}
		return stringBuilder;
	}



	private void writeAfterComment(int type) {
		if(!keyValueCommentObjects.isEmpty()) {
			CommentObject commentObject  = keyValueCommentObjects.removeFirst();
			String afterComment = commentObject.getAfterComment();
			if(afterComment == null) {
				return;
			}
			writeComment(afterComment, type);
		}
	}


	private void writeBeforeComment() {
		writeBeforeComment(COMMENT_BEFORE_ARRAY_VALUE);
	}

	private void writeBeforeComment(int type) {
		if(type == 6) {
			System.out.println('s');
		}
		if(!keyValueCommentObjects.isEmpty()) {
			CommentObject commentObject = keyValueCommentObjects.getFirst();
			String beforeComment  = commentObject.getBeforeComment();
			if(beforeComment == null) {
				return;
			}
			writeComment(beforeComment, type);
			commentObject.setBeforeComment(null);
		}
	}

	private void writeBeforeAndRemoveComment(int type) {
		if(!keyValueCommentObjects.isEmpty()) {
			CommentObject commentObject = keyValueCommentObjects.removeFirst();
			String beforeComment  = commentObject.getBeforeComment();
			if(beforeComment == null) {
				return;
			}
			writeComment(beforeComment, type);
			commentObject.setBeforeComment(null);
		}
	}

	public JSONWriter() {

	}

	public JSONWriter(JSONOptions jsonOptions) {
		this.jsonOptions = jsonOptions;
		if(jsonOptions.isPretty()) {
			isPretty = true;
		}
		if(jsonOptions.isUnprettyArray()) {
			isUnprettyArray = false;
		}
		if(jsonOptions.getDepthSpace() != null) {
			depthSpace = jsonOptions.getDepthSpace();
		}
		isAllowUnquoted = jsonOptions.isAllowUnquoted();
		keyQuote = jsonOptions.getKeyQuote();
		valueQuote = jsonOptions.getValueQuote();
		if(!jsonOptions.isAllowUnquoted() && keyQuote.isEmpty()) {
			keyQuote = "\"";
		}
		if(!jsonOptions.isAllowSingleQuotes()) {
			if(keyQuote.equals("'")) {
				keyQuote = jsonOptions.isAllowUnquoted() ? "" : "\"";
			}
			if(valueQuote.equals("'")) {
				valueQuote = "\"";
			}
		}
		isAllowLineBreak = jsonOptions.isAllowLineBreak();

		isComment = jsonOptions.isAllowComments() && !jsonOptions.isSkipComments();
	}


	private void writeComment(String comment, int commentType) {

		if(comment != null && isComment) {
			String[] commentLines = comment.split("\n");
			for (int i = 0; i < commentLines.length; i++) {
				String commentLine = commentLines[i];
				if(commentLine.trim().isEmpty()) {
					stringBuilder.append("\n");
					continue;
				}
				if(!isPretty || commentType == COMMENT_SLASH_START || commentType == COMMENT_COMMA_AND_SLASH_START) {
					if(i == 0) {
						if(commentType == COMMENT_COMMA_AND_SLASH_START) {
							stringBuilder.append(",");
						}
						stringBuilder.append(" /* ");
					} else {
						stringBuilder.append("\n");
					}
					stringBuilder.append(commentLine);
				}
				else if(commentType == COMMENT_BEFORE_ARRAY_VALUE) {
					stringBuilder.append("//");
					stringBuilder.append(commentLine);
					stringBuilder.append("\n");
					stringBuilder.append(getDepthTab());
				}
				else if(commentType == COMMENT_BEFORE_KEY) {
					stringBuilder.append(getDepthTab());
					stringBuilder.append("//");
					stringBuilder.append(commentLine);
					stringBuilder.append("\n");
				}
			}
			if(commentType == COMMENT_SLASH_START || commentType == COMMENT_COMMA_AND_SLASH_START || !isPretty) {
				stringBuilder.append(" */");
			}

		}
	}


	public void nextCommentObject(CommentObject commentObject) {
		if(commentObject == null) return;
		if(!keyValueCommentObjects.isEmpty() && !keyValueCommentObjects.getLast().isCommented()) {
			keyValueCommentObjects.removeLast();
		}
		if(!commentObject.isCommented()) {
			return;
		}

		this.keyValueCommentObjects.addLast(commentObject.clone());
	}


	protected void writeComment(String comment, boolean alwaysSlash) {
		if(!isComment && comment == null) return;
		if(!isPretty || alwaysSlash || comment.contains("\n")) {
			stringBuilder.append(" /* " ).append(comment).append(" */ ");
		} else {
			this.stringBuilder.append(" //").append(comment);
		}
	}






	private void writeString(String quoteArg, String str) {
		String quote = quoteArg;
		if(isAllowLineBreak && str.contains("\\\n")) {
			quote = "\"";
		}
		str = DataConverter.escapeJSONString(str, isAllowLineBreak);
		stringBuilder.append(quote);
		stringBuilder.append(str);
		stringBuilder.append(quote);
	}

	public JSONWriter key(String key) {
		ObjectType type = typeStack_.getLast();
		if(type != ObjectType.OpenObject) {
			stringBuilder.append(',');
			/*if(commentStringAfterObjectValue != null) {
				writeComment(commentStringAfterObjectValue, COMMENT_SLASH_START);
				commentStringAfterObjectValue = null;
			}*/
		}
		else {
			changeStack(ObjectType.Object);
		}
		pushStack(ObjectType.ObjectKey);
		if(isPretty) {
			stringBuilder.append("\n");
		}
		writeBeforeComment(COMMENT_BEFORE_KEY);
		stringBuilder.append(getDepthTab());
		writeString(keyQuote, key);
		writeAfterComment(COMMENT_SLASH_START);
		stringBuilder.append(":");
		return this;
	}

	/*public JSONWriter key(char key) {
		writeBeforeComment();
		key(String.valueOf(key));
		writeBeforeComment(COMMENT_SLASH_START);;
		return this;
	}*/

	public JSONWriter nullValue() {
		if(typeStack_.getLast() != ObjectType.ObjectKey) {
			throw new CSONWriteException();
		}
		writeBeforeComment(COMMENT_SLASH_START);
		stringBuilder.append("null");
		writeAfterComment(COMMENT_SLASH_START);;
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
		writeBeforeComment(COMMENT_SLASH_START);
		writeString(valueQuote, value);
		writeAfterComment(COMMENT_SLASH_START);;
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
		writeBeforeComment(COMMENT_SLASH_START);
		stringBuilder.append("\"base64,");
		stringBuilder.append(Base64.encode(value));
		stringBuilder.append('"');
		writeAfterComment(COMMENT_SLASH_START);;
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
		writeBeforeComment(COMMENT_SLASH_START);
		if(value instanceof CharSequence || value instanceof Character) {
			writeString(valueQuote, value.toString());
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
		writeAfterComment(COMMENT_SLASH_START);;
		return this;
	}

	public JSONWriter value(byte value) {
		if(typeStack_.getLast() != ObjectType.ObjectKey) {
			throw new CSONWriteException();
		}
		removeStack();
		writeBeforeComment(COMMENT_SLASH_START);
		stringBuilder.append(value);
		writeAfterComment(COMMENT_SLASH_START);;
		return this;
	}

	public JSONWriter value(int value) {
		if(typeStack_.getLast() != ObjectType.ObjectKey) {
			throw new CSONWriteException();
		}
		removeStack();
		writeBeforeComment(COMMENT_SLASH_START);
		stringBuilder.append(value);
		writeAfterComment(COMMENT_SLASH_START);;
		return this;
	}

	public JSONWriter value(long value) {
		if(typeStack_.getLast() != ObjectType.ObjectKey) {
			throw new CSONWriteException();
		}
		removeStack();
		writeBeforeComment(COMMENT_SLASH_START);
		stringBuilder.append(value);
		writeAfterComment(COMMENT_SLASH_START);;
		return this;
	}

	public JSONWriter value(short value) {
		if(typeStack_.getLast() != ObjectType.ObjectKey) {
			throw new CSONWriteException();
		}
		removeStack();
		writeBeforeComment(COMMENT_SLASH_START);
		stringBuilder.append(value);
		writeAfterComment(COMMENT_SLASH_START);;
		return this;
	}

	public JSONWriter value(boolean value) {
		if(typeStack_.getLast() != ObjectType.ObjectKey) {
			throw new CSONWriteException();
		}
		removeStack();
		writeBeforeComment(COMMENT_SLASH_START);
		stringBuilder.append(value ? "true" : "false");
		writeAfterComment(COMMENT_SLASH_START);;
		return this;
	}

	public JSONWriter value(char value) {
		if(typeStack_.getLast() != ObjectType.ObjectKey) {
			throw new CSONWriteException();
		}
		removeStack();
		writeBeforeComment(COMMENT_SLASH_START);
		String quote =  jsonOptions.isAllowCharacter() ? "'" : valueQuote;
		stringBuilder.append(quote);
		stringBuilder.append(value);
		stringBuilder.append(quote);
		writeAfterComment(COMMENT_SLASH_START);;
		return this;
	}



	public JSONWriter value(float value) {
		if(typeStack_.getLast() != ObjectType.ObjectKey) {
			throw new CSONWriteException();
		}
		removeStack();
		writeBeforeComment(COMMENT_SLASH_START);
		if(jsonOptions.isAllowInfinity() && Float.isInfinite(value)) {
			if(value > 0) {
				stringBuilder.append("Infinity");
			} else {
				stringBuilder.append("-Infinity");
			}
		} else if(Float.isNaN(value)) {
			if(jsonOptions.isAllowNaN()) {
				stringBuilder.append("NaN");
			} else {
				stringBuilder.append(valueQuote).append("NaN").append(valueQuote);
			}
		} else {
			stringBuilder.append(value);
		}
		writeAfterComment(COMMENT_SLASH_START);;
		return this;
	}

	public JSONWriter value(double value) {
		if(typeStack_.getLast() != ObjectType.ObjectKey) {
			throw new CSONWriteException();
		}
		removeStack();
		writeBeforeComment(COMMENT_SLASH_START);
		if(jsonOptions.isAllowInfinity() && Double.isInfinite(value)) {
			if(value > 0) {
				stringBuilder.append("Infinity");
			} else {
				stringBuilder.append("-Infinity");
			}
		} else if(Double.isNaN(value)) {
			if(jsonOptions.isAllowNaN()) {
				stringBuilder.append("NaN");
			} else {
				stringBuilder.append(valueQuote).append("NaN").append(valueQuote);
			}
		} else {
			stringBuilder.append(value);
		}
		writeAfterComment(COMMENT_SLASH_START);;
		return this;
	}

	private void checkAndAppendInArray() {
		ObjectType type = typeStack_.getLast();
		if(type != ObjectType.OpenArray) {
			writeAfterComment(COMMENT_SLASH_START);
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
		writeBeforeComment();
		stringBuilder.append("null");
		return this;
	}

	public JSONWriter add(String value) {
		if(value== null) {
			addNull();
			return this;
		}
		writeBeforeComment();
		checkAndAppendInArray();
		writeString(valueQuote, value);
		writeBeforeComment(COMMENT_SLASH_START);
		return this;
	}

	public JSONWriter add(byte[] value) {
		if(value== null) {
			addNull();
			return this;
		}
		writeBeforeComment();
		checkAndAppendInArray();
		stringBuilder.append("\"base64,");
		stringBuilder.append(Base64.encode(value));
		stringBuilder.append('"');
		writeBeforeComment(COMMENT_SLASH_START);;
		return this;
	}

	public JSONWriter add(BigDecimal value) {
		if(value== null) {
			addNull();
			return this;
		}
		writeBeforeComment();
		checkAndAppendInArray();
		stringBuilder.append(value);
		writeBeforeComment(COMMENT_SLASH_START);;
		return this;
	}


	public JSONWriter add(byte value) {
		writeBeforeComment();
		checkAndAppendInArray();
		stringBuilder.append(value);
		writeBeforeComment(COMMENT_SLASH_START);;
		return this;
	}

	public JSONWriter add(int value) {
		checkAndAppendInArray();
		writeBeforeComment();
		stringBuilder.append(value);
		writeBeforeComment(COMMENT_SLASH_START);;
		return this;
	}

	public JSONWriter add(long value) {
		writeBeforeComment();
		checkAndAppendInArray();
		stringBuilder.append(value);
		writeBeforeComment(COMMENT_SLASH_START);;
		return this;
	}

	public JSONWriter add(short value) {
		writeBeforeComment();
		checkAndAppendInArray();
		stringBuilder.append(value);
		writeBeforeComment(COMMENT_SLASH_START);;
		return this;
	}

	public JSONWriter add(boolean value) {
		writeBeforeComment();
		checkAndAppendInArray();
		stringBuilder.append(value ? "true" : "false");
		writeBeforeComment(COMMENT_SLASH_START);;
		return this;
	}

	public JSONWriter add(Object value) {
		if(value== null) {
			addNull();
			return this;
		}
		writeBeforeComment();
		checkAndAppendInArray();
		if(value instanceof CharSequence || value instanceof Character) {
			writeString(valueQuote, value.toString());
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
		writeBeforeComment(COMMENT_SLASH_START);;
		return this;
	}


	public JSONWriter add(char value) {
		writeBeforeComment();
		checkAndAppendInArray();
		stringBuilder.append('\'');
		stringBuilder.append(value);
		stringBuilder.append('\'');
		writeBeforeComment(COMMENT_SLASH_START);;
		return this;
	}

	public JSONWriter add(float value) {
		writeBeforeComment();
		checkAndAppendInArray();
		stringBuilder.append(value);
		writeBeforeComment(COMMENT_SLASH_START);;
		return this;
	}

	public JSONWriter add(double value) {
		writeBeforeComment();
		checkAndAppendInArray();
		stringBuilder.append(value);
		writeBeforeComment(COMMENT_SLASH_START);;
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
			} else {
				writeBeforeComment(COMMENT_SLASH_START);
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

		writeBeforeComment(COMMENT_COMMA_AND_SLASH_START);

		if(typeStack_.isEmpty()) {
			stringBuilder.append(']');
			writeBeforeComment(COMMENT_BEFORE_KEY);;
			return this;
		}

		if(typeStack_.getLast() == ObjectType.ObjectKey) {
			removeStack();
		}
		stringBuilder.append(']');
		writeAfterComment(COMMENT_SLASH_START);;
		return this;
	}

	public JSONWriter writeHeaderComment(CommentObject commentObject) {
		if(!isComment || commentObject == null || commentObject.getBeforeComment() == null) {
			return this;
		}
		stringBuilder.append("/*").append(commentObject.getBeforeComment()).append( "*/");

		return this;

	}

	public JSONWriter openObject() {
		ObjectType type = typeStack_.isEmpty() ? null : typeStack_.getLast();
		int commentType = COMMENT_BEFORE_ARRAY_VALUE;
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
		} else if(type == ObjectType.ObjectKey) {
			commentType = COMMENT_SLASH_START;
		} else if(type == ObjectType.OpenObject) {
			commentType = COMMENT_BEFORE_KEY;
		}
		writeBeforeAndRemoveComment(commentType);
		stringBuilder.append('{');
		pushStack(ObjectType.OpenObject);
		return this;
	}

	public JSONWriter closeObject() {
		if(typeStack_.getLast() != ObjectType.Object && typeStack_.getLast() != ObjectType.OpenObject) {
			throw new CSONWriteException();
		}
		removeStack();
		/*if(commentStringAfterObjectValue != null) {
			writeComment(commentStringAfterObjectValue, COMMENT_SLASH_START);
			commentStringAfterObjectValue = null;
		}*/
		if(isPretty && stringBuilder.charAt(stringBuilder.length() - 1) != '{') {
			stringBuilder.append('\n');
			stringBuilder.append(getDepthTab());
		} else {
			stringBuilder.append("");
		}
		writeBeforeComment(COMMENT_COMMA_AND_SLASH_START);

		stringBuilder.append('}');

		if(!typeStack_.isEmpty() && typeStack_.getLast() == ObjectType.ObjectKey) {
			removeStack();
		}
		if(typeStack_.isEmpty()) {
			writeAfterComment(COMMENT_BEFORE_KEY);
		} else {
			writeAfterComment(COMMENT_SLASH_START);
		}
		return this;
	}

	@Override
	public String toString() {
		/*if(!typeStack_.isEmpty()) {
			throw new CSONWriteException();
		}*/
		return stringBuilder.toString();
	}

}
