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

    protected Class<?> getType() {
        return type;
    }

    protected CSONObject serialize(Object typeObject) {
        return serialize(typeObject,null);
    }
    protected CSONObject serialize(Object typeObject,CSONObject origin) {
        if(typeObject.getClass() != this.type) {
            throw new CSONObjectException("Type mismatch error. " + this.type.getName() + "!=" + typeObject.getClass().getName());
        }
        else if(typeObject == null) {
            return null;
        }

        CSONObject csonObject = origin == null? new CSONObject() : origin;
        CSONObject treeObject = tree;
        Iterator<String> iter = tree.keySet().iterator();
        ArrayDeque<Iterator<String>> iterators = new ArrayDeque<>();
        ArrayDeque<CSONElement> resultElements = new ArrayDeque<>();
        ArrayDeque<CSONElement> treeElements = new ArrayDeque<>();
        ArrayDeque<Object> parents = new ArrayDeque<>();
        ArrayDeque<Object> typeObjects = new ArrayDeque<>();
        iterators.add(iter);
        treeElements.add(tree);
        typeObjects.add(typeObject);
        resultElements.add(csonObject);
        parents.add(typeObject);
        FieldRack lastParentFieldRack = null;
        while(iter.hasNext()) {
            String key = iter.next();
            Object treeObj = treeObject.get(key);
            if(treeObj instanceof CSONArray) csonObject.put(key, ((CSONArray)treeObj).clone());
            else if(treeObj instanceof CSONObject) {
                CSONObject childObject = csonObject.optObject(key);
                if(childObject == null) {
                    childObject = new CSONObject();
                    csonObject.put(key, childObject);
                }
                csonObject = childObject;
                treeObject = (CSONObject)treeObj;
                iter = treeObject.keySet().iterator();
                iterators.add(iter);
                resultElements.add(csonObject);
                treeElements.add(treeObject);
            }
            else if(treeObj instanceof FieldRack) {
                FieldRack fieldRack = (FieldRack)treeObj;
                FieldRack parentFieldRack  = fieldRack.getParentFieldRack();
                if(parentFieldRack != null && parentFieldRack != lastParentFieldRack) {
                    typeObject = parentFieldRack.getValue(typeObject);
                    lastParentFieldRack = parentFieldRack;
                    parents.add(typeObject);
                } else if(parentFieldRack != lastParentFieldRack){
                    typeObject = parents.peekFirst();
                }
                Object value = fieldRack.getValue(typeObject);
                csonObject.put(key, value);

            }
            else csonObject.put(key, treeObj);
            if(!iter.hasNext() && !iterators.isEmpty()) {
                iter = iterators.peekFirst();
                treeObject = (CSONObject) treeElements.peekFirst();
                csonObject = (CSONObject) resultElements.peekFirst();
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
            if(fieldRack.getType() == Types.Object) {
                TypeElement typeElement = TypeElements.getInstance().getTypeInfo(fieldRack.getFieldType());
                CSONTypeObject childTree = (CSONTypeObject) typeElement.tree.clone();
                findAndSetParentFieldRackIfFieldRackValue(childTree,fieldRack);
                csonPath.put(fieldRack.getPath(),childTree);
                continue;
            }
            csonPath.put(fieldRack.getPath(),fieldRack.copy());
        }
        return csonTypeObject;
    }


    private void findAndSetParentFieldRackIfFieldRackValue(CSONObject csonObject,FieldRack parent) {
        for(String key : csonObject.keySet()) {
            Object element = csonObject.get(key);
            if(element instanceof CSONObject) {
                findAndSetParentFieldRackIfFieldRackValue((CSONObject)element,parent);
            }
            else if(element instanceof FieldRack) {
                FieldRack fieldRack = (FieldRack)element;
                fieldRack.setParentFiledRack(parent);

            }
        }
    }



}
