package com.snoworca.cson.serializer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
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
    final Class<?>  valueType;


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


    SchemaValue(TypeElement parentsTypeElement, String path, Class<?> valueType) {

        this.path = path;
        this.valueType = valueType;
        this.parentsTypeElement = parentsTypeElement;
        this.type = Types.of(valueType);



        if(this.type == Types.Object) {
            this.objectTypeElement = TypeElements.getInstance().getTypeInfo(valueType);
        }
        else {
            this.objectTypeElement = null;
        }

        this.isPrimitive = valueType.isPrimitive();
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

    final Class<?> getValueType() {
        return valueType;
    }

    @SuppressWarnings("unchecked")
    final <T extends SchemaValue> T getParentField() {
        return (T) parentFieldRack;
    }



    final void setParentFiled(SchemaValue parent) {
        this.parentFieldRack = parent;
    }


    abstract Object getValue(Object parent);

    abstract Object setValue(Object parent, Object value);

    Object setValue(Object parent, short value) {
        return setValue(parent, Short.valueOf(value));
    }

    Object setValue(Object parent, int value) {
        return setValue(parent, Integer.valueOf(value));
    }

    Object setValue(Object parent, long value) {
        return setValue(parent, Long.valueOf(value));
    }

    Object setValue(Object parent, float value) {
        return setValue(parent, Float.valueOf(value));
    }

    Object setValue(Object parent, double value) {
        return setValue(parent,Double.valueOf(value));
    }

    Object setValue(Object parent, boolean value) {
        return setValue(parent,Boolean.valueOf(value));
    }

    Object setValue(Object parent, char value) {
        return setValue(parent,Character.valueOf(value));
    }

    Object setValue(Object parent, byte value) {
        return setValue(parent,Byte.valueOf(value));
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


    static boolean isSchemaMethodGetter(SchemaNode schemaValue) {
        return schemaValue instanceof SchemaMethod && ((SchemaMethod)schemaValue).getMethodType() == SchemaMethod.MethodType.Getter;
    }

    static boolean isSchemaMethodSetter(SchemaNode schemaValue) {
        return schemaValue instanceof SchemaMethod && ((SchemaMethod)schemaValue).getMethodType() == SchemaMethod.MethodType.Setter;
    }



}
