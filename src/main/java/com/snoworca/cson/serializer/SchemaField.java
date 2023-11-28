package com.snoworca.cson.serializer;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class SchemaField implements SchemaNode {

    private static final AtomicInteger LAST_ID = new AtomicInteger(1);

    private final int id = LAST_ID.getAndIncrement();

    final TypeElement parentsTypeElement;
    final TypeElement objectTypeElement;

    final Field field;
    final String path;
    final Types type;

    final String comment;
    final String afterComment;

    private final boolean isPrimitive;


    private SchemaField parentFieldRack;
    final Class<?> fieldType;


    static SchemaField of(TypeElement typeElement, Field field) {
        CSONValue csonValue = field.getAnnotation(CSONValue.class);
        if(csonValue == null) return null;
        String key = csonValue.key();
        if(key == null || key.isEmpty()) key = csonValue.value();
        if(key == null || key.isEmpty()) key = field.getName();

        if(Collection.class.isAssignableFrom(field.getType())) {
            return new SchemaFieldArray(typeElement, field, key);
        } else if(java.util.Map.class.isAssignableFrom(field.getType())) {
            return new SchemaFieldMap(typeElement, field, key);
        }
        else {
            return new SchemaFieldNormal(typeElement, field, key);
        }

    }


    SchemaField(TypeElement parentsTypeElement, Field field, String path) {
        this.field = field;
        field.setAccessible(true);
        this.path = path;
        this.fieldType = field.getType();
        this.parentsTypeElement = parentsTypeElement;
        this.type = Types.of(field.getType());

        CSONValue csonValue = field.getAnnotation(CSONValue.class);
        String comment = csonValue.comment();
        String afterComment = csonValue.commentAfterKey();
        this.comment = comment.isEmpty() ? null : comment;
        this.afterComment = afterComment.isEmpty() ? null : afterComment;

      if(this.type == Types.Object) {
            this.objectTypeElement = TypeElements.getInstance().getTypeInfo(field.getType());
        } else {
            this.objectTypeElement = null;
        }


        if(this.field.getType().isArray() && this.type != Types.ByteArray) {
            throw new CSONObjectException("Array type '" + this.field.getName() + "' is not supported");
        }
        if(this.type == Types.Object && this.field.getType().getAnnotation(CSON.class) == null)  {
            throw new CSONObjectException("Object field '" + this.field.getName() + "' is not annotated with @CSON");
        }
        this.isPrimitive = field.getType().isPrimitive();
    }


    Object newInstance() {
        if(objectTypeElement == null) return null;
        return objectTypeElement.newInstance();
    }


    boolean isPrimitive() {
        return isPrimitive;
    }


    String getComment() {
        return comment;
    }

    String getAfterComment() {
        return afterComment;
    }

    Types getType() {
        return type;
    }

    int getId() {
        return id;
    }

    String getPath() {
        return path;
    }

    Class<?> getFieldType() {
        return fieldType;
    }

    SchemaField getParentField() {
        return parentFieldRack;
    }


    Field getField() {
        return field;
    }


    void setParentFiled(SchemaField parent) {
        this.parentFieldRack = parent;
    }


    Object getValue(Object parent) {
        try {
            return field.get(parent);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    Object setValue(Object parent, Object value) {
        try {
            field.set(parent, value);
            return value;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }


    Object setValue(Object parent, short value) {
        try {
            field.setShort(parent, value);
            return value;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    Object setValue(Object parent, int value) {
        try {
            field.setInt(parent, value);
            return value;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    Object setValue(Object parent, long value) {
        try {
            field.setLong(parent, value);
            return value;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    Object setValue(Object parent, float value) {
        try {
            field.setFloat(parent, value);
            return value;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    Object setValue(Object parent, double value) {
        try {
            field.setDouble(parent, value);
            return value;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    Object setValue(Object parent, boolean value) {
        try {
            field.setBoolean(parent, value);
            return value;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    Object setValue(Object parent, char value) {
        try {
            field.setChar(parent, value);
            return value;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

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
