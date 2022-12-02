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
        if(this.type < 0) {
            isError = true;
            return;
        }
        else if(this.type == DataType.TYPE_CSON_OBJECT) {
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
        if(type.isArray()) {
            isArray = true;
            Class<?> componentClass = type.getComponentType();
            componentType = DataType.getDataType(componentClass);
            isPrimitive = componentClass.isPrimitive();

            if(componentType < 0) {
                isError = true;
                //return;
            }
        } else if(Collection.class.isAssignableFrom(type)) {
            setCollection(true);
            try {
                Type genericType = field.getGenericType();
                ParameterizedType integerListType = null;
                Class<?> componentClass = null;
                if(genericType instanceof ParameterizedType) {
                    integerListType = (ParameterizedType) genericType;
                    componentClass = (Class<?>) integerListType.getActualTypeArguments()[0];
                } else {
                    componentClass = Object.class;
                }

                componentType = DataType.getDataType(componentClass);
                if(!(componentClass instanceof Object) && componentType < 0) {
                    isError = true;
                    return;
                }
                if(type.isInterface() && SortedSet.class.isAssignableFrom(type)) {
                    componentTypeConstructor = TreeSet.class.getConstructor();
                }
                else if(type.isInterface() && Set.class.isAssignableFrom(type)) {
                    componentTypeConstructor = HashSet.class.getConstructor();
                }
                else if(type.isInterface() && (Deque.class.isAssignableFrom(type) || Queue.class.isAssignableFrom(type))) {
                    componentTypeConstructor = ArrayDeque.class.getConstructor();
                }
                else if(type.isInterface() && (List.class.isAssignableFrom(type) || Collection.class.isAssignableFrom(type)) || type == Collection.class) {
                    componentTypeConstructor = ArrayList.class.getConstructor();
                }
                else {
                    componentTypeConstructor = type.getConstructor();
                }
            } catch (Exception e) {
                isError = true;
            }

        }
    }
    private boolean isError = false;
    private boolean isArray = false;
    private boolean isCollection = false;

    private boolean isByteArrayToCSONArray = false;

    private int arraySize = 0;


    private byte type;
    private byte componentType;
    private Constructor<?> componentTypeConstructor;
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

    public byte getComponentType() {
        return componentType;
    }

    protected void setComponentType(byte componentType) {
        this.componentType = componentType;
    }

    public Constructor<?> getComponentTypeConstructor() {
        return componentTypeConstructor;
    }

    protected void setComponentTypeConstructor(Constructor<?> componentTypeConstructor) {
        this.componentTypeConstructor = componentTypeConstructor;
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
        FieldInfo info = (FieldInfo)o;
        return name.compareTo(info.name);
    }
}
