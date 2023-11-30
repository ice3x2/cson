package com.snoworca.cson.serializer;

import com.snoworca.cson.CSONArray;
import com.snoworca.cson.CSONElement;
import com.snoworca.cson.CSONObject;

public class Utils {

    static Class<?> primitiveTypeToBoxedType(Class<?> primitiveType) {
        if (primitiveType == int.class) {
            return Integer.class;
        } else if (primitiveType == long.class) {
            return Long.class;
        } else if (primitiveType == float.class) {
            return Float.class;
        } else if (primitiveType == double.class) {
            return Double.class;
        } else if (primitiveType == boolean.class) {
            return Boolean.class;
        } else if (primitiveType == char.class) {
            return Character.class;
        } else if (primitiveType == byte.class) {
            return Byte.class;
        } else if (primitiveType == short.class) {
            return Short.class;
        } else if (primitiveType == void.class) {
            return Void.class;
        } else {
            return primitiveType;
        }
    }

    static Object optFrom(CSONElement cson, Object key, Types valueType) {



        boolean isArrayType = cson instanceof CSONArray;
        if(isArrayType && ((CSONArray)cson).isNull((int)key)) {
            return null;
        } else if(!isArrayType && ((CSONObject)cson).isNull((String)key)) {
            return null;
        }
        if(Types.Boolean == valueType) {
            return isArrayType ? ((CSONArray) cson).optBoolean((int)key) : ((CSONObject)cson).optBoolean((String)key);
        } else if(Types.Byte == valueType) {
            return  isArrayType ? ((CSONArray) cson).optByte((int)key) : ((CSONObject)cson).optByte((String)key);
        } else if(Types.Character == valueType) {
            return  isArrayType ? ((CSONArray) cson).optChar((int)key, '\0') : ((CSONObject)cson).optChar((String)key, '\0');
        } else if(Types.Short == valueType) {
            return  isArrayType ? ((CSONArray) cson).optShort((int)key) : ((CSONObject)cson).optShort((String)key);
        } else if(Types.Integer == valueType) {
            return  isArrayType ? ((CSONArray) cson).optInt((int)key) : ((CSONObject)cson).optInt((String)key);
        } else if(Types.Float == valueType) {
            return  isArrayType ? ((CSONArray) cson).optFloat((int)key) : ((CSONObject)cson).optFloat((String)key);
        } else if(Types.Double == valueType) {
            return  isArrayType ? ((CSONArray) cson).optDouble((int)key) : ((CSONObject)cson).optDouble((String)key);
        } else if(Types.String == valueType) {
            return  isArrayType ? ((CSONArray) cson).optString((int)key) : ((CSONObject)cson).optString((String)key);
        }  else if(Types.ByteArray == valueType) {
            return  isArrayType ? ((CSONArray) cson).optByteArray((int)key) : ((CSONObject)cson).optByteArray((String)key);
        }
        return null;

    }

}
