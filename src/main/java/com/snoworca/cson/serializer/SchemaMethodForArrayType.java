package com.snoworca.cson.serializer;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

class SchemaMethodForArrayType extends SchemaMethod implements SchemaArrayValue {


    static boolean isCollectionTypeParameterOrReturns(Method method) {
        CSONValueGetter getter = method.getAnnotation(CSONValueGetter.class);
        CSONValueSetter setter = method.getAnnotation(CSONValueSetter.class);

        if(getter != null && Collection.class.isAssignableFrom(method.getReturnType())) {
            return true;
        }
        Class<?>[] types = method.getParameterTypes();
        Class<?> returnType = method.getParameterTypes()[0];
        if(setter != null && types.length == 1 && Collection.class.isAssignableFrom(types[0])) {
            return true;
        }
        return false;
    }

    private final List<CollectionItems> collectionBundles;
    protected final Types ValueType;
    private final TypeElement objectValueTypeElement;

    SchemaMethodForArrayType(TypeElement parentsTypeElement, Method method) {
        super(parentsTypeElement, method);

        this.collectionBundles = getGenericType(method, getMethodType() == MethodType.Getter);
        Class<?> valueClass = this.collectionBundles.get(collectionBundles.size() - 1).valueClass;
        ValueType = Types.of(valueClass);
        if (ValueType == Types.Object) {
            objectValueTypeElement = TypeElements.getInstance().getTypeInfo(valueClass);
        } else {
            objectValueTypeElement = null;
        }

    }





    @SuppressWarnings("DuplicatedCode")
    private List<CollectionItems> getGenericType(Method method, boolean isGetter) {
        Type genericFieldType = isGetter ? method.getGenericReturnType() : method.getGenericParameterTypes()[0];
        ArrayList<CollectionItems> result = new ArrayList<>();
        if (genericFieldType instanceof ParameterizedType) {
            CollectionItems collectionBundle = new CollectionItems((ParameterizedType) genericFieldType);
            result.add(collectionBundle);
            return getGenericType(result,(ParameterizedType)genericFieldType);
        } else  {
            throw new CSONObjectException(isGetter ? "Collection getter method '" + method.getName() + "' is Raw type. Please use generic type." : "Collection setter method '" + method.getName() + "' is Raw type. Please use generic type.");
        }
    }

    private List<CollectionItems>  getGenericType(List<CollectionItems> collectionBundles, ParameterizedType parameterizedType) {
        Type[] fieldArgTypes = parameterizedType.getActualTypeArguments();
        if(fieldArgTypes.length == 0) {
            throw new CSONObjectException("");
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
                    throw new CSONObjectException("");
                }
                assertValueType((Class<?>)rawType, "");
                collectionBundles.get(collectionBundles.size() - 1).valueClass = (Class<?>)rawType;
                return collectionBundles;
            }
            CollectionItems collectionBundle = new CollectionItems(parameterizedType);
            collectionBundles.add(collectionBundle);
            return getGenericType(collectionBundles,parameterizedType);
        }
        else {
            //fieldArgTypes[0]
            throw new CSONObjectException("");
        }
    }

    @Override
    public Types getValueType() {
        return this.ValueType;
    }

    @Override
    public TypeElement getObjectValueTypeElement() {
        return this.objectValueTypeElement;
    }

    @Override
    public List<CollectionItems> getCollectionItems() {
        return collectionBundles;
    }



    @Override
    boolean equalsValueType(SchemaValueAbs schemaValueAbs) {
        if(!(schemaValueAbs instanceof SchemaArrayValue)) {
            return false;
        }
        if(!SchemaArrayValue.equalsCollectionTypes(this.getCollectionItems(), ((SchemaArrayValue)schemaValueAbs).getCollectionItems())) {
            return false;
        }
        return schemaValueAbs.getValueTypeClass() == this.getValueTypeClass();
    }

}
