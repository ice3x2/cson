package com.snoworca.cson.serializer;

import java.lang.reflect.Method;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

abstract class SchemaValue implements SchemaNode {

    private static final AtomicInteger LAST_ID = new AtomicInteger(1);

    private final int id = LAST_ID.getAndIncrement();

    final TypeElement parentsTypeElement;
    final TypeElement objectTypeElement;

    final String path;
    final Types type;

    private final boolean isPrimitive;

    //private final boolean isMapField;

    private SchemaValue parentFieldRack;
    final Class<?> valueTypeClass;

    private ArrayList<SchemaValue> duplicatedSchemaValueList = null;


    static SchemaValue of(TypeElement typeElement, Field field) {
        CSONValue csonValue = field.getAnnotation(CSONValue.class);
        if(csonValue == null) return null;
        String key = csonValue.key();
        if(key == null || key.isEmpty()) key = csonValue.value();
        if(key == null || key.isEmpty()) key = field.getName();

        if(Collection.class.isAssignableFrom(field.getType())) {
            return new SchemaFieldArray(typeElement, field, key);
        } else if(Map.class.isAssignableFrom(field.getType())) {
            return new SchemaFieldMap(typeElement, field, key);
        }
        else {
            return new SchemaFieldNormal(typeElement, field, key);
        }
    }

    static SchemaValue of(TypeElement typeElement, Method method) {
        CSONValueGetter setter = method.getAnnotation(CSONValueGetter.class);
        CSONValueSetter getter = method.getAnnotation(CSONValueSetter.class);
        if(setter == null && getter == null) return null;
        return new SchemaMethod(typeElement, method);
    }


    boolean appendDuplicatedSchemaValue(SchemaValue node) {
        if(node.parentsTypeElement != this.parentsTypeElement) return false;
        if(this.duplicatedSchemaValueList == null) {
            this.duplicatedSchemaValueList = new ArrayList<>();
        }
        this.duplicatedSchemaValueList.add(node);
        return true;
    }

    @SuppressWarnings("unchecked")
    <T extends SchemaValue> List<T> getDuplicatedSchemaValueList() {
        return (List<T>) this.duplicatedSchemaValueList;
    }


    SchemaValue(TypeElement parentsTypeElement, String path, Class<?> valueTypeClass) {

        this.path = path;
        this.valueTypeClass = valueTypeClass;
        this.parentsTypeElement = parentsTypeElement;
        this.type = Types.of(valueTypeClass);



        if(this.type == Types.Object) {
            this.objectTypeElement = TypeElements.getInstance().getTypeInfo(valueTypeClass);
        }
        else {
            this.objectTypeElement = null;
        }

        this.isPrimitive = valueTypeClass.isPrimitive();
    }

    protected static void assertValueType(Class<?> valueType, String parentPath) {
        Types type = Types.of(valueType);
        if(valueType.isArray() && type != Types.ByteArray) {
            if(parentPath != null) {
                throw new CSONObjectException("Array type '" + valueType.getName() + "' is not supported");
            } else  {
                throw new CSONObjectException("Array type '" + valueType.getName() + "' of field '" + parentPath + "' is not supported");
            }
        }
        if(type == Types.Object && valueType.getAnnotation(CSON.class) == null)  {
            if(parentPath != null) {
                throw new CSONObjectException("Object type '" + valueType.getName() + "' is not annotated with @CSON");
            } else  {
                throw new CSONObjectException("Object type '" + valueType.getName() + "' of field '" + parentPath + "' is not annotated with @CSON");
            }
        }
    }


    Object newInstance() {
        if(objectTypeElement == null) return null;
        return objectTypeElement.newInstance();
    }


    boolean isPrimitive() {
        return isPrimitive;
    }


    abstract String  getComment();

    abstract String getAfterComment();

    final Types getType() {
        return type;
    }

    final int getId() {
        return id;
    }

    final String getPath() {
        return path;
    }

    final Class<?> getValueTypeClass() {
        return valueTypeClass;
    }

    @SuppressWarnings("unchecked")
    final <T extends SchemaValue> T getParentField() {
        return (T) parentFieldRack;
    }



    final void setParentFiled(SchemaValue parent) {
        this.parentFieldRack = parent;
    }


    final Object getValue(Object parent) {
        Object value = null;
        if(this.duplicatedSchemaValueList != null) {
            value = null;
            int index = this.duplicatedSchemaValueList.size() - 1;
            while(value == null && index > -1) {
                SchemaValue duplicatedSchemaValue = this.duplicatedSchemaValueList.get(index);
                value = duplicatedSchemaValue.onGetValue(parent);
                if(duplicatedSchemaValue.valueTypeClass != this.valueTypeClass) {
                    value = Utils.convertValue(value, duplicatedSchemaValue.type);
                }
                index--;

            }
            if(value != null) {
                return value;
            }
        }
        value = onGetValue(parent);
        return value;

    }

    final void setValue(Object parent, Object value) {
        if(this.duplicatedSchemaValueList != null) {
            for(SchemaValue duplicatedSchemaValue : this.duplicatedSchemaValueList) {
                Object setValue = value;
                if(duplicatedSchemaValue.valueTypeClass != this.valueTypeClass) {
                    setValue = Utils.convertValue(value, duplicatedSchemaValue.type);
                }
                duplicatedSchemaValue.onSetValue(parent, setValue);
            }
        }
        onSetValue(parent, value);
    }


    abstract Object onGetValue(Object parent);

    abstract void onSetValue(Object parent, Object value);




    void onSetValue(Object parent, short value) {
        onSetValue(parent, Short.valueOf(value));
    }

    void onSetValue(Object parent, int value) {
         onSetValue(parent, Integer.valueOf(value));
    }

    void onSetValue(Object parent, long value) {
         onSetValue(parent, Long.valueOf(value));
    }

    void onSetValue(Object parent, float value) {
         setValue(parent, Float.valueOf(value));
    }

    void onSetValue(Object parent, double value) {
         onSetValue(parent,Double.valueOf(value));
    }

    void onSetValue(Object parent, boolean value) {
         onSetValue(parent,Boolean.valueOf(value));
    }

    void onSetValue(Object parent, char value) {
         onSetValue(parent,Character.valueOf(value));
    }

    void onSetValue(Object parent, byte value) {
         onSetValue(parent,Byte.valueOf(value));
    }



    @Override
    public String toString() {
        return id + ""; /*"FieldRack{" +
                "id=" + id +
                ", field=" + field +
                ", path='" + path + '\'' +
                ", isPrimitive=" + isPrimitive +
                ", isByteArray=" + isByteArray +
                ", typeElement=" + typeElement +
                ", fieldType=" + fieldType +
                ", type=" + type +
                ", parentFieldRack=" + parentFieldRack +
                '}';*/
    }




}
