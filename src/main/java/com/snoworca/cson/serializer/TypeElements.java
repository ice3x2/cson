package com.snoworca.cson.serializer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TypeElements {

    private static TypeElements instance = new TypeElements();

    private Map<Class<?>, TypeElement> typeInfoMap = new ConcurrentHashMap<>();

    private TypeElements() {
    }

    public static TypeElements getInstance() {
        return instance;
    }

    protected TypeElement getTypeInfo(Class<?> type) {
        TypeElement typeInfo = typeInfoMap.get(type);
        if(typeInfo == null) {
            typeInfo = TypeElement.create(type);
            typeInfoMap.put(type, typeInfo);
        }
        return typeInfo;
    }


}
