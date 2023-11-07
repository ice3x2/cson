package com.snoworca.cson.object;


import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;

class TypeElement {

    private final Class<?> type;
    private final Constructor<?> constructor;

    private final SchemaObjectNode schema;


    protected SchemaObjectNode getSchema() {
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
        this.schema = init();
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


    private List<SchemaField> searchAllCSONValueFields(Class<?> clazz) {
        Set<String> fieldPaths = new HashSet<>();
        List<SchemaField> results = new ArrayList<>();
        Class<?> currentClass = clazz;
        while(currentClass != Object.class) {
            for(Field field : clazz.getDeclaredFields()) {
                SchemaField fieldRack = SchemaField.of(this,field);
                if(fieldRack != null && !fieldPaths.contains(fieldRack.getPath())) {
                    // 동일한 path 가 있으면 거른다.
                    fieldPaths.add(fieldRack.getPath());
                    results.add(fieldRack);
                }
            }
            currentClass = currentClass.getSuperclass();
        }
        return results;
    }



    private SchemaObjectNode init() {
        return makeSchema(null);
    }

    protected SchemaObjectNode makeSchema(SchemaField parentFieldRack) {
        List<SchemaField> fieldRacks = searchAllCSONValueFields(type);
        SchemaObjectNode objectNode = new SchemaObjectNode();
        NodePath nodePath = new NodePath(objectNode);
        for(SchemaField fieldRack : fieldRacks) {
            fieldRack.setParentFiled(parentFieldRack);
            if(fieldRack.getType() == Types.Object) {
                TypeElement typeElement = TypeElements.getInstance().getTypeInfo(fieldRack.getFieldType());
                SchemaObjectNode childTree = typeElement.makeSchema(fieldRack);
                childTree.addParentFieldRack(fieldRack);
                nodePath.put(fieldRack.getPath(),childTree);

                continue;
            }
            nodePath.put(fieldRack.getPath(),fieldRack);
        }
        return objectNode;
    }




}
