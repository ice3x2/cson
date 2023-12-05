package com.snoworca.cson.serializer;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

class SchemaFieldArray extends SchemaField implements SchemaArrayValue {

    //private final Types valueType;

    private final List<CollectionItems> collectionBundles;
    protected final Types ValueType;
    private final TypeElement objectValueTypeElement;


    protected SchemaFieldArray(TypeElement typeElement, Field field, String path) {
        super(typeElement, field, path);
        this.collectionBundles = getGenericType();

        Class<?> valueClass = this.collectionBundles.get(collectionBundles.size() - 1).valueClass;
        ValueType = Types.of(valueClass);
        if (ValueType == Types.Object) {
            objectValueTypeElement = TypeElements.getInstance().getTypeInfo(valueClass);
        } else {
            objectValueTypeElement = null;
        }

    }



    @Override
    public TypeElement getObjectValueTypeElement() {
        return objectValueTypeElement;
    }

    @Override
    public List<CollectionItems> getCollectionItems() {
        return collectionBundles;
    }

    private List<CollectionItems> getGenericType() {
        Type genericFieldType = field.getGenericType();
        ArrayList<CollectionItems> result = new ArrayList<>();
        if (genericFieldType instanceof ParameterizedType) {
            CollectionItems collectionBundle = new CollectionItems((ParameterizedType) genericFieldType);
            result.add(collectionBundle);
            return getGenericType(result,(ParameterizedType)genericFieldType);
        } else  {
            throw new CSONObjectException("Collection field '" + field.getName() + "' is Raw type. Please use generic type.");
        }
    }

    private List<CollectionItems>  getGenericType(List<CollectionItems> collectionBundles, ParameterizedType parameterizedType) {
        Type[] fieldArgTypes = parameterizedType.getActualTypeArguments();
        if(fieldArgTypes.length == 0) {
            throw new CSONObjectException("Collection field '" + field.getName() + "' is Raw type. Please use generic type.");
        }
        if(fieldArgTypes[0] instanceof Class<?>) {
            return collectionBundles;
        }
        else if (fieldArgTypes[0] instanceof ParameterizedType) {
            parameterizedType = (ParameterizedType) fieldArgTypes[0];
            Type rawType = parameterizedType.getRawType();
            if(!(rawType instanceof Class<?>) || !Collection.class.isAssignableFrom((Class<?>)rawType)) {
                assert rawType instanceof Class<?>;
                if(Map.class.isAssignableFrom((Class<?>) rawType)) {
                    throw new CSONObjectException("java.util.Map type cannot be directly used as an element of Collection. Please create a class that wraps your Map and use it as an element of the Collection of field. (Field path: " + field.getDeclaringClass().getName() + "." + field.getName() + ")");
                }
                assertValueType((Class<?>)rawType, field.getDeclaringClass().getName() + "." + field.getName());
                collectionBundles.get(collectionBundles.size() - 1).valueClass = (Class<?>)rawType;
                return collectionBundles;
            }
            CollectionItems collectionBundle = new CollectionItems(parameterizedType);
            collectionBundles.add(collectionBundle);
            return getGenericType(collectionBundles,parameterizedType);
        }
        else {
            //fieldArgTypes[0]
            throw new CSONObjectException("Collection field '" + field.getDeclaringClass().getName() + "." + field.getName() + "' is unknown or RAW type. Please use generic type.");
        }
    }




    @Override
    public Types getValueType() {
        return ValueType;
    }

    @Override
    public SchemaNode copyNode() {
        return new SchemaFieldArray(parentsTypeElement, field, path);
    }

    @Override
    public Object newInstance() {
        return collectionBundles.get(0).newInstance();
    }


    @Override
    public Object getValue(Object parent) {
        return onGetValue(parent);
    }

    @Override
    public void setValue(Object parent, Object value) {
        onSetValue(parent, value);
    }

    @Override
    boolean equalsValueType(SchemaValueAbs schemaValueAbs) {
        if(!(schemaValueAbs instanceof SchemaArrayValue)) {
            return false;
        }
        SchemaArrayValue.equalsCollectionTypes(this.getCollectionItems(), ((SchemaArrayValue)schemaValueAbs).getCollectionItems());
        return schemaValueAbs.getValueTypeClass() == this.getValueTypeClass();
    }

}
