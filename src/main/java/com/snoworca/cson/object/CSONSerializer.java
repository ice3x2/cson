package com.snoworca.cson.object;

import com.snoworca.cson.CSONObject;

public class CSONSerializer {

    private CSONSerializer() {}

    public static CSONObject serialize(Object obj) {
        Class<?> clazz = obj.getClass();
        TypeElement typeElement = TypeElements.getInstance().getTypeInfo(clazz);
        return typeElement.serialize(obj);
    }

    public static Object deserialize(CSONObject csonObject) {
        return null;
    }


}
