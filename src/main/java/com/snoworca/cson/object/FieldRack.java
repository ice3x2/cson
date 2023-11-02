package com.snoworca.cson.object;

import java.lang.reflect.Field;

public class FieldRack {
    private final Field field;
    private final String path;
    private final boolean isPremitive;
    private final boolean isByteArray;
    private final TypeElement typeElement;

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
        this.isByteArray = isByteArray;
        this.typeElement = typeElement;
        this.isPremitive = field.getType().isPrimitive();
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
