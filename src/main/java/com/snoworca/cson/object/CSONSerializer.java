package com.snoworca.cson.object;

import com.snoworca.cson.CSONArray;
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

    private static CSONObject serializeTypeElement(TypeElement typeElement, final Object rootObject) {
        Class<?> type = typeElement.getType();
        if(rootObject.getClass() != type) {
            throw new CSONObjectException("Type mismatch error. " + type.getName() + "!=" + rootObject.getClass().getName());
        }
        else if(rootObject == null) {
            return null;
        }
        SchemaObjectNode schemaRoot = typeElement.getSchema();

        HashMap<Integer, Object> parentObjMap = new HashMap<>();
        CSONObject csonObject = new CSONObject();
        CSONObject root = csonObject;
        ArrayDeque<ObjectSerializeDequeueItem> objectSerializeDequeueItems = new ArrayDeque<>();
        Iterator<String> iter = schemaRoot.keySet().iterator();
        SchemaObjectNode schemaNode = schemaRoot;
        ObjectSerializeDequeueItem currentObjectSerializeDequeueItem = new ObjectSerializeDequeueItem(iter, schemaNode, csonObject);
        objectSerializeDequeueItems.add(currentObjectSerializeDequeueItem);

        while(iter.hasNext()) {
            String key = iter.next();
            SchemaNode node = schemaNode.get(key);
            if(node instanceof SchemaArrayNode) {}//csonObject.put(key, ((CSONArray)node).clone());
            else if(node instanceof SchemaObjectNode) {
                schemaNode = (SchemaObjectNode)node;
                iter = schemaNode.keySet().iterator();
                List<SchemaField> parentFieldRack = schemaNode.getParentSchemaFieldList();
                int nullCount = parentFieldRack.size();
                for(SchemaField fieldRack : parentFieldRack) {
                    int id = fieldRack.getId();
                    if(parentObjMap.containsKey(id)) {
                        continue;
                    }
                    SchemaField grandFieldRack = fieldRack.getParentField();
                    if (grandFieldRack == null) {
                        Object obj = fieldRack.getValue(rootObject);
                        if(obj != null) {
                            parentObjMap.put(id, obj);
                            nullCount--;
                        }
                    }
                    else {
                        Object grandObj = parentObjMap.get(grandFieldRack.getId());
                        if(grandObj == null) {

                        } else {
                            Object obj = fieldRack.getValue(grandObj);
                            if(obj != null) {
                                parentObjMap.put(id, obj);
                                nullCount--;
                            }
                        }
                    }
                }

                if(!schemaNode.isBranchNode() && nullCount > 0) {
                    csonObject.put(key,null);
                    while (iter.hasNext())  {
                        iter.next();
                    }
                } else {
                    CSONObject childObject = csonObject.optObject(key);
                    if(childObject == null) {
                        childObject = new CSONObject();
                        csonObject.put(key, childObject);
                        csonObject = childObject;
                    }
                    objectSerializeDequeueItems.add(new ObjectSerializeDequeueItem(iter, schemaNode, csonObject));
                }
            }
            else if(node instanceof SchemaFieldNormal) {
                SchemaFieldNormal fieldRack = (SchemaFieldNormal)node;
                Object parent = obtainParentObjects(parentObjMap, fieldRack, rootObject);
                if(parent != null) {
                    Object value = fieldRack.getValue(parent);
                    csonObject.put(key, value);
                }
            } else if(node instanceof SchemaFieldArray) {
                SchemaFieldArray schemaFieldArray = (SchemaFieldArray)node;
                Object parent = obtainParentObjects(parentObjMap, schemaFieldArray, rootObject);
                if(parent != null) {
                    Object value = schemaFieldArray.getValue(parent);
                    CSONArray csonArray = collectionObjectToCSONArray((Collection<?>)value, schemaFieldArray);
                    csonObject.put(key, csonArray);
                }
            }
            while(!iter.hasNext() && !objectSerializeDequeueItems.isEmpty()) {
                ObjectSerializeDequeueItem objectSerializeDequeueItem = objectSerializeDequeueItems.getFirst();
                iter = objectSerializeDequeueItem.keyIterator;
                schemaNode = (SchemaObjectNode) objectSerializeDequeueItem.schemaNode;
                csonObject = (CSONObject) objectSerializeDequeueItem.resultElement;
                if(!iter.hasNext() && !objectSerializeDequeueItems.isEmpty()) {
                    objectSerializeDequeueItems.removeFirst();
                }
            }
        }
        return root;
    }



    private static CSONArray collectionObjectToCSONArray(Collection<?> collection, SchemaFieldArray schemaFieldArray) {
        CSONArray resultCsonArray  = new CSONArray();
        CSONArray csonArray = resultCsonArray;
        Iterator<?> iter = collection.iterator();
        TypeElement objectValueTypeElement = schemaFieldArray.getObjectValueTypeElement();
        Deque<ArraySerializeDequeueItem> arraySerializeDequeueItems = new ArrayDeque<>();
        ArraySerializeDequeueItem currentArraySerializeDequeueItem = new ArraySerializeDequeueItem(iter, csonArray);
        arraySerializeDequeueItems.add(currentArraySerializeDequeueItem);
        while(iter.hasNext()) {
            Object object = iter.next();
            if(object instanceof Collection<?>) {
                CSONArray childArray = new CSONArray();
                csonArray.add(childArray);
                csonArray = childArray;
                iter = ((Collection<?>)object).iterator();
                currentArraySerializeDequeueItem = new ArraySerializeDequeueItem(iter, csonArray);
                arraySerializeDequeueItems.add(currentArraySerializeDequeueItem);
            } else if(objectValueTypeElement == null) {
                csonArray.add(object);
            } else {
                CSONObject childObject = serializeTypeElement(objectValueTypeElement, object);
                csonArray.add(childObject);
            }
            while(!iter.hasNext() && !arraySerializeDequeueItems.isEmpty()) {
                ArraySerializeDequeueItem arraySerializeDequeueItem = arraySerializeDequeueItems.getFirst();
                iter = arraySerializeDequeueItem.iterator;
                csonArray = arraySerializeDequeueItem.resultArray;
                if(!iter.hasNext() && !arraySerializeDequeueItems.isEmpty()) {
                    arraySerializeDequeueItems.removeFirst();
                }
            }
        }
        return resultCsonArray;

    }


    private static Object obtainParentObjects(Map<Integer, Object> parentsMap, SchemaField fieldRack, Object rootObject) {
        SchemaField parentFieldRack = fieldRack.getParentField();
        if(parentFieldRack == null) {
            return rootObject;
        }
        int parentId = parentFieldRack.getId();
        Object parent = parentsMap.get(parentId);
        return parent;

        /*if(parent != null) {
            return parent;
        }
        SchemaField grandParentFieldRack = parentFieldRack.getParentField();
        if(grandParentFieldRack == null) {
            parent = parentFieldRack.getValue(rootObject);
        }
        else {
            parent = obtainParentObjects(parentsMap, parentFieldRack, rootObject);
            parentsMap.put(parentId, parent);
        }
        return parent;*/
    }


    public static Object deserialize(CSONObject csonObject) {
        return null;
    }



    private static class ArraySerializeDequeueItem {
        Iterator<?> iterator;
        CSONArray resultArray;

        private ArraySerializeDequeueItem(Iterator<?> iterator,CSONArray resultArray) {
            this.iterator = iterator;
            this.resultArray = resultArray;
        }
    }

    private static class ObjectSerializeDequeueItem {
        Iterator<String> keyIterator;
        SchemaNode schemaNode;
        CSONElement resultElement;

        private ObjectSerializeDequeueItem(Iterator<String> keyIterator, SchemaNode schemaNode, CSONElement resultElement) {
            this.keyIterator = keyIterator;
            this.schemaNode = schemaNode;
            this.resultElement = resultElement;
        }
    }




}
