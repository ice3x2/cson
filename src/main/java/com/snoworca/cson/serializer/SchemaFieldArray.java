package com.snoworca.cson.serializer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

@SuppressWarnings("ClassEscapesDefinedScope")
public class SchemaFieldArray extends SchemaField {

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

    protected TypeElement getObjectValueTypeElement() {
        return objectValueTypeElement;
    }

    protected List<CollectionItems> getCollectionItems() {
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


    private static Constructor<?> constructorOfCollection(Class<?> type) {
        try {
            if (type.isInterface() && SortedSet.class.isAssignableFrom(type)) {
                return TreeSet.class.getConstructor();
            } else if (type.isInterface() && Set.class.isAssignableFrom(type)) {
                return HashSet.class.getConstructor();
            } else if (type.isInterface() && (AbstractQueue.class.isAssignableFrom(type) || Deque.class.isAssignableFrom(type) || Queue.class.isAssignableFrom(type))) {
                return ArrayDeque.class.getConstructor();
            } else if (type.isInterface() && (List.class.isAssignableFrom(type) || Collection.class.isAssignableFrom(type)) || type == Collection.class) {
                return ArrayList.class.getConstructor();
            } else if (type.isInterface() && (NavigableSet.class.isAssignableFrom(type) || SortedSet.class.isAssignableFrom(type))) {
                return TreeSet.class.getConstructor();
            }
            return type.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new CSONObjectException("Collection field '" + type.getName() + "' has no default constructor");
        }
    }


    @Override
    public SchemaNode copyNode() {

        return new SchemaFieldArray(parentsTypeElement, field, path);
    }

    @Override
    public Object newInstance() {
        return collectionBundles.get(0).newInstance();
    }

    protected static class CollectionItems {


        CollectionItems(ParameterizedType type) {
            this.collectionType = (Class<?>) type.getRawType();
            //noinspection unchecked
            this.collectionConstructor = (Constructor<? extends Collection<?>>) constructorOfCollection(collectionType);
            Type[] actualTypes =  type.getActualTypeArguments();
            if(actualTypes.length > 0 && actualTypes[0] instanceof Class<?>) {
                this.valueClass = (Class<?>) type.getActualTypeArguments()[0];
            } else {
                this.valueClass = null;
            }
        }
        protected final Constructor<? extends Collection<?>> collectionConstructor;
        protected final Class<?> collectionType;
        protected Class<?> valueClass;



        protected Collection<?> newInstance() {
            try {
                return collectionConstructor.newInstance();
            } catch (Exception e) {
                throw new CSONObjectException("Collection field '" + collectionType.getName() + "' has no default constructor");
            }
        }






    }

}
