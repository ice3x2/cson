package com.snoworca.cson.object;


import com.snoworca.cson.CSONElement;
import com.snoworca.cson.CSONObject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;

public class TypeElement {

    private final Class<?> type;
    private final Constructor<?> constructor;

    private final SchemaObjectNode tree;


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
    protected CSONObject serialize(final Object typeObject,CSONObject origin) {
        if(typeObject.getClass() != this.type) {
            throw new CSONObjectException("Type mismatch error. " + this.type.getName() + "!=" + typeObject.getClass().getName());
        }
        else if(typeObject == null) {
            return null;
        }

        HashMap<Integer, Object> parentObjMap = new HashMap<>();
        HashMap<Class<?>, Object> parentsObjMap = new HashMap<>();
        int currentFieldRackId = 0;



        CSONObject csonObject = origin == null? new CSONObject() : origin;
        SchemaObjectNode treeObject = tree;
        Iterator<String> iter = tree.keySet().iterator();
        ArrayDeque<Iterator<String>> iterators = new ArrayDeque<>();
        ArrayDeque<CSONElement> resultElements = new ArrayDeque<>();
        ArrayDeque<SchemaNode> treeElements = new ArrayDeque<>();
        ArrayDeque<Object> parents = new ArrayDeque<>();
        ArrayDeque<Object> typeObjects = new ArrayDeque<>();
        iterators.add(iter);
        //treeElements.add(tree);
        typeObjects.add(typeObject);
        resultElements.add(csonObject);
        parents.add(typeObject);
        FieldRack lastParentFieldRack = null;
        while(iter.hasNext()) {
            String key = iter.next();
            SchemaNode node = treeObject.get(key);
            if(node instanceof SchemaArrayNode) {}//csonObject.put(key, ((CSONArray)node).clone());
            else if(node instanceof SchemaObjectNode) {
                CSONObject childObject = csonObject.optObject(key);
                if(childObject == null) {
                    childObject = new CSONObject();
                    csonObject.put(key, childObject);
                }
                csonObject = childObject;
                treeObject = (SchemaObjectNode)node;
                iter = treeObject.keySet().iterator();
                iterators.add(iter);
                resultElements.add(csonObject);
                List<FieldRack> parentFieldRack = treeObject.getParentFieldRackList();
                for(FieldRack fieldRack : parentFieldRack) {
                    int id = fieldRack.getId();
                    if(parentObjMap.containsKey(id)) {
                        continue;
                    }
                    FieldRack grandFieldRack = fieldRack.getParentFieldRack();
                    if (grandFieldRack == null) {
                        Object obj = fieldRack.getValue(typeObject);
                        parentObjMap.put(id, obj);
                    }
                    else {
                        Object grandObj = parentObjMap.get(grandFieldRack.getId());
                        Object obj = fieldRack.getValue(grandObj);
                        parentObjMap.put(id, obj);
                    }
                }
                treeElements.add(treeObject);
            }
            else if(node instanceof FieldRack) {
                FieldRack fieldRack = (FieldRack)node;
                Object parent =obtainParentObjects(parentObjMap, fieldRack, typeObject);
                Object value = fieldRack.getValue(parent);
                csonObject.put(key, value);
            }
            else csonObject.put(key, node);
            if(!iter.hasNext() && !iterators.isEmpty()) {
                iter = iterators.peekFirst();
                treeObject = (SchemaObjectNode)treeElements.peekFirst();
                csonObject = (CSONObject) resultElements.peekFirst();
            }
        }
        return csonObject;
    }



    private Object obtainParentObjects(Map<Integer, Object> parentsMap, FieldRack fieldRack, Object rootObject) {
        FieldRack parentFieldRack = fieldRack.getParentFieldRack();
        if(parentFieldRack == null) {
            return rootObject;
        }
        int parentId = parentFieldRack.getId();
        Object parent = parentsMap.get(parentId);
        if(parent != null) {
            return parent;
        }
        FieldRack grandParentFieldRack = parentFieldRack.getParentFieldRack();
        if(grandParentFieldRack == null) {
            parent = parentFieldRack.getValue(rootObject);
        }
        else {
            parent = obtainParentObjects(parentsMap, parentFieldRack, rootObject);
            parentsMap.put(parentId, parent);
        }
        return parent;
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



    private SchemaObjectNode init() {
        return makeTree(null);
    }

    protected SchemaObjectNode makeTree(FieldRack parentFieldRack) {
        List<FieldRack> fieldRacks = searchAllCSONValueFields(type);
        SchemaObjectNode objectNode = new SchemaObjectNode();
        NodePath nodePath = new NodePath(objectNode);
        for(FieldRack fieldRack : fieldRacks) {
            fieldRack.setParentFiledRack(parentFieldRack);
            if(fieldRack.getType() == Types.Object) {
                TypeElement typeElement = TypeElements.getInstance().getTypeInfo(fieldRack.getFieldType());
                SchemaObjectNode childTree = typeElement.makeTree(fieldRack);
                childTree.addParentFieldRack(fieldRack);
                nodePath.put(fieldRack.getPath(),childTree);
                continue;
            }
            nodePath.put(fieldRack.getPath(),fieldRack);
        }
        return objectNode;
    }


    /*private void findAndSetParentFieldRackIfFieldRackValue(CSONObject csonObject,FieldRack parent) {
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
    }*/



}
