package com.snoworca.cson.serializer;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

class SchemaMethodForArrayType extends SchemaMethod implements ISchemaArrayValue {


    @SuppressWarnings("DuplicatedCode")
    static boolean isCollectionTypeParameterOrReturns(Method method) {
        CSONValueGetter getter = method.getAnnotation(CSONValueGetter.class);
        CSONValueSetter setter = method.getAnnotation(CSONValueSetter.class);
        if(getter != null && Collection.class.isAssignableFrom(method.getReturnType())) {
            return true;
        }
        Class<?>[] types = method.getParameterTypes();
        if(setter != null && types.length == 1 && Collection.class.isAssignableFrom(types[0])) {
            return true;
        }
        return false;
    }

    private final List<CollectionItems> collectionBundles;
    protected final Types endpointValueType;
    private final TypeElement objectValueTypeElement;

    SchemaMethodForArrayType(TypeElement parentsTypeElement, Method method) {
        super(parentsTypeElement, method);

        boolean isGetter = getMethodType() == MethodType.Getter;
        Type genericFieldType = isGetter ? method.getGenericReturnType() : method.getGenericParameterTypes()[0];
        String methodPath = method.getDeclaringClass().getName() + "." + method.getName();
        if(isGetter) {
            methodPath += "() <return: " + method.getReturnType().getName() + ">";
        }
        else {
            methodPath += "(" + method.getParameterTypes()[0].getName() + ") <return: " + method.getReturnType().getName() + ">";
        }

        this.collectionBundles = ISchemaArrayValue.getGenericType(genericFieldType, methodPath);
        Class<?> valueClass = this.collectionBundles.get(collectionBundles.size() - 1).valueClass;
        endpointValueType = Types.of(valueClass);
        if (endpointValueType == Types.Object) {
            objectValueTypeElement = TypeElements.getInstance().getTypeInfo(valueClass);
        } else {
            objectValueTypeElement = null;
        }

    }



    @Override
    public Types getEndpointValueType() {
        return this.endpointValueType;
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
        if(!(schemaValueAbs instanceof ISchemaArrayValue)) {
            return false;
        }
        if(!ISchemaArrayValue.equalsCollectionTypes(this.getCollectionItems(), ((ISchemaArrayValue)schemaValueAbs).getCollectionItems())) {
            return false;
        }
        if(this.endpointValueType != ((ISchemaArrayValue)schemaValueAbs).getEndpointValueType()) {
            return false;
        }
        return super.equalsValueType(schemaValueAbs);
    }

}
