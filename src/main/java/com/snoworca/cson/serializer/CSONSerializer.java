package com.snoworca.cson.serializer;

import com.snoworca.cson.CSONArray;
import com.snoworca.cson.CSONElement;
import com.snoworca.cson.CSONObject;
import java.util.*;

public class CSONSerializer {

    private CSONSerializer() {}

    public static CSONObject toCSONObject(Object obj) {
        Objects.requireNonNull(obj, "obj is null");
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
                for(SchemaField parentSchemaField : parentschemaField) {
                    int id = parentSchemaField.getId();
                    if(parentObjMap.containsKey(id)) {
                        continue;
                    }
                    SchemaField grandschemaField = parentSchemaField.getParentField();
                    if (grandschemaField == null) {
                        Object obj = parentSchemaField.getValue(rootObject);
                        if(obj != null) {
                            parentObjMap.put(id, obj);
                            nullCount--;
                        }
                    }
                    else {
                        Object grandObj = parentObjMap.get(grandschemaField.getId());
                        if(grandObj != null) {
                            Object obj = parentSchemaField.getValue(grandObj);
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
                        ((CSONObject) csonElement).put((String) key, value);
                        ((CSONObject) csonElement).getComment()
                    }
                    else {
                        assert csonElement instanceof CSONArray;
                        ((CSONArray) csonElement).set((int) key, value);
                    }


                }
            } else if(node instanceof SchemaFieldArray) {
                SchemaFieldArray schemaFieldArray = (SchemaFieldArray)node;
                Object parent = obtainParentObjects(parentObjMap, schemaFieldArray, rootObject);
                if(parent != null) {
                    Object value = schemaFieldArray.getValue(parent);
                    if(value != null) {
                        CSONArray csonArray = collectionObjectToCSONArray((Collection<?>)value, schemaFieldArray);
                        if(key instanceof String)
                            ((CSONObject)csonElement).put((String) key, csonArray);
                        else {
                            assert csonElement instanceof CSONArray;
                            ((CSONArray)csonElement).set((int)key, csonArray);
                        }
                        csonElement.setComment(schemaFieldArray.getComment());
                        csonElement.setTailComment(schemaFieldArray.getAfterComment());
                    }
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
                if(object == null)  {
                    csonArray.add(null);
                } else {
                    CSONObject childObject = serializeTypeElement(objectValueTypeElement, object);
                    csonArray.add(childObject);
                }
            }
            while(!iter.hasNext() && !arraySerializeDequeueItems.isEmpty()) {
                ArraySerializeDequeueItem arraySerializeDequeueItem = arraySerializeDequeueItems.getFirst();
                iter = arraySerializeDequeueItem.iterator;
                csonArray = arraySerializeDequeueItem.csonArray;
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
    }


    @SuppressWarnings("unchecked")
    public static<T> T fromCSONObject(CSONObject csonObject, Class<T> clazz) {
        TypeElement typeElement = TypeElements.getInstance().getTypeInfo(clazz);
        Object object = typeElement.newInstance();
        return (T) fromCSONObject(csonObject, object);

    }


    private static CSONElement getChildElement(SchemaElementNode schemaElementNode,CSONElement csonElement, Object key) {
        if(key instanceof String) {
            CSONObject csonObject = (CSONObject)csonElement;
            return schemaElementNode instanceof  SchemaArrayNode ? csonObject.optArray((String) key) :  csonObject.optObject((String) key) ;
        } else {
            CSONArray csonArray = (CSONArray) csonElement;
            return schemaElementNode instanceof SchemaArrayNode ?  csonArray.optArray((int) key) :  csonArray.optObject((int) key);
        }

    }

    public static<T> T fromCSONObject(final CSONObject csonObject, T targetObject) {
        TypeElement typeElement = TypeElements.getInstance().getTypeInfo(targetObject.getClass());
        SchemaObjectNode schemaRoot = typeElement.getSchema();
        HashMap<Integer, Object> parentObjMap = new HashMap<>();
        CSONElement csonElement = csonObject;
        ArrayDeque<ObjectSerializeDequeueItem> objectSerializeDequeueItems = new ArrayDeque<>();
        Iterator<Object> iter = schemaRoot.keySet().iterator();
        SchemaObjectNode schemaNode = schemaRoot;
        ObjectSerializeDequeueItem currentObjectSerializeDequeueItem = new ObjectSerializeDequeueItem(iter, schemaNode, csonObject);
        objectSerializeDequeueItems.add(currentObjectSerializeDequeueItem);
        while(iter.hasNext()) {
            Object key = iter.next();
            SchemaNode node = schemaNode.get(key);
            if(node instanceof SchemaElementNode) {
                boolean nullValue = false;
                CSONElement childElement = getChildElement((SchemaElementNode) node, csonElement, key);
                if(key instanceof String) {
                    CSONObject parentObject = (CSONObject) csonElement;
                    if(childElement == null) {
                        if(parentObject.isNull((String) key)) {
                            nullValue = true;
                        } else {
                            continue;
                        }
                    }
                } else {
                    CSONArray parentArray = (CSONArray)csonElement;
                    int index = (Integer)key;
                    if(childElement == null) {
                        if(parentArray.size() <= index || parentArray.isNull(index)) {
                            nullValue = true;
                        } else {
                            continue;
                        }
                    }
                }
                csonElement = childElement;
                schemaNode = (SchemaObjectNode)node;
                List<SchemaField> parentSchemaFieldList = schemaNode.getParentSchemaFieldList();
                for(SchemaField parentSchemaField : parentSchemaFieldList) {
                    getOrCreateParentObject(parentSchemaField, parentObjMap, targetObject, nullValue);
                }
                iter = schemaNode.keySet().iterator();
                currentObjectSerializeDequeueItem = new ObjectSerializeDequeueItem(iter, schemaNode, csonElement);
                objectSerializeDequeueItems.add(currentObjectSerializeDequeueItem);
            }
            else if(node instanceof SchemaField && ((SchemaField)node).type != Types.Object) {
                SchemaField schemaField = (SchemaField) node;
                SchemaField parentField = schemaField.getParentField();
                if(csonElement != null) {
                    Object obj = getOrCreateParentObject(parentField, parentObjMap, targetObject);
                    setValueTargetFromCSONObject(obj, schemaField, csonElement, key);
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
        return targetObject;
    }

    private static Object getOrCreateParentObject(SchemaField parentSchemaField, HashMap<Integer, Object> parentObjMap, Object root) {
        return getOrCreateParentObject(parentSchemaField, parentObjMap, root, false);
    }

    private static Object getOrCreateParentObject(SchemaField parentSchemaField, HashMap<Integer, Object> parentObjMap, Object root, boolean setNull) {
        if(parentSchemaField == null) return root;

        int id = parentSchemaField.getId();
        Object parent = parentObjMap.get(id);
        if (parent != null) {
            return parent;
        }
        ArrayList<SchemaField>  pedigreeList = new ArrayList<>();
        while(parentSchemaField != null) {
            pedigreeList.add(parentSchemaField);
            parentSchemaField = parentSchemaField.getParentField();
        }
        Collections.reverse(pedigreeList);
        parent = root;
        SchemaField last = pedigreeList.get(pedigreeList.size() - 1);
        for(SchemaField schemaField : pedigreeList) {
           int parentId = schemaField.getId();
           Object child = parentObjMap.get(parentId);
           if(setNull && child == null && schemaField == last) {
                schemaField.setValue(parent, null);
           }
           else if(!setNull && child == null) {
                child = schemaField.newInstance();
                parentObjMap.put(parentId, child);
                schemaField.setValue(parent, child);
           }
           parent = child;
        }
        return parent;

    }

    private static boolean setValueTargetFromCSONObject(Object parents,SchemaField schemaField, CSONElement cson, Object key) {
        boolean isArrayType = cson instanceof CSONArray;

        Object value = isArrayType ? ((CSONArray) cson).opt((int)key) : ((CSONObject)cson).opt((String)key);
        //todo null 값에 대하여 어떻게 할 것인지 고민해봐야함.
        if(value == null) {
            boolean isNull = isArrayType ? ((CSONArray) cson).isNull((int)key) : ((CSONObject)cson).isNull((String)key);
            if(isNull && !schemaField.isPrimitive()) {
                try {
                    schemaField.setValue(parents, null);
                } catch (Exception ignored) {}
            }
            return false;
        }
        Types valueType = schemaField.getType();
        if(Types.Boolean == valueType) {
             schemaField.setValue(parents,isArrayType ? ((CSONArray) cson).optBoolean((int)key) : ((CSONObject)cson).optBoolean((String)key));
        } else if(Types.Byte == valueType) {
            schemaField.setValue(parents, isArrayType ? ((CSONArray) cson).optByte((int)key) : ((CSONObject)cson).optByte((String)key));
        } else if(Types.Character == valueType) {
            schemaField.setValue(parents, isArrayType ? ((CSONArray) cson).optChar((int)key, '\0') : ((CSONObject)cson).optChar((String)key, '\0'));
        } else if(Types.Short == valueType) {
            schemaField.setValue(parents, isArrayType ? ((CSONArray) cson).optShort((int)key) : ((CSONObject)cson).optShort((String)key));
        } else if(Types.Integer == valueType) {
            schemaField.setValue(parents, isArrayType ? ((CSONArray) cson).optInt((int)key) : ((CSONObject)cson).optInt((String)key));
        } else if(Types.Float == valueType) {
            schemaField.setValue(parents, isArrayType ? ((CSONArray) cson).optFloat((int)key) : ((CSONObject)cson).optFloat((String)key));
        } else if(Types.Double == valueType) {
            schemaField.setValue(parents, isArrayType ? ((CSONArray) cson).optDouble((int)key) : ((CSONObject)cson).optDouble((String)key));
        } else if(Types.String == valueType) {
            schemaField.setValue(parents, isArrayType ? ((CSONArray) cson).optString((int)key) : ((CSONObject)cson).optString((String)key));
        } else if(Types.Collection == valueType) {
            CSONArray csonArray = isArrayType ? ((CSONArray) cson).optArray((int)key) : ((CSONObject)cson).optArray((String)key);
            if(csonArray != null) {
                csonArrayToCollectionObject(csonArray, (SchemaFieldArray)schemaField, parents);
            } else if(isArrayType ? ((CSONArray) cson).isNull((int)key) : ((CSONObject)cson).isNull((String)key)) {
                try {
                    schemaField.setValue(parents, null);
                } catch (Exception ignored) {}
            }
        } else if(Types.Object == valueType) {
            CSONObject csonObj = isArrayType ? ((CSONArray) cson).optObject((int)key) : ((CSONObject)cson).optObject((String)key);
            if(csonObj != null) {
                Object target = schemaField.newInstance();
                fromCSONObject(csonObj, target);
                schemaField.setValue(parents, target);
            }
        }
        else {
            try {
                schemaField.setValue(parents, null);
            } catch (Exception ignored) {}
            return false;
        }
        return true;

    }


    private static Object optValueInCSONArray(CSONArray csonArray, int index, SchemaFieldArray schemaFieldArray) {



        switch (schemaFieldArray.ValueType) {
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
            case Object:
                CSONObject csonObject = csonArray.optObject(index);
                if(csonObject != null) {
                    Object target = schemaFieldArray.getObjectValueTypeElement().newInstance();
                    fromCSONObject(csonObject, target);
                    return target;
                }
        }
        return null;
    }




    @SuppressWarnings({"rawtypes", "ReassignedVariable", "unchecked"})
    private static void csonArrayToCollectionObject(CSONArray csonArray, SchemaFieldArray schemaFieldArray, Object parent) {
        List<SchemaFieldArray.CollectionItems> collectionItems = schemaFieldArray.getCollectionItems();
        int collectionItemIndex = 0;
        final int collectionItemSize = collectionItems.size();
        if(collectionItemSize == 0) {
            return;
        }
        SchemaFieldArray.CollectionItems collectionItem = collectionItems.get(collectionItemIndex);
        ArrayList<ArraySerializeDequeueItem> arraySerializeDequeueItems = new ArrayList<>();
        ArraySerializeDequeueItem objectItem = new ArraySerializeDequeueItem(csonArray,collectionItem.newInstance());
        int end = objectItem.getEndIndex();
        arraySerializeDequeueItems.add(objectItem);
        schemaFieldArray.setValue(parent, objectItem.collectionObject);

        if(objectItem.collectionObject instanceof LinkedList) {
            System.out.println("테스트 시작");
        }

        for(int index = 0; index <= end; ++index) {
            objectItem.setArrayIndex(index);
            if (collectionItem.valueClass != null) {
                Object value = optValueInCSONArray(objectItem.csonArray, index, schemaFieldArray);
                objectItem.collectionObject.add(value);
            } else {
                CSONArray inArray = objectItem.csonArray.optArray(index);
                if (inArray == null) {
                    objectItem.collectionObject.add(null);
                } else {
                    collectionItem = collectionItems.get(++collectionItemIndex);
                    Collection newCollection = collectionItem.newInstance();
                    objectItem.collectionObject.add(newCollection);
                    ArraySerializeDequeueItem newArraySerializeDequeueItem = new ArraySerializeDequeueItem(inArray, newCollection);
                    arraySerializeDequeueItems.add(newArraySerializeDequeueItem);
                    index = -1;
                    end = newArraySerializeDequeueItem.getEndIndex();
                    objectItem = newArraySerializeDequeueItem;
                }
            }

            while (index == end) {
                arraySerializeDequeueItems.remove(arraySerializeDequeueItems.size() - 1);
                if (arraySerializeDequeueItems.isEmpty()) {
                    break;
                }
                objectItem = arraySerializeDequeueItems.get(arraySerializeDequeueItems.size() - 1);
                index = objectItem.index;
                end = objectItem.arraySize - 1;
                collectionItem = collectionItems.get(--collectionItemIndex);
            }
        }

    }



    private static void burnIterator(Iterator<?> iterator) {
        while(iterator.hasNext()) {
            iterator.next();
        }
    }





    @SuppressWarnings("rawtypes")
    private static class ArraySerializeDequeueItem {
        Iterator<?> iterator;
        CSONArray csonArray;

        Collection collectionObject;
        int index = 0;
        int arraySize = 0;
        private ArraySerializeDequeueItem(Iterator<?> iterator,CSONArray csonArray) {
            this.iterator = iterator;
            this.csonArray = csonArray;
        }

        private ArraySerializeDequeueItem(CSONArray csonArray, Collection collection) {
            this.csonArray = csonArray;
            this.arraySize = csonArray.size();
            this.collectionObject = collection;
        }

        private int getEndIndex() {
            return arraySize - 1;
        }


        private ArraySerializeDequeueItem setCollectionObject(Collection<?> collectionObject) {
            this.collectionObject = collectionObject;
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
