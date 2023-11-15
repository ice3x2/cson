package com.snoworca.cson.serializer;

import com.snoworca.cson.CSONArray;
import com.snoworca.cson.CSONElement;
import com.snoworca.cson.CSONObject;

import java.lang.reflect.Type;
import java.util.*;

public class CSONSerializer {

    private CSONSerializer() {}

    public static CSONObject toCSONObject(Object obj) {
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
        CSONElement csonElement = new CSONObject();
        CSONObject root = (CSONObject) csonElement;
        ArrayDeque<ObjectSerializeDequeueItem> objectSerializeDequeueItems = new ArrayDeque<>();
        Iterator<Object> iter = schemaRoot.keySet().iterator();
        SchemaObjectNode schemaNode = schemaRoot;
        ObjectSerializeDequeueItem currentObjectSerializeDequeueItem = new ObjectSerializeDequeueItem(iter, schemaNode, csonElement);
        objectSerializeDequeueItems.add(currentObjectSerializeDequeueItem);

        while(iter.hasNext()) {
            Object key = iter.next();
            SchemaNode node = schemaNode.get(key);
            if(node instanceof SchemaObjectNode) {
                schemaNode = (SchemaObjectNode)node;
                iter = schemaNode.keySet().iterator();
                List<SchemaField> parentschemaField = schemaNode.getParentSchemaFieldList();
                int nullCount = parentschemaField.size();
                for(SchemaField schemaField : parentschemaField) {
                    int id = schemaField.getId();
                    if(parentObjMap.containsKey(id)) {
                        continue;
                    }
                    SchemaField grandschemaField = schemaField.getParentField();
                    if (grandschemaField == null) {
                        Object obj = schemaField.getValue(rootObject);
                        if(obj != null) {
                            parentObjMap.put(id, obj);
                            nullCount--;
                        }
                    }
                    else {
                        Object grandObj = parentObjMap.get(grandschemaField.getId());
                        if(grandObj != null) {
                            Object obj = schemaField.getValue(grandObj);
                            if(obj != null) {
                                parentObjMap.put(id, obj);
                                nullCount--;
                            }
                        }
                    }
                }

                if(!schemaNode.isBranchNode() && nullCount > 0) {
                    if(key instanceof String) {
                        ((CSONObject)csonElement).put((String) key,null);
                    } else {
                        ((CSONArray)csonElement).set((Integer) key,null);
                    }
                    while (iter.hasNext())  {
                        iter.next();
                    }
                } else {
                    if(key instanceof String) {
                        CSONObject currentObject = ((CSONObject)csonElement);
                        CSONElement childElement = currentObject.optObject((String) key);
                        if(childElement == null) {
                            childElement =  (schemaNode instanceof SchemaArrayNode) ? new CSONArray() : new CSONObject();
                            currentObject.put((String) key, childElement);
                            csonElement = childElement;
                        }
                    } else {
                        CSONArray currentObject = ((CSONArray)csonElement);
                        CSONArray currentArray = ((CSONArray)csonElement);
                        CSONElement childElement = (CSONElement) currentArray.opt((Integer) key);
                        if(childElement == null) {
                            childElement =  (schemaNode instanceof SchemaArrayNode) ? new CSONArray() : new CSONObject();
                            currentObject.set((int)key, childElement);
                            csonElement = childElement;
                        }
                    }


                    objectSerializeDequeueItems.add(new ObjectSerializeDequeueItem(iter, schemaNode, csonElement));
                }
            }
            else if(node instanceof SchemaFieldNormal) {
                SchemaFieldNormal schemaField = (SchemaFieldNormal)node;
                Object parent = obtainParentObjects(parentObjMap, schemaField, rootObject);
                if(parent != null) {
                    Object value = schemaField.getValue(parent);
                    if(key instanceof String) {
                        //if(csonElement instanceof CSONArray) {
                        //    ((CSONArray) csonElement).set((int) key, value);
                        //} else {
                            ((CSONObject) csonElement).put((String) key, value);
                        //}


                    }
                    else {
                        ((CSONArray) csonElement).set((int) key, value);
                    }

                }
            } else if(node instanceof SchemaFieldArray) {
                SchemaFieldArray schemaFieldArray = (SchemaFieldArray)node;
                Object parent = obtainParentObjects(parentObjMap, schemaFieldArray, rootObject);
                if(parent != null) {
                    Object value = schemaFieldArray.getValue(parent);
                    CSONArray csonArray = collectionObjectToCSONArray((Collection<?>)value, schemaFieldArray);

                    if(key instanceof String)
                        ((CSONObject)csonElement).put((String) key, csonArray);
                    else
                        ((CSONArray)csonElement).set((int)key, csonArray);

                    //csonElement.put((String) key, csonArray);
                }
            }
            while(!iter.hasNext() && !objectSerializeDequeueItems.isEmpty()) {
                ObjectSerializeDequeueItem objectSerializeDequeueItem = objectSerializeDequeueItems.getFirst();
                iter = objectSerializeDequeueItem.keyIterator;
                schemaNode = (SchemaObjectNode) objectSerializeDequeueItem.schemaNode;
                csonElement = objectSerializeDequeueItem.resultElement;
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


    private static Object obtainParentObjects(Map<Integer, Object> parentsMap, SchemaField schemaField, Object rootObject) {
        SchemaField parentschemaField = schemaField.getParentField();
        if(parentschemaField == null) {
            return rootObject;
        }
        int parentId = parentschemaField.getId();
        Object parent = parentsMap.get(parentId);
        return parent;

        /*if(parent != null) {
            return parent;
        }
        SchemaField grandParentschemaField = parentschemaField.getParentField();
        if(grandParentschemaField == null) {
            parent = parentschemaField.getValue(rootObject);
        }
        else {
            parent = obtainParentObjects(parentsMap, parentschemaField, rootObject);
            parentsMap.put(parentId, parent);
        }
        return parent;*/
    }


    @SuppressWarnings("unchecked")
    public static<T> T fromCSONObject(CSONObject csonObject, Class<T> clazz) {
        TypeElement typeElement = TypeElements.getInstance().getTypeInfo(clazz);
        Object object = typeElement.newInstance();
        return (T) fromCSONObject(csonObject, object);
    }


    public static<T> T fromCSONObject(CSONObject csonObject, T targetObject) {
        TypeElement typeElement = TypeElements.getInstance().getTypeInfo(targetObject.getClass());
        SchemaObjectNode schemaRoot = typeElement.getSchema();
        ArrayDeque<ObjectSerializeDequeueItem> objectSerializeDequeueItems = new ArrayDeque<>();
        Iterator<Object> iter = schemaRoot.keySet().iterator();
        SchemaObjectNode schemaNode = schemaRoot;
        ObjectSerializeDequeueItem currentObjectSerializeDequeueItem = new ObjectSerializeDequeueItem(iter, schemaNode, csonObject);
        objectSerializeDequeueItems.add(currentObjectSerializeDequeueItem);
        while(iter.hasNext()) {
            Object key = iter.next();
            SchemaNode node = schemaNode.get(key);
            if(node instanceof SchemaFieldNormal) {
                SchemaFieldNormal schemaField = (SchemaFieldNormal)node;
                SchemaField parentField = schemaField.getParentField();
                Object parent = null;
                if(parentField == null) {
                    parent = targetObject;
                }
                if(key instanceof String) {
                  setValueTargetFromCSONObject(parent,schemaField, csonObject, (String)key);
                } else if(key instanceof Integer) {
                }
            }
        }
        return targetObject;
    }

    private static void setValueTargetFromCSONObject(Object parents,SchemaFieldNormal schemaField, CSONObject csonObject, String key) {
        Object value = csonObject.opt(key);
        //todo null 값에 대하여 어떻게 할 것인지 고민해봐야함.
        if(value == null) return;
        Types valueType = schemaField.getType();
        if(Types.Boolean == valueType) {
             schemaField.setValue(parents, csonObject.optBoolean(key));
        } else if(Types.Byte == valueType) {
            schemaField.setValue(parents, csonObject.optByte(key));
        } else if(Types.Character == valueType) {
            schemaField.setValue(parents, csonObject.optChar(key, '\0'));
        } else if(Types.Short == valueType) {
            schemaField.setValue(parents, csonObject.optShort(key));
        } else if(Types.Integer == valueType) {
            schemaField.setValue(parents, csonObject.optInt(key));
        } else if(Types.Float == valueType) {
            schemaField.setValue(parents, csonObject.optInt(key));
        } else if(Types.Double == valueType) {
            schemaField.setValue(parents, csonObject.optDouble(key));
        } else if(Types.String == valueType) {
            schemaField.setValue(parents, csonObject.optString(key));
        }
        else {
            try {
                schemaField.setValue(parents, null);
            } catch (Exception ignored) {}
        }






    }

    public static Object fromCSONObject2(CSONObject csonObject, Object targetObject) {
        TypeElement typeElement = TypeElements.getInstance().getTypeInfo(targetObject.getClass());
        SchemaObjectNode schemaRoot = typeElement.getSchema();
        HashMap<Integer, Object> parentObjMap = new HashMap<>();
        CSONObject rootObject = csonObject;
        CSONElement csonElement = csonObject;
        ArrayDeque<ObjectSerializeDequeueItem> objectSerializeDequeueItems = new ArrayDeque<>();
        Iterator<Object> iter = schemaRoot.keySet().iterator();
        SchemaObjectNode schemaNode = schemaRoot;
        ObjectSerializeDequeueItem currentObjectSerializeDequeueItem = new ObjectSerializeDequeueItem(iter, schemaNode, csonObject);
        objectSerializeDequeueItems.add(currentObjectSerializeDequeueItem);

        while(iter.hasNext()) {
            Object key = iter.next();
            SchemaNode node = schemaNode.get(key);
            if(node instanceof SchemaObjectNode) {
                schemaNode = (SchemaObjectNode)node;
                iter = schemaNode.keySet().iterator();
                List<SchemaField> parentschemaField = schemaNode.getParentSchemaFieldList();
                //int nullCount = parentschemaField.size();
                // 부모 object 를 찾는다.
                for(SchemaField schemaField : parentschemaField) {
                    int id = schemaField.getId();
                    if(parentObjMap.containsKey(id)) {
                        continue;
                    }
                    SchemaField grandschemaField = schemaField.getParentField();
                    if (grandschemaField == null) {
                        Object obj = schemaField.getValue(targetObject);
                        if(obj == null) {
                            obj = schemaField.newInstance();
                        }
                        if(obj != null) {
                            parentObjMap.put(id, obj);
                            //nullCount--;
                        }
                    }
                    else {
                        Object grandObj = parentObjMap.get(grandschemaField.getId());
                        if(grandObj == null) {
                            grandObj = grandschemaField.newInstance();
                        }
                        if(grandObj != null) {
                            Object obj = schemaField.getValue(grandObj);
                            if(obj != null) {
                                parentObjMap.put(id, obj);
                                //nullCount--;
                            }
                        }
                    }
                }
                if(!schemaNode.isBranchNode() /*&& nullCount > 0*/) {
                    if(key instanceof String) {
                        //((CSONObject)csonElement).put((String) key,null);
                    } else {
                        //((CSONArray)csonElement).set((Integer) key,null);
                    }
                    while (iter.hasNext())  {
                        iter.next();
                    }
                } else {
                    if(key instanceof String) {
                        CSONObject currentObject = ((CSONObject)csonElement);
                        CSONElement childElement = currentObject.optObject((String) key);
                        if(childElement == null) {
                            childElement =  (schemaNode instanceof SchemaArrayNode) ? new CSONArray() : new CSONObject();
                            currentObject.put((String) key, childElement);
                            csonElement = childElement;
                        }
                    } else {
                        CSONArray currentObject = ((CSONArray)csonElement);
                        CSONArray currentArray = ((CSONArray)csonElement);
                        CSONElement childElement = (CSONElement) currentArray.opt((Integer) key);
                        if(childElement == null) {
                            childElement =  (schemaNode instanceof SchemaArrayNode) ? new CSONArray() : new CSONObject();
                            currentObject.set((int)key, childElement);
                            csonElement = childElement;
                        }
                    }


                    objectSerializeDequeueItems.add(new ObjectSerializeDequeueItem(iter, schemaNode, csonElement));
                }
            }
            else if(node instanceof SchemaFieldNormal) {
                SchemaFieldNormal schemaField = (SchemaFieldNormal)node;
                Object parent = obtainParentObjects(parentObjMap, schemaField, rootObject);
                if(parent != null) {
                    Object value = schemaField.getValue(parent);
                    if(key instanceof String) {
                        //if(csonElement instanceof CSONArray) {
                        //    ((CSONArray) csonElement).set((int) key, value);
                        //} else {
                        ((CSONObject) csonElement).put((String) key, value);
                        //}


                    }
                    else {
                        ((CSONArray) csonElement).set((int) key, value);
                    }

                }
            } else if(node instanceof SchemaFieldArray) {
                SchemaFieldArray schemaFieldArray = (SchemaFieldArray)node;
                Object parent = obtainParentObjects(parentObjMap, schemaFieldArray, rootObject);
                if(parent != null) {
                    Object value = schemaFieldArray.getValue(parent);
                    CSONArray csonArray = collectionObjectToCSONArray((Collection<?>)value, schemaFieldArray);

                    if(key instanceof String)
                        ((CSONObject)csonElement).put((String) key, csonArray);
                    else
                        ((CSONArray)csonElement).set((int)key, csonArray);

                    //csonElement.put((String) key, csonArray);
                }
            }
            while(!iter.hasNext() && !objectSerializeDequeueItems.isEmpty()) {
                ObjectSerializeDequeueItem objectSerializeDequeueItem = objectSerializeDequeueItems.getFirst();
                iter = objectSerializeDequeueItem.keyIterator;
                schemaNode = (SchemaObjectNode) objectSerializeDequeueItem.schemaNode;
                csonElement = objectSerializeDequeueItem.resultElement;
                if(!iter.hasNext() && !objectSerializeDequeueItems.isEmpty()) {
                    objectSerializeDequeueItems.removeFirst();
                }
            }
        }
        return rootObject;



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
        Iterator<Object> keyIterator;
        SchemaNode schemaNode;
        CSONElement resultElement;

        private ObjectSerializeDequeueItem(Iterator<Object> keyIterator, SchemaNode schemaNode, CSONElement resultElement) {
            this.keyIterator = keyIterator;
            this.schemaNode = schemaNode;
            this.resultElement = resultElement;
        }
    }




}
