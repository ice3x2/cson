package com.snoworca.cson.serializer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

abstract class SchemaValueAbs implements SchemaNode, SchemaValue {

    private static final AtomicInteger LAST_ID = new AtomicInteger(1);

    private final int id = LAST_ID.getAndIncrement();

    final TypeElement parentsTypeElement;
    final TypeElement objectTypeElement;

    final String path;
    final Types type;

    private final boolean isPrimitive;

    //private final boolean isMapField;

    private SchemaValueAbs parentFieldRack;
    final Class<?> valueTypeClass;

    private ArrayList<SchemaValueAbs> duplicatedSchemaValueAbsList = null;


    static SchemaValueAbs of(TypeElement typeElement, Field field) {
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

    static SchemaValueAbs of(TypeElement typeElement, Method method) {
        CSONValueGetter getter = method.getAnnotation(CSONValueGetter.class);
        CSONValueSetter setter = method.getAnnotation(CSONValueSetter.class);
        if(setter == null && getter == null) return null;
        if(SchemaMethodForArrayType.isCollectionTypeParameterOrReturns(method)) {
            return new SchemaMethodForArrayType(typeElement, method);
        }
        return new SchemaMethod(typeElement, method);
    }


    boolean appendDuplicatedSchemaValue(SchemaValueAbs node) {
        if(node.parentsTypeElement != this.parentsTypeElement) {
            return false;
        }
        else if(node instanceof SchemaArrayValue && !(this instanceof SchemaArrayValue) ||
                !(node instanceof SchemaArrayValue) && this instanceof SchemaArrayValue) {
            //TODO 예외 발생 시켜야한다.
            return false;
        }
        else if(node instanceof SchemaArrayValue && this instanceof SchemaArrayValue) {
            SchemaArrayValue nodeArray = (SchemaArrayValue) node;
            SchemaArrayValue thisArray = (SchemaArrayValue) this;
            if(nodeArray.getCollectionItems().size() != thisArray.getCollectionItems().size()) {
                //TODO 예외 발생 시켜야한다.
            }
        }

       

        if(this.duplicatedSchemaValueAbsList == null) {
            this.duplicatedSchemaValueAbsList = new ArrayList<>();
        }
        this.duplicatedSchemaValueAbsList.add(node);
        return true;
    }

    @SuppressWarnings("unchecked")
    <T extends SchemaValueAbs> List<T> getDuplicatedSchemaValueList() {
        return (List<T>) this.duplicatedSchemaValueAbsList;
    }


    SchemaValueAbs(TypeElement parentsTypeElement, String path, Class<?> valueTypeClass) {

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
    final <T extends SchemaValueAbs> T getParentField() {
        return (T) parentFieldRack;
    }



    final void setParentFiled(SchemaValueAbs parent) {
        this.parentFieldRack = parent;
    }


    @Override
    public Object getValue(Object parent) {
        Object value = null;
        if(this.duplicatedSchemaValueAbsList != null) {
            value = null;
            int index = this.duplicatedSchemaValueAbsList.size() - 1;
            while(value == null && index > -1) {
                SchemaValueAbs duplicatedSchemaValueAbs = this.duplicatedSchemaValueAbsList.get(index);
                value = duplicatedSchemaValueAbs.onGetValue(parent);
                if(!this.equalsValueType(duplicatedSchemaValueAbs)) {
                    if(this instanceof SchemaArrayValue) {
                        SchemaArrayValue schemaArrayValue = (SchemaArrayValue) duplicatedSchemaValueAbs;
                        try {
                            value = Utils.convertCollectionValue(value, schemaArrayValue.getCollectionItems(),schemaArrayValue.getValueType());
                            if(value != null) {
                                return value;
                            }
                        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
                            value = null;
                        }
                    } else {
                        value = Utils.convertValue(value, duplicatedSchemaValueAbs.type);
                    }
                    value = Utils.convertValue(value, duplicatedSchemaValueAbs.type);
                }
                index--;

            }
            if(value != null) {
                return value;
            }
        }
        value = onGetValue(parent);
        if(value == null) {
            return null;
        }
        return value;

    }

    @Override
    public void setValue(Object parent, Object value) {
        if(this.duplicatedSchemaValueAbsList != null) {
            for(SchemaValueAbs duplicatedSchemaValueAbs : this.duplicatedSchemaValueAbsList) {
                Object setValue = value;
                if(!this.equalsValueType(duplicatedSchemaValueAbs)) {
                    if(this instanceof SchemaArrayValue) {
                        SchemaArrayValue schemaArrayValue = (SchemaArrayValue) duplicatedSchemaValueAbs;
                        try {
                            setValue = Utils.convertCollectionValue(value, schemaArrayValue.getCollectionItems(),schemaArrayValue.getValueType());
                        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
                            setValue = null;
                        }
                    } else {
                        setValue = Utils.convertValue(value, duplicatedSchemaValueAbs.type);
                    }
                }
                duplicatedSchemaValueAbs.setValue(parent, setValue);
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


    boolean equalsValueType(SchemaValueAbs schemaValueAbs) {
        return schemaValueAbs.getValueTypeClass() == this.valueTypeClass;
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
