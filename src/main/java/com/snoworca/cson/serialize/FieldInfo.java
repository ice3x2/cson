package com.snoworca.cson.serialize;


import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

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
                componentInfo.componentConstructor = componentClass.getDeclaredConstructor();
                componentInfo.componentConstructor.setAccessible(true);
            }

            this.componentInfos.add(componentInfo);
            if (!(componentClass instanceof Object) && componentInfo.type < 0) {
                isError = true;
                return;
            }

            if(DataType.TYPE_COLLECTION ==  componentInfo.type) {
                readComponentType(componentClass);
            }
        } catch (Exception e) {
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
        }
        return  type.getConstructor();
    }


    private boolean isError = false;
    private boolean isArray = false;
    private boolean isCollection = false;

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
        byte type;

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
