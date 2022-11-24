package com.snoworca.cson;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

class DataConverter {
	
	private final static Charset UTF8 = Charset.forName("UTF-8");

	final static CSONArray toArray(Object value) {
		if(value instanceof CSONArray) {
			return (CSONArray)value;
		}
		return null; 
	}
	
	final static CSONObject toObject(Object value) {
		if(value instanceof CSONObject) {
			return (CSONObject)value;
		}
		return null; 
	}

	final static int toInteger(Object value) {
		return toInteger(value, 0);
	}
	
	final static int toInteger(Object value, int def) {
		try {
			if (value instanceof Number) {
				return ((Number) value).intValue();
			} else if (value instanceof Character) {
				return ((Character) value).charValue();
			} else if (value instanceof Boolean) {
				return ((Boolean) value) ? 1 : 0;
			} else if (value instanceof String) {
				return Integer.parseInt((String) value);
			} else if (value instanceof byte[] && ((byte[]) value).length > 3) {
				return ByteBuffer.wrap((byte[]) value).getInt();
			}
		} catch (Throwable e) {}
		return def;

	}

	final static short toShort(Object value) {
		return toShort(value, (short) 0);
	}
	
	final static short toShort(Object value, short def) {
		try {
			if (value instanceof Number) {
				return ((Number) value).shortValue();
			} else if (value instanceof Character) {
				return (short) ((Character) value).charValue();
			} else if (value instanceof String) {
				return Short.parseShort((String) value);
			} else if (value instanceof byte[] && ((byte[]) value).length > 1) {
				return ByteBuffer.wrap((byte[]) value).getShort();
			}
		} catch (Throwable e) {}
		return def;
	}


	final static float toFloat(Object value) {
		return toFloat(value, 0);
	}
	
	final static float toFloat(Object value, float def) {
		try {
			if (value instanceof Number) {
				return ((Number) value).floatValue();
			} else if (value instanceof Character) {
				return ((Character) value).charValue();
			} else if (value instanceof Boolean) {
				return ((Boolean) value) ? 1 : 0;
			} else if (value instanceof String) {
				return Float.parseFloat((String) value);
			} else if (value instanceof byte[] && ((byte[]) value).length > 3) {
				return ByteBuffer.wrap((byte[]) value).getFloat();
			}
		}catch (Throwable e) {}
		return def;
	}

	final static double toDouble(Object value) {
		return toDouble(value, 0);
	}
	
	final static double toDouble(Object value, double def) {
		try {
			if (value instanceof Number) {
				return ((Number) value).doubleValue();
			} else if (value instanceof Character) {
				return ((Character) value).charValue();
			} else if (value instanceof String) {
				return Double.parseDouble((String) value);
			} else if (value instanceof byte[] && ((byte[]) value).length > 7) {
				return ByteBuffer.wrap((byte[]) value).getDouble();
			}
		} catch (Throwable e) {}
		return def;
	}

	final static long toLong(Object value) {
		return toLong(value, 0L);
	}
	
	
	final static long toLong(Object value, long def) {

		try {
			if (value instanceof Number) {
				return ((Number) value).longValue();
			} else if (value instanceof Character) {
				return ((Character) value).charValue();
			} else if (value instanceof Boolean) {
				return ((Boolean) value) ? 1 : 0;
			} else if (value instanceof String) {
				return Long.parseLong((String) value);
			} else if (value instanceof byte[] && ((byte[]) value).length > 7) {
				return ByteBuffer.wrap((byte[]) value).getLong();
			}
		} catch (Throwable e) {}
		return def;
	}

	final static char toChar(Object value) {
		return toChar(value, '\0');
	}
	
	final static char toChar(Object value, char def) {
		if(value instanceof Number) {
			return (char)((Number)value).shortValue();
		}
		else if(value instanceof Character) {
			return ((Character)value).charValue();
		}
		else if(value instanceof Boolean) {
			return (char)(((Boolean)value) ? 1 : 0);
		}
		else if(value instanceof String) {
  			return (char) Short.parseShort((String) value);
		} 
		else if(value instanceof byte[] && ((byte[])value).length > 1 ) {
  			return (char) ByteBuffer.wrap((byte[])value).getShort();
		} 
		return def;
	}
	
	
	
	final static  String toString(Object value) {
		if(value == null) return null;
		if(value instanceof String) { 
			return (String) value;
		}
		if(value instanceof Number) {
			return ((Number)value).toString();
		}
		else if(value instanceof byte[]) {
			byte[] buffer = (byte[])value;
  			return Base64.encode(buffer);
		} 
		return value + "";
	}

	final static  boolean toBoolean(Object value) {
		return toBoolean(value, false);

	}
	
	final static  boolean toBoolean(Object value, boolean def) {
		try {
			if (value instanceof Boolean) {
				return ((Boolean) value);
			} else if (value instanceof Number) {
				return ((Number) value).intValue() > 0;
			} else if (value instanceof String) {
				String strValue = ((String) value).trim();
				return ("true".equalsIgnoreCase(strValue) || "1".equals(strValue));
			}
		}catch (Throwable e) {}
		return def;
	}
	
	
	final static String escapeJSONString(String str) {
		if(str == null) return  null;
		char[] charArray = str.toCharArray();
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < charArray.length; ++i) {
			char ch = charArray[i];
			switch (ch) {
			case '\n':
				builder.append("\\n");
				break;
			case '\r':
				builder.append("\\r");
				break;
			case '\f':
				builder.append("\\f");
				break;
			case '\t':
				builder.append("\\t");
				break;
			case '\b':
				builder.append("\\b");
				break;
			case '"':
				builder.append("\\\"");
				break;
			case '\\':
				builder.append("\\");
				break;
			default:
				builder.append(ch);
				break;
			}
		}
		return builder.toString();
		
	}
	
	
}
