package com.snoworca.cson.serializer;

import com.snoworca.cson.CSONArray;
import com.snoworca.cson.CSONElement;
import com.snoworca.cson.CSONObject;
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
            if(node instanceof SchemaField) {
                SchemaField schemaField = (SchemaField) node;
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

    private static void setValueTargetFromCSONObject(Object parents,SchemaField schemaField, CSONObject csonObject, String key) {
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
        } else if(Types.Collection == valueType) {
            CSONArray csonArray = csonObject.optArray(key);
            if(csonArray == null || !(schemaField instanceof SchemaFieldArray)) return;
            csonArrayToCollectionObject(csonArray, (SchemaFieldArray)schemaField, parents);
            // todo
        }

        else {
            try {
                schemaField.setValue(parents, null);
            } catch (Exception ignored) {}
        }

    }


    private static Object optValueInCSONArray(CSONArray csonArray, int index, Types valueType) {
        switch (valueType) {
            case Byte:
                return csonArray.optByte(index);
            case Short:
                return csonArray.optShort(index);
            case Integer:
                return csonArray.optInt(index);
            case Long:
                return csonArray.optLong(index);
            case Float:
                return csonArray.optFloat(index);
            case Double:
                return csonArray.optDouble(index);
            case Boolean:
                return csonArray.optBoolean(index);
            case Character:
                return csonArray.optChar(index, '\0');
            case String:
                return csonArray.optString(index);
        }
        return null;
    }




    @SuppressWarnings({"rawtypes", "ReassignedVariable", "unchecked"})
    private static void csonArrayToCollectionObject(CSONArray csonArray, SchemaFieldArray schemaFieldArray, Object parent) {
        List<SchemaFieldArray.CollectionItems> collectionItems = schemaFieldArray.getCollectionItems();
        int collectionItemIndex = 0;
        final int collectionItemSize = collectionItems.size();
        final int collectionItemEndIndex = collectionItemSize - 1;
        if(collectionItemSize == 0) {
            return;
        }
        SchemaFieldArray.CollectionItems collectionItem = collectionItems.get(collectionItemIndex);



        ArrayList<ArraySerializeDequeueItem> arraySerializeDequeueItems = new ArrayList<>();
        ArraySerializeDequeueItem currentArraySerializeDequeueItem = new ArraySerializeDequeueItem(null,csonArray).setArraySize(csonArray.size());
        int end = currentArraySerializeDequeueItem.arraySize - 1;
        arraySerializeDequeueItems.add(currentArraySerializeDequeueItem);

        CSONArray childCsonArray = csonArray;
        Collection currentCollection = null;


        // collectionItemIter 가 뒤로 빠꾸하는 일은 없음.
        for(int index = 0; index <= end; ++index) {

            if(index == 0) {
                if(currentCollection == null) {
                    currentCollection = collectionItem.newInstance();
                    schemaFieldArray.setValue(parent, currentCollection);
                }
                currentArraySerializeDequeueItem.setCollection(currentCollection);
            }
            currentArraySerializeDequeueItem.setArrayIndex(index);

            if(collectionItem.valueClass != null) {
                /*if(index == 0) {
                    Collection newCollection = collectionItem.newInstance();
                    currentCollection.add(newCollection);
                    currentCollection = newCollection;
                }*/
                Object value = optValueInCSONArray(csonArray, index, schemaFieldArray.valueType);
                currentCollection.add(value);
            } else {
                CSONArray inArray = childCsonArray.optArray(index);
                if(inArray == null) {
                    currentCollection.add(null);
                    continue;
                }

                ArraySerializeDequeueItem newArraySerializeDequeueItem = new ArraySerializeDequeueItem(null,csonArray);
                newArraySerializeDequeueItem.setArraySize(inArray.size());
                arraySerializeDequeueItems.add(newArraySerializeDequeueItem);
                childCsonArray = inArray;
                index = -1;
                end = newArraySerializeDequeueItem.arraySize - 1;
                currentArraySerializeDequeueItem = newArraySerializeDequeueItem;
                collectionItem = collectionItems.get(++collectionItemIndex);
                Collection newCollection = collectionItem.newInstance();
                currentCollection.add(newCollection);
                currentCollection = newCollection;
                newArraySerializeDequeueItem.setCollection(currentCollection);
            }
            if(index == end) {
                arraySerializeDequeueItems.remove(arraySerializeDequeueItems.size() - 1);
                if(arraySerializeDequeueItems.isEmpty()) {
                    break;
                }
                currentArraySerializeDequeueItem = arraySerializeDequeueItems.get(arraySerializeDequeueItems.size() - 1);
                childCsonArray = currentArraySerializeDequeueItem.resultArray;
                index = currentArraySerializeDequeueItem.index;
                end = currentArraySerializeDequeueItem.arraySize - 1;
                collectionItem = collectionItems.get(--collectionItemIndex);
            }
        }

    }



    private static void burnIterator(Iterator<?> iterator) {
        while(iterator.hasNext()) {
            iterator.next();
        }
    }





    private static class ArraySerializeDequeueItem {
        Iterator<?> iterator;
        CSONArray resultArray;

        Collection<?> collection;
        int index = 0;
        int arraySize = 0;
        private ArraySerializeDequeueItem(Iterator<?> iterator,CSONArray resultArray) {
            this.iterator = iterator;
            this.resultArray = resultArray;
        }

        private ArraySerializeDequeueItem setCollection(Collection<?> collection) {
            this.collection = collection;
            return this;
        }

        /**
         * 역직렬화에서 사용됨.
         * @param index
         * @return
         */
        private ArraySerializeDequeueItem setArrayIndex(int index) {
            this.index = index;
            return this;
        }

        /**
         * 역직렬화에서 사용됨.
         * @param maxIndex
         * @return
         */
        private ArraySerializeDequeueItem setArraySize(int maxIndex) {
            this.arraySize = maxIndex;
            return this;
        }
    }

    private static class ArrayDeserializeDequeueItem {
        Iterator<?> iterator;
        Collection<?> resultCollection;
        private ArrayDeserializeDequeueItem(Iterator<?> iterator) {
            this.iterator = iterator;
            this.resultCollection = resultCollection;
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
