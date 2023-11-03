package com.snoworca.cson.object;


import java.lang.reflect.Field;

public class FieldRack  {
    private final Field field;
    private final String path;
    private final boolean isPrimitive;
    private final boolean isByteArray;
    private final TypeElement typeElement;

    private FieldRack parentFieldRack;

    private final Class<?> fieldType;

    private final Types type;

    public static FieldRack of(TypeElement typeElement, Field field) {
        CSONValue csonValue = field.getAnnotation(CSONValue.class);
        if(csonValue == null) return null;
        String key = csonValue.key();
        if(key == null || key.isEmpty()) key = csonValue.value();
        if(key == null || key.isEmpty()) key = field.getName();
        boolean isByteArrayToCSONArray = csonValue.byteArrayToCSONArray();
        return new FieldRack(typeElement, field, key, isByteArrayToCSONArray);
    }

    public String getPath() {
        return path;
    }

    private FieldRack(TypeElement typeElement, Field field, String path, boolean isByteArray) {
        this.field = field;
        field.setAccessible(true);
        this.path = path;
        this.fieldType = field.getType();
        this.isByteArray = isByteArray;
        this.typeElement = typeElement;
        this.isPrimitive = field.getType().isPrimitive();
        this.type = Types.of(field.getType());
        if(this.type == Types.Object && this.field.getType().getAnnotation(CSON.class) == null)  {
            throw new CSONObjectException("Field type is not annotated with @CSON");
        }

    }

    protected Class<?> getFieldType() {
        return fieldType;
    }


    public Types getType() {
        return type;
    }

    public FieldRack copy() {
        return new FieldRack(typeElement, field, path, isByteArray);
    }

    protected FieldRack getParentFieldRack() {
        return parentFieldRack;
    }

    protected void setParentFiledRack(FieldRack parent) {
        this.parentFieldRack = parent;
    }


    public Object getValue(Object parent) {
        try {
            return field.get(parent);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

}
