package com.snoworca.cson.serialize;


import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.*;

public class FieldInfo implements Comparable {






    private boolean isComponent = false;
    private String keyName = null;
    private boolean isError = false;
    private boolean isArray = false;
    private boolean isCollection = false;
    private boolean isMap = false;

    private boolean isByteArrayToCSONArray = false;

    private int arraySize = 0;


    private Constructor<?> collectionConstructor;
    private Constructor<?> componentConstructor;

    private Constructor<?> keyConstructor;

    private FieldInfo parent;




    public Constructor<?> getCollectionConstructor() {
        return collectionConstructor;
    }
    public Constructor<?> getComponentConstructor() {
        return componentConstructor;
    }



    private byte type;
    //private byte componentType;
    private ArrayList<FieldInfo> componentInfos = new ArrayList<>();
    private Constructor<?> csonObjectConstructor;
    private boolean isPrimitive = true;
    private boolean isCSON = false;

    private Field field;
    private String name;


    FieldInfo() {
        isComponent = true;
    }

    FieldInfo(Field field, String name) {
        this.field = field;
        this.name = name;
        field.setAccessible(true);
        Class<?> type = this.field.getType();
        this.type = DataType.getDataType(type);
        if (this.type < 0) {
            isError = true;
            return;
        } else if (this.type == DataType.TYPE_CSON_OBJECT) {
            try {
                csonObjectConstructor = type.getDeclaredConstructor();
                csonObjectConstructor.setAccessible(true);
                return;
            } catch (NoSuchMethodException e) {
                isError = true;
                return;
            }
        }

        this.isPrimitive = type.isPrimitive();
        if (type.isArray()) {
            readArrayType(type);
        } else if (Collection.class.isAssignableFrom(type)) {
            setCollection(true);
            readComponentType(field.getGenericType());
        } else if (Map.class.isAssignableFrom(type)) {
            setMap(true);
            readMapType(field.getGenericType());
        }
    }


    private void readMapType(Type type) {
        try {
            FieldInfo componentInfo = new FieldInfo();
            ParameterizedType integerListType = null;

            if (type instanceof ParameterizedType) {
                integerListType = (ParameterizedType)type;
                Type[] actualType = integerListType.getActualTypeArguments();
                if(actualType.length != 2) {
                    isError = true;
                    return;
                }
                Type keyType = actualType[0];
                Type valueType = actualType[1];
                Type rawType = integerListType.getRawType();
                componentInfo.collectionConstructor = constructorOfMap((Class<?>) rawType);
                componentInfo.collectionConstructor.setAccessible(true);

                try {
                    Constructor<?> constructor = ((Class<?>) keyType).getDeclaredConstructor(String.class);
                    constructor.setAccessible(true);
                    componentInfo.keyConstructor = constructor;
                } catch (NoSuchMethodException e) {
                    //TODO key 값은 문자열이 인자인 생성자가 있어야함.
                    isError = true;
                    return;
                }


                if(valueType instanceof ParameterizedType) {
                    putCollectionType((ParameterizedType) valueType,componentInfo);
                    return;
                }


                componentInfo.type = DataType.getDataType((Class<?>)valueType);
                if(DataType.TYPE_ARRAY == componentInfo.type) {
                    Class<?> componentClass = ((Class<?>)valueType).getComponentType();
                    componentInfo.type = DataType.getDataType(componentClass);
                    componentInfo.isArray = true;
                    componentInfo.isPrimitive = componentClass.isPrimitive();
                    if (componentInfo.type < 0) {
                        isError = true;
                        return;
                    }
                }
                else if(!DataType.isJsonDefaultType(componentInfo.type)) {
                    componentInfo.componentConstructor = ((Class<?>) valueType).getDeclaredConstructor();
                    componentInfo.componentConstructor.setAccessible(true);
                } else {
                    componentInfo.isPrimitive = ((Class<?>) valueType).isPrimitive();


                }

                componentInfo.parent = this;
                this.componentInfos.add(componentInfo);

                if(DataType.TYPE_COLLECTION ==  componentInfo.type) {
                    readComponentType(valueType);
                } else if(DataType.TYPE_MAP ==  componentInfo.type) {
                    readMapType(valueType);
                }


            } else {
                //TODO 예외처리 필요 (Map이 아닌 경우)
                isError = true;
                return;
            }

        } catch (Exception e) {
            // TODO 어딘가에서 에러를 처리해야함
            isError = true;
        }
    }


    private void putCollectionType(ParameterizedType valueType, FieldInfo componentInfo) {
        if (Collection.class.isAssignableFrom((Class<?>) valueType.getRawType())){
            componentInfo.type = DataType.TYPE_COLLECTION;
            componentInfo.isCollection = true;
            componentInfo.parent = this;
            componentInfos.add(componentInfo);
            readComponentType(valueType);
        }
        else if (Map.class.isAssignableFrom((Class<?>) (valueType).getRawType())){
            componentInfo.type = DataType.TYPE_MAP;
            componentInfo.isMap = true;
            componentInfo.parent = this;
            componentInfos.add(componentInfo);
            readMapType(valueType);
        }
    }

    private void readArrayType(Type type) {
        try {
            isArray = true;
            FieldInfo componentInfo = new FieldInfo();
            Class<?> componentClass = ((Class<?>)type).getComponentType();
            componentInfo.type = DataType.getDataType(componentClass);
            componentInfo.isArray = true;
            isPrimitive = componentClass.isPrimitive();

            componentInfo.parent = this;
            ParameterizedType integerListType = null;
            componentInfos.add(componentInfo);
            if (componentInfo.type < 0) {
                isError = true;
            }

            if(DataType.isObjectType(componentInfo.type)) {
                //TODO 생성자가 없을 경우 예외를 발생시켜야함.
                componentInfo.componentConstructor = componentClass.getDeclaredConstructor();
                componentInfo.componentConstructor.setAccessible(true);
            } else if(DataType.TYPE_COLLECTION == componentInfo.type || DataType.TYPE_MAP == componentInfo.type) {
                // TODO 컬렉션 타입일 경우 예외를 발생시켜야함.
                isError = true;
                return;
            }
            else if (!(componentClass instanceof Object) && componentInfo.type < 0) {
                isError = true;
                return;
            }


        } catch (Exception e) {
            // TODO 어딘가에서 에러를 처리해야함
            isError = true;
        }
    }


    private void readComponentType(Type type) {
        try {
            FieldInfo componentInfo = new FieldInfo();

            ParameterizedType integerListType = null;
            Class<?> componentClass = null;


            if (type instanceof ParameterizedType) {
                integerListType = (ParameterizedType)type;
                Type actualType = integerListType.getActualTypeArguments()[0];
                Type rawType = integerListType.getRawType();
                componentInfo.collectionConstructor = constructorOfCollection((Class<?>) rawType);
                componentInfo.collectionConstructor.setAccessible(true);
                if(actualType instanceof ParameterizedType) {
                    putCollectionType((ParameterizedType) actualType,componentInfo);
                    return;
                }
                componentClass = ((Class<?>) actualType);
            } else {
                componentClass = Object.class;
            }
            componentInfo.type = DataType.getDataType(componentClass);
            if(DataType.isObjectType(componentInfo.type)) {
                //TODO 생성자가 없을 경우 예외를 발생시켜야함.
                componentInfo.componentConstructor = componentClass.getDeclaredConstructor();
                componentInfo.componentConstructor.setAccessible(true);
            }

            if (!(componentClass instanceof Object) && componentInfo.type < 0) {
                isError = true;
                return;
            }
            componentInfo.parent = this;
            this.componentInfos.add(componentInfo);

            if(DataType.TYPE_COLLECTION ==  componentInfo.type) {
                readComponentType(componentClass);
            } else if(DataType.TYPE_MAP ==  componentInfo.type) {
                readMapType(componentClass);
            }

        } catch (Exception e) {
            // TODO 어딘가에서 에러를 처리해야함
            isError = true;
        }
    }

    private Constructor<?> constructorOfCollection(Class<?> type) throws NoSuchMethodException {
        if (type.isInterface() && SortedSet.class.isAssignableFrom(type)) {
            return TreeSet.class.getConstructor();
        } else if (type.isInterface() && Set.class.isAssignableFrom(type)) {
            return  HashSet.class.getConstructor();
        } else if (type.isInterface() && (AbstractQueue.class.isAssignableFrom(type) || Deque.class.isAssignableFrom(type) || Queue.class.isAssignableFrom(type))) {
            return  ArrayDeque.class.getConstructor();
        } else if (type.isInterface() && (List.class.isAssignableFrom(type) || Collection.class.isAssignableFrom(type)) || type == Collection.class) {
            return  ArrayList.class.getConstructor();
        } else if (type.isInterface() && (NavigableSet.class.isAssignableFrom(type) || SortedSet.class.isAssignableFrom(type))) {
            return  TreeSet.class.getConstructor();
        }
        return  type.getConstructor();
    }

    private Constructor<?> constructorOfMap(Class<?> type) throws NoSuchMethodException {
        if (type.isInterface() && ConcurrentNavigableMap.class.isAssignableFrom(type)) {
            return  ConcurrentSkipListMap.class.getConstructor();
        }  else if (type.isInterface() && ConcurrentMap.class.isAssignableFrom(type)) {
            return  ConcurrentHashMap.class.getConstructor();
        }
        else if (type.isInterface() && (AbstractMap.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type))) {
            return LinkedHashMap.class.getConstructor();
        } else if (type.isInterface() && Dictionary.class.isAssignableFrom(type)) {
            return  Hashtable.class.getConstructor();
        } else if (type.isInterface() && (NavigableMap.class.isAssignableFrom(type) || SortedMap.class.isAssignableFrom(type))) {
            return  TreeMap.class.getConstructor();
        }
        return  type.getConstructor();
    }


    public boolean isByteArrayToCSONArray() {
        return isByteArrayToCSONArray;
    }

    public FieldInfo setByteArrayToCSONArray(boolean byteArrayToCSONArray) {
        isByteArrayToCSONArray = byteArrayToCSONArray;
        return this;
    }

    public boolean isError() {
        return isError;
    }

    protected void setError(boolean error) {
        isError = error;
    }

    public boolean isArray() {
        return isArray;
    }

    public boolean isCSONObject() {
        return this.type == DataType.TYPE_CSON_OBJECT;
    }

    protected void setArray(boolean array) {
        isArray = array;
    }

    public boolean isCollection() {
        return isCollection;
    }

    protected void setCollection(boolean collection) {
        isCollection = collection;
    }

    protected void setMap(boolean map) {
        isMap = map;
    }

    public boolean isMap() {
        return isMap;
    }

    public int getArraySize() {
        return arraySize;
    }

    protected void setArraySize(int arraySize) {
        this.arraySize = arraySize;
    }

    public byte getType() {
        return type;
    }

    protected void setType(byte type) {
        this.type = type;
    }


    public boolean isNestedCollection() {
        return (!isComponent && componentInfos.size() > 1) || (isComponent && (isCollection || isMap));

    }

    public int componentInfoSize() {
        return componentInfos.size();
    }


    public FieldInfo getComponentInfo(int index) {
        if((parent == null && index >= componentInfos.size())  || (parent != null && index >= parent.componentInfos.size())) {
            return null;
        }
        return parent == null ? componentInfos.get(index) : parent.componentInfos.get(index);
    }


    public boolean isPrimitive() {
        return isPrimitive;
    }

    protected void setPrimitive(boolean primitive) {
        isPrimitive = primitive;
    }

    public Field getField() {
        return field;
    }

    protected void setField(Field field) {
        this.field = field;
    }

    public String getName() {
        return name;
    }

    protected void setName(String name) {
        this.name = name;
    }


    @Override
    public int compareTo(Object o) {
        FieldInfo info = (FieldInfo) o;
        return name.compareTo(info.name);
    }


}
