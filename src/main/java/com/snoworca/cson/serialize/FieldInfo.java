package com.snoworca.cson.serialize;


import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.*;

public class FieldInfo implements Comparable {
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
            isArray = true;
            ComponentInfo componentInfo = new ComponentInfo();
            Class<?> componentClass = type.getComponentType();
            componentInfo.type = DataType.getDataType(componentClass);
            isPrimitive = componentClass.isPrimitive();
            if (componentInfo.type < 0) {
                isError = true;
            }
            componentInfos.add(componentInfo);
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
            ComponentInfo componentInfo = new ComponentInfo();
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
                    if (Collection.class.isAssignableFrom((Class<?>) ((ParameterizedType) valueType).getRawType())){
                        componentInfo.type = DataType.TYPE_COLLECTION;
                        componentInfos.add(componentInfo);
                        readComponentType(valueType);
                        return;
                    }
                    else if (Map.class.isAssignableFrom((Class<?>) ((ParameterizedType) valueType).getRawType())){
                        componentInfo.type = DataType.TYPE_MAP;
                        componentInfos.add(componentInfo);
                        readMapType(valueType);
                        return;
                    }
                }


                componentInfo.type = DataType.getDataType((Class<?>)valueType);
                if(DataType.TYPE_ARRAY == componentInfo.type) {
                    Class<?> componentClass = ((Class<?>)valueType).getComponentType();
                    componentInfo.type = DataType.getDataType(componentClass);
                    componentInfo.isArray = true;
                    componentInfo.isPrimitive = componentClass.isPrimitive();
                    if (componentInfo.type < 0) {
                        isError = true;
                    }
                    componentInfos.add(componentInfo);
                }
                else if(!DataType.isJsonDefaultType(componentInfo.type)) {
                    componentInfo.componentConstructor = ((Class<?>) valueType).getDeclaredConstructor();
                    componentInfo.componentConstructor.setAccessible(true);
                }



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



    private void readComponentType(Type type) {
        try {
            ComponentInfo componentInfo = new ComponentInfo();

            ParameterizedType integerListType = null;
            Class<?> componentClass = null;
            if (type instanceof ParameterizedType) {
                integerListType = (ParameterizedType)type;
                Type actualType = integerListType.getActualTypeArguments()[0];
                Type rawType = integerListType.getRawType();
                componentInfo.collectionConstructor = constructorOfCollection((Class<?>) rawType);
                componentInfo.collectionConstructor.setAccessible(true);
                if(actualType instanceof ParameterizedType && Collection.class.isAssignableFrom((Class<?>)((ParameterizedType)actualType).getRawType())) {
                    componentInfo.type = DataType.TYPE_COLLECTION;
                    componentInfos.add(componentInfo);
                    readComponentType(actualType);
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



    private String keyName = null;
    private boolean isError = false;
    private boolean isArray = false;
    private boolean isCollection = false;
    private boolean isMap = false;

    private boolean isByteArrayToCSONArray = false;

    private int arraySize = 0;


    private byte type;
    //private byte componentType;
    private ArrayList<ComponentInfo> componentInfos = new ArrayList<>();
    private Constructor<?> csonObjectConstructor;
    private boolean isPrimitive = true;
    private boolean isCSON = false;

    private Field field;
    private String name;

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
        return componentInfos.size() > 1;
    }

    public int componentInfoSize() {
        return componentInfos.size();
    }

    public ComponentInfo getComponentInfo(int index) {
        return componentInfos.get(index);
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

    protected class ComponentInfo {

        private Constructor<?> collectionConstructor;
        private Constructor<?> componentConstructor;

        private Constructor<?> keyConstructor;



        boolean isPrimitive = false;
        boolean isArray = false;
        byte type;


        public boolean isPrimitive() {
            return isPrimitive;
        }
        public boolean isArray() {
            return isArray;
        }

        public Constructor<?> getCollectionConstructor() {
            return collectionConstructor;
        }
        public Constructor<?> getComponentConstructor() {
            return componentConstructor;
        }

        public byte getType() {
            return type;
        }
    }
}
