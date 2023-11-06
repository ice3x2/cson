package com.snoworca.cson.object;

import com.snoworca.cson.CSONElement;
import com.snoworca.cson.CSONObject;

import java.util.*;

public class CSONSerializer {

    private CSONSerializer() {}

    public static CSONObject serialize(Object obj) {
        Class<?> clazz = obj.getClass();
        TypeElement typeElement = TypeElements.getInstance().getTypeInfo(clazz);
        return serializeTypeElement(typeElement,obj);
    }

    private static CSONObject serializeTypeElement(TypeElement typeElement, final Object typeObject) {
        Class<?> type = typeElement.getType();
        if(typeObject.getClass() != type) {
            throw new CSONObjectException("Type mismatch error. " + type.getName() + "!=" + typeObject.getClass().getName());
        }
        else if(typeObject == null) {
            return null;
        }
        SchemaObjectNode schema = typeElement.getSchema();

        HashMap<Integer, Object> parentObjMap = new HashMap<>();
        CSONObject csonObject = new CSONObject();
        CSONObject root = csonObject;
        ArrayDeque<DequeueItem> dequeueItems = new ArrayDeque<>();
        Iterator<String> iter = schema.keySet().iterator();
        SchemaObjectNode schemaNode = schema;
        DequeueItem currentDequeueItem = new DequeueItem(iter, schemaNode, csonObject);
        dequeueItems.add(currentDequeueItem);


        while(iter.hasNext()) {
            String key = iter.next();

            SchemaNode node = schemaNode.get(key);
            if(node instanceof SchemaArrayNode) {}//csonObject.put(key, ((CSONArray)node).clone());
            else if(node instanceof SchemaObjectNode) {
                CSONObject childObject = csonObject.optObject(key);
                if(childObject == null) {
                    childObject = new CSONObject();
                    csonObject.put(key, childObject);
                }
                csonObject = childObject;
                schemaNode = (SchemaObjectNode)node;
                iter = schemaNode.keySet().iterator();

                List<FieldRack> parentFieldRack = schemaNode.getParentFieldRackList();
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
                dequeueItems.add(new DequeueItem(iter, schemaNode, csonObject));
            }
            else if(node instanceof FieldRack) {
                FieldRack fieldRack = (FieldRack)node;
                Object parent = obtainParentObjects(parentObjMap, fieldRack, typeObject);
                Object value = fieldRack.getValue(parent);
                csonObject.put(key, value);
            }
            while(!iter.hasNext() && !dequeueItems.isEmpty()) {
                DequeueItem dequeueItem = dequeueItems.getFirst();
                iter = dequeueItem.keyIterator;
                schemaNode = (SchemaObjectNode)dequeueItem.schemaNode;
                csonObject = (CSONObject)dequeueItem.resultElement;
                if(!iter.hasNext() && !dequeueItems.isEmpty()) {
                    dequeueItems.removeFirst();
                }
            }

        }
        return root;
    }

    private static Object obtainParentObjects(Map<Integer, Object> parentsMap, FieldRack fieldRack, Object rootObject) {
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


    public static Object deserialize(CSONObject csonObject) {
        return null;
    }



    private static class DequeueItem {
        Iterator<String> keyIterator;
        SchemaNode schemaNode;
        CSONElement resultElement;

        private DequeueItem(Iterator<String> keyIterator, SchemaNode schemaNode, CSONElement resultElement) {
            this.keyIterator = keyIterator;
            this.schemaNode = schemaNode;
            this.resultElement = resultElement;
        }
    }




}
