package com.snoworca.cson.object;


import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class FieldRack implements SchemaNode {

    private static final AtomicInteger LAST_ID = new AtomicInteger(1);

    private final List<FieldRack> parentFieldRackList = new ArrayList<>();
    private final int id = LAST_ID.getAndIncrement();
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

    public int getId() {
        return id;
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

        if(Collection.class.isAssignableFrom(this.fieldType)) {
            java.lang.reflect.Type[] genericTypes = this.fieldType.getGenericInterfaces();
            if(genericTypes.length == 0) {
                //todo 필드 이름 표시해야함/
                throw new CSONObjectException("Raw Collection type is not supported. Be sure to use generic type.");
            }
            java.lang.reflect.Type genericType = genericTypes[0];
            if(genericType instanceof java.lang.reflect.ParameterizedType) {
                java.lang.reflect.ParameterizedType parameterizedType = (java.lang.reflect.ParameterizedType) genericType;
                java.lang.reflect.Type[] actualTypes = parameterizedType.getActualTypeArguments();
                if(actualTypes.length == 0) {
                    //todo 필드 이름 표시해야함/
                    throw new CSONObjectException("Raw Collection type is not supported. Be sure to use generic type.");
                }
                java.lang.reflect.Type actualType = actualTypes[0];
                Class fieldArgClass = (Class) actualType;
                if(actualType instanceof Class) {
                    Class<?> actualClass = (Class<?>) actualType;
                    if(actualClass.getAnnotation(CSON.class) == null) {
                        //todo 필드 이름 표시해야함/
                       // throw new CSONObjectException("Collection type is not annotated with @CSON");
                    }
                } else {
                    //todo 필드 이름 표시해야함/
                   // throw new CSONObjectException("Collection type is not annotated with @CSON");
                }
            } else {
                //todo 필드 이름 표시해야함/
              //  throw new CSONObjectException("Raw Collection type is not supported. Be sure to use generic type.");
            }




        }


    }

    protected Class<?> getFieldType() {
        return fieldType;
    }


    public Types getType() {
        return type;
    }

    public FieldRack copy() {
        FieldRack fieldRack = new FieldRack(typeElement, field, path, isByteArray);
        fieldRack.parentFieldRack = parentFieldRack;
        return fieldRack;
    }

    protected FieldRack getParentFieldRack() {
        return parentFieldRack;
    }

    protected void setParentFiledRack(FieldRack parent) {
        this.parentFieldRack = parent;
    }

    protected Field getField() {
        return field;
    }

    public Object getValue(Object parent) {
        try {
            return field.get(parent);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public SchemaNode copyNode() {
        FieldRack fieldRack = copy();
        return fieldRack;
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
