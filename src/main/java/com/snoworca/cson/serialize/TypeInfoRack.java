package com.snoworca.cson.serialize;

import java.util.concurrent.ConcurrentHashMap;

public class TypeInfoRack {

    private final static TypeInfoRack instance = new TypeInfoRack();

    public static TypeInfoRack getInstance() {
        return instance;
    }

    private TypeInfoRack() {
    }

    private final ConcurrentHashMap<Class<?>, TypeInfo> typeInfoMap = new ConcurrentHashMap<>();

    public TypeInfo getTypeInfo(Class<?> type) {
        TypeInfo typeInfo = typeInfoMap.get(type);
        if(typeInfo == null) {
            typeInfo = TypeInfo.create(type);
            typeInfoMap.put(type, typeInfo);
        }
        return typeInfo;
    }



}
