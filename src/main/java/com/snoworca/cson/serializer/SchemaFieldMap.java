package com.snoworca.cson.serializer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class SchemaFieldMap extends SchemaField {

    private final Constructor<?> constructorMap;
    SchemaFieldMap(TypeElement parentsTypeElement, Field field, String path) {
        super(parentsTypeElement, field, path);

        Map.Entry<Class<?>, Class<?>> entry = readKeyValueGenericType(field);
        Class<?> keyClass = entry.getKey();
        Class<?> valueClass = entry.getValue();

        if(!String.class.isAssignableFrom(keyClass)) {
            throw new CSONObjectException("Map field '" + field.getDeclaringClass() + "."  + field.getName() + "' is not String key. Please use String key.");
        }




        constructorMap = constructorOfMap(field.getType());
    }

    Map.Entry<Class<?>, Class<?>> readKeyValueGenericType(Field field) {
        Type genericFieldType = field.getGenericType();
        if (genericFieldType instanceof java.lang.reflect.ParameterizedType) {
            java.lang.reflect.ParameterizedType aType = (java.lang.reflect.ParameterizedType) genericFieldType;
            Type[] fieldArgTypes = aType.getActualTypeArguments();
            if(fieldArgTypes.length != 2) {
                throw new CSONObjectException("Map field '" + field.getDeclaringClass() + "."  + field.getName() + "' is Raw type. Please use generic type.");
            }
            if(fieldArgTypes[0] instanceof Class<?> && fieldArgTypes[1] instanceof Class<?>) {
                return new AbstractMap.SimpleEntry<>((Class<?>)fieldArgTypes[0], (Class<?>)fieldArgTypes[1]);
            }
            else {
                throw new CSONObjectException("Map field '" + field.getDeclaringClass() + "."  + field.getName() + "' is Raw type. Please use generic type.");
            }
        } else  {
            throw new CSONObjectException("Map field '" + field.getDeclaringClass() + "."  + field.getName() + "' is Raw type. Please use generic type.");
        }
    }


    @SuppressWarnings("unchecked")
    private static Constructor<?> constructorOfMap(Class<?> type) {
        try {
            if (type.isInterface() && Map.class.isAssignableFrom(type)) {
                return HashMap.class.getConstructor();
            } else if(type.isInterface() && SortedMap.class.isAssignableFrom(type)) {
                return TreeMap.class.getConstructor();
            } else if(type.isInterface() && NavigableMap.class.isAssignableFrom(type)) {
                return TreeMap.class.getConstructor();
            } else if(type.isInterface() && ConcurrentMap.class.isAssignableFrom(type)) {
                return ConcurrentHashMap.class.getConstructor();
            } else if(type.isInterface() && ConcurrentNavigableMap.class.isAssignableFrom(type)) {
                return ConcurrentSkipListMap.class.getConstructor();
            }
            return type.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new CSONObjectException("Map type " + type.getName() + " has no default constructor.");
        }



    }


    @Override
    public SchemaNode copyNode() {
        return null;
    }
}
