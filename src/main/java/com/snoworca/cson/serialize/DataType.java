package com.snoworca.cson.serialize;

import java.util.Collection;
import java.util.Map;

public class DataType {
    public final static byte TYPE_UNKNOWN = -1;

    public final static byte TYPE_NULL = 0;

    public final static byte TYPE_BYTE = 1;
    public final static byte TYPE_SHORT = 2;
    public final static byte TYPE_CHAR = 3;
    public final static byte TYPE_INT = 4;
    public final static byte TYPE_FLOAT = 5;
    public final static byte TYPE_LONG = 6;
    public final static byte TYPE_DOUBLE = 7;
    public final static byte TYPE_BOOLEAN = 8;
    public final static byte TYPE_STRING = 9;
    public final static byte TYPE_ARRAY = 12;
    public final static byte TYPE_COLLECTION = 13;
    public final static byte TYPE_CSON_OBJECT = 31;
    public final static byte TYPE_OBJECT = 30;

    public final static byte TYPE_MAP = 14;

    public static boolean isObjectType(byte type) {
        return type == TYPE_OBJECT || type == TYPE_CSON_OBJECT || type == -1;
    }

    public static byte getDataType(Class<?> type) {
        if(Byte.TYPE.isAssignableFrom(type) || Byte.class.isAssignableFrom(type)) {
            return DataType.TYPE_BYTE;
        }
        else if(Boolean.TYPE.isAssignableFrom(type) || Boolean.class.isAssignableFrom(type)) {
            return DataType.TYPE_BOOLEAN;
        }
        else if(Short.TYPE.isAssignableFrom(type) || Short.class.isAssignableFrom(type)) {
            return DataType.TYPE_SHORT;
        }
        else if(Character.TYPE.isAssignableFrom(type) || Character.class.isAssignableFrom(type)) {
            return DataType.TYPE_CHAR;
        }
        else if(Integer.TYPE.isAssignableFrom(type) || Integer.class.isAssignableFrom(type) ) {
            return DataType.TYPE_INT;
        }
        else if(Float.TYPE.isAssignableFrom(type) || Float.class.isAssignableFrom(type)) {
            return DataType.TYPE_FLOAT;
        }
        else if(Long.TYPE.isAssignableFrom(type) || Long.class.isAssignableFrom(type)) {
            return DataType.TYPE_LONG;
        }
        else if(Double.TYPE.isAssignableFrom(type) || Double.class.isAssignableFrom(type)) {
            return DataType.TYPE_DOUBLE;
        }
        else if(String.class.isAssignableFrom(type)) {
            return DataType.TYPE_STRING;
        }
        else if(type.isArray()) {
            return DataType.TYPE_ARRAY;
        }
        else if(Collection.class.isAssignableFrom(type)) {
            return DataType.TYPE_COLLECTION;
        }
        else if(Map.class.isAssignableFrom(type)) {
            return DataType.TYPE_MAP;
        }
        else if(!type.isInterface() && type.getAnnotation(Cson.class) != null) {
            return DataType.TYPE_CSON_OBJECT;
        }
        return -1;

    }

    public static boolean isNumberType(byte type) {
        return type >= TYPE_BYTE && type <= TYPE_DOUBLE;
    }

    public static int getNumberTypeLength(byte type) {
        switch (type) {
            case TYPE_BYTE:
            case TYPE_BOOLEAN:
                return 1;
            case TYPE_SHORT:
            case TYPE_CHAR:
                return 2;
            case TYPE_INT:
            case TYPE_FLOAT:
                return 4;
            case TYPE_LONG:
            case TYPE_DOUBLE:
                return 8;
            default:
                return -1;
        }
    }

    public static boolean checkNumberTypeLength(byte type,int length) {
        int numLen = getNumberTypeLength(type);
        return numLen == -1 || length == numLen;

    }
}
