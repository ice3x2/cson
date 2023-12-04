package com.snoworca.cson.serializer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class SchemaFieldMap extends SchemaField {

    private final Constructor<?> constructorMap;
    private final Class<?> elementClass;
    SchemaFieldMap(TypeElement parentsTypeElement, Field field, String path) {
        super(parentsTypeElement, field, path);

        Map.Entry<Class<?>, Class<?>> entry = readKeyValueGenericType(field);
        Class<?> keyClass = entry.getKey();
        this.elementClass = entry.getValue();
        if(elementClass != null) {
            assertValueType(elementClass, field.getDeclaringClass().getName() + "." + field.getName());
        }
        assertCollectionOrMapValue(elementClass);



        if(!String.class.isAssignableFrom(keyClass)) {
            throw new CSONObjectException("Map field '" + field.getDeclaringClass() + "."  + field.getName() + "' is not String key. Please use String key.");
        }
        constructorMap = constructorOfMap(field.getType());
    }








    private void assertCollectionOrMapValue(Class<?> type) {
        if(type == null) return;
        if(Map.class.isAssignableFrom(type)) {
            throw new CSONObjectException("The java.util.Map type cannot be directly used as a value element of a Map. Please create a class that wraps your Map and use it as a value element of the Map of field. (Field path: " + field.getDeclaringClass().getName() + "." + field.getName() + ")");
        } else if(Collection.class.isAssignableFrom(type)) {
            throw new CSONObjectException("The java.util.Collection type cannot be directly used as a value element of a Map. Please create a class that wraps your Collection and use it as a value element of the Map  of field. (Field path: " + field.getDeclaringClass().getName() + "." + field.getName() + ")");
        }
    }

    Class<?> getElementType() {
        return elementClass;
    }

    @Override
    Object newInstance() {
        try {
            return constructorMap.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new CSONObjectException("Map type " + field.getDeclaringClass().getName() + "." + field.getType().getName() + " has no default constructor.", e);
        }
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
            } else if(fieldArgTypes[1] instanceof  java.lang.reflect.ParameterizedType) {
                return new AbstractMap.SimpleEntry<>((Class<?>)fieldArgTypes[0], (Class<?>)((java.lang.reflect.ParameterizedType)fieldArgTypes[1]).getRawType());
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
        return new SchemaFieldMap(parentsTypeElement, field, path);
    }
}
