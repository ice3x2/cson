package com.snoworca.cson.serializer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class SchemaField implements SchemaNode {

    private static final AtomicInteger LAST_ID = new AtomicInteger(1);

    private final int id = LAST_ID.getAndIncrement();

    protected final TypeElement typeElement;

    protected final Field field;
    protected final String path;
    protected final boolean isByteArray;
    protected final Types type;

    private SchemaField parentFieldRack;
    protected final Class<?> fieldType;


    public static SchemaField of(TypeElement typeElement, Field field) {
        CSONValue csonValue = field.getAnnotation(CSONValue.class);
        if(csonValue == null) return null;
        String key = csonValue.key();
        if(key == null || key.isEmpty()) key = csonValue.value();
        if(key == null || key.isEmpty()) key = field.getName();
        boolean isByteArrayToCSONArray = csonValue.byteArrayToCSONArray();

        if(Collection.class.isAssignableFrom(field.getType())) {
            return new SchemaFieldArray(typeElement, field, key, isByteArrayToCSONArray);
        } else {
            return new SchemaFieldNormal(typeElement, field, key, isByteArrayToCSONArray);
        }
    }


    protected SchemaField(TypeElement typeElement, Field field, String path, boolean isByteArray) {
        this.field = field;
        field.setAccessible(true);
        this.path = path;
        this.fieldType = field.getType();
        this.isByteArray = isByteArray;
        this.typeElement = typeElement;
        this.type = Types.of(field.getType());

        Annotation at = this.field.getType().getAnnotation(CSON.class);
        if(this.field.getType().isArray()) {
            throw new CSONObjectException("Array type '" + this.field.getName() + "' is not supported");
        }
        if(this.type == Types.Object && this.field.getType().getAnnotation(CSON.class) == null)  {
            throw new CSONObjectException("Object field '" + this.field.getName() + "' is not annotated with @CSON");
        }
    }



    protected Types getType() {
        return type;
    }

    protected int getId() {
        return id;
    }

    protected String getPath() {
        return path;
    }

    protected Class<?> getFieldType() {
        return fieldType;
    }

    protected SchemaField getParentField() {
        return parentFieldRack;
    }


    protected Field getField() {
        return field;
    }


    protected void setParentFiled(SchemaField parent) {
        this.parentFieldRack = parent;


    }


    protected Object getValue(Object parent) {
        try {
            return field.get(parent);
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
