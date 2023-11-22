package com.snoworca.cson.serializer;

enum Types {
    Byte,
    Short,
    Integer,
    Long,
    Float,
    Double,
    Boolean,
    Character,
    String,
    ByteArray,
    BYTEArray,
    Object,
    Collection;




    public static Types of(Class<?> type) {
        if(type == byte.class || type == Byte.class) {
            return Byte;
        } else if(type == short.class || type == Short.class) {
            return Short;
        } else if(type == int.class || type == Integer.class) {
            return Integer;
        } else if(type == long.class || type == Long.class) {
            return Long;
        } else if(type == float.class || type == Float.class) {
            return Float;
        } else if(type == double.class || type == Double.class) {
            return Double;
        } else if(type == boolean.class || type == Boolean.class) {
            return Boolean;
        } else if(type == char.class || type == Character.class) {
            return Character;
        } else if(type == String.class) {
            return String;
        } else if(type == byte[].class ) {
            return ByteArray;
        } else if(type == Byte[].class ) {
            return BYTEArray;
        } else if(java.util.Collection.class.isAssignableFrom(type)) {
            return Collection;
        } else {
            return Object;
        }
    }

}
