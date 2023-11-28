package com.snoworca.cson.serializer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;

public class SchemaFieldMap extends SchemaField {
    SchemaFieldMap(TypeElement parentsTypeElement, Field field, String path) {
        super(parentsTypeElement, field, path);
    }



    private static Constructor<?> constructorOfMap(Class<?> type) {
        try {
            if (type.isInterface() && Map.class.isAssignableFrom(type)) {
                return HashMap.class.getConstructor();
            } else if(type.isInterface())

        } catch (NoSuchMethodException e) {
            throw new CSONObjectException("Map type " + type.getName() + " has no default constructor.");
        }



    }


    @Override
    public SchemaNode copyNode() {
        return null;
    }
}
