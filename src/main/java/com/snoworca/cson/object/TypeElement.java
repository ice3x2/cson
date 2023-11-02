package com.snoworca.cson.object;

import com.snoworca.cson.CSONArray;
import com.snoworca.cson.CSONElement;
import com.snoworca.cson.CSONObject;
import com.snoworca.cson.CSONPath;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;

public class TypeElement {

    private final Class<?> type;
    private final Constructor<?> constructor;

    private final CSONTypeObject tree;


    public static TypeElement create(Class<?> type) {
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
        this.tree = init();
    }

    protected CSONObject serialize(Object typeObject) {
        if(typeObject.getClass() != this.type) {
            throw new CSONObjectException("Type mismatch error. " + this.type.getName() + "!=" + typeObject.getClass().getName());
        }
        CSONObject csonObject = new CSONObject();
        Iterator<Map.Entry<String, Object>> iter = tree.toMap().entrySet().iterator();
        ArrayDeque<Iterator<Map.Entry<String, Object>>> iterators = new ArrayDeque<>();
        ArrayDeque<CSONElement> csonElements = new ArrayDeque<>();
        iterators.add(iter);
        while(iter.hasNext()) {
            Map.Entry<String, Object> entry = iter.next();
            String key = entry.getKey();
            Object obj = entry.getValue();
            if(obj instanceof CSONArray) csonObject.put(key, ((CSONArray)obj).clone());
            else if(obj instanceof CSONObject) {
                CSONObject childObject = (CSONObject)obj;
                iter = childObject.toMap().entrySet().iterator();
                iterators.add(iter);
                csonElements.add(csonObject);
                csonObject = childObject;
            }
            else if(obj instanceof FieldRack) {
                FieldRack fieldRack = (FieldRack)obj;
                Object value = fieldRack.getValue(typeObject);
                csonObject.put(key, value);
            }
            else csonObject.put(key, obj);
            if(!iter.hasNext() && !iterators.isEmpty()) {
                iter = iterators.peekFirst();
            }
        }
        return csonObject;

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


    private List<FieldRack> searchAllCSONValueFields(Class<?> clazz) {
        Set<String> fieldPaths = new HashSet<>();
        List<FieldRack> results = new ArrayList<>();
        Class<?> currentClass = clazz;
        while(currentClass != Object.class) {
            for(Field field : clazz.getDeclaredFields()) {
                FieldRack fieldRack = FieldRack.of(this,field);
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



    private CSONTypeObject init() {
        List<FieldRack> fieldRacks = searchAllCSONValueFields(type);
        CSONTypeObject csonTypeObject = new CSONTypeObject();
        CSONPath csonPath = csonTypeObject.getCsonPath();
        for(FieldRack fieldRack : fieldRacks) {
            csonPath.put(fieldRack.getPath(),fieldRack);
        }
        return csonTypeObject;
    }





}
