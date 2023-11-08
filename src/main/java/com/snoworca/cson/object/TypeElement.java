package com.snoworca.cson.object;


import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;

class TypeElement {

    private final Class<?> type;
    private final Constructor<?> constructor;

    private SchemaObjectNode schema;


    protected SchemaObjectNode getSchema() {
        if(schema == null) {
            schema = NodePath.makeSchema(this,null);
            System.out.println(schema);
        }
        return schema;
    }

    protected static TypeElement create(Class<?> type) {
        checkCSONAnnotation(type);
        checkConstructor(type);
        Constructor<?> constructor = null;
        try {
            constructor = type.getDeclaredConstructor();
        } catch (NoSuchMethodException ignored) {}
        //noinspection DataFlowIssue
        constructor.setAccessible(true);
        return new TypeElement(type, constructor);
    }

    private TypeElement(Class<?> type, Constructor<?> constructor) {
        this.type = type;
        this.constructor = constructor;
        //this.schema = NodePath.makeSchema(this,null);
        //System.out.println(this.schema.toString());
    }

    protected Class<?> getType() {
        return type;
    }



    private static void checkCSONAnnotation(Class<?> type) {
         Annotation a = type.getAnnotation(CSON.class);
         if(a == null) {
             throw new CSONObjectException("Type " + type.getName() + " is not annotated with @CSON");
         }
    }

    private static void checkConstructor(Class<?> type) {
        Constructor<?> constructor = null;
        try {
            constructor = type.getDeclaredConstructor();
            if(constructor == null) {
                throw new CSONObjectException("Type " + type.getName() + " has no constructor");
            }
        } catch (NoSuchMethodException e) {
            throw new CSONObjectException("Type " + type.getName() + " has invalid constructor");
        }

    }





}
