package com.snoworca.cson.serializer;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class SchemaField extends SchemaValue {

    final Field field;

    final String comment;
    final String afterComment;

    //private final boolean isMapField;


    SchemaField(TypeElement parentsTypeElement, Field field, String path) {
        super(parentsTypeElement, path, field.getType());
        this.field = field;
        field.setAccessible(true);


        CSONValue csonValue = field.getAnnotation(CSONValue.class);
        String comment = csonValue.comment();
        String afterComment = csonValue.commentAfterKey();
        this.comment = comment.isEmpty() ? null : comment;
        this.afterComment = afterComment.isEmpty() ? null : afterComment;


        /*if(this.field.getType().isArray() && this.type != Types.ByteArray) {
            throw new CSONObjectException("Array type '" + this.field.getName() + "' is not supported");
        }
        if(this.type == Types.Object && this.field.getType().getAnnotation(CSON.class) == null)  {
            throw new CSONObjectException("Object field '" + this.field.getName() + "' is not annotated with @CSON");
        }*/
        assertValueType(field.getType(), field.getDeclaringClass().getName() + "." + field.getName() );
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



    @Override
    String getComment() {
        return comment;
    }
    @Override
    String getAfterComment() {
        return afterComment;
    }


    Field getField() {
        return field;
    }


    @Override
    Object getValue(Object parent) {
        try {
            return field.get(parent);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }
    @Override
    Object setValue(Object parent, Object value) {
        try {
            field.set(parent, value);
            return value;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    Object setValue(Object parent, short value) {
        try {
            field.setShort(parent, value);
            return value;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }
    @Override
    Object setValue(Object parent, int value) {
        try {
            field.setInt(parent, value);
            return value;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }
    @Override
    Object setValue(Object parent, long value) {
        try {
            field.setLong(parent, value);
            return value;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }
    @Override
    Object setValue(Object parent, float value) {
        try {
            field.setFloat(parent, value);
            return value;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }
    @Override
    Object setValue(Object parent, double value) {
        try {
            field.setDouble(parent, value);
            return value;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }
    @Override
    Object setValue(Object parent, boolean value) {
        try {
            field.setBoolean(parent, value);
            return value;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }
    @Override
    Object setValue(Object parent, char value) {
        try {
            field.setChar(parent, value);
            return value;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    Object setValue(Object parent, byte value) {
        try {
            field.setByte(parent, value);
            return value;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }



    @Override
    public String toString() {
        return getId() + ""; /*"FieldRack{" +
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
