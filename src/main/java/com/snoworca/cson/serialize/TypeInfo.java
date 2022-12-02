package com.snoworca.cson.serialize;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;

public class TypeInfo {
    private Class<?> type;
    private ArrayList<FieldInfo> fieldInfos;

    protected ArrayList<FieldInfo> getFieldInfos() {
        return fieldInfos;
    }

    public static TypeInfo create(Class<?> type) {
        Cson cson = type.getAnnotation(Cson.class);
        if(cson == null) {
            throw  new InvalidSerializeException("Class " + type.getName() + " is not annotated with @Cson");
        }
        TypeInfo typeInfo = new TypeInfo();
        typeInfo.type = type;
        typeInfo.fieldInfos = extractFieldInfos(typeInfo.type);
        return typeInfo;
    }

    private static ArrayList<FieldInfo> extractFieldInfos(Class<?> type) {
        ArrayList<FieldInfo> fieldInfos = new ArrayList<>();
        List<Field> fields = getAllFields(type);
        HashSet<String> keyNames = new HashSet<>();
        for(Field field : fields) {
            Value valueAnnotation = field.getAnnotation(Value.class);
            if(valueAnnotation == null) continue;

            String keyName = field.getName();
            String keyInAnnotation = valueAnnotation.key();
            if(!keyInAnnotation.isEmpty()) {
                keyName = keyInAnnotation;
            }
            if(keyNames.contains(keyName)) {
                throw new InvalidSerializeException("Duplicate key name: " + keyName);
            }
            keyNames.add(keyName);
            fieldInfos.add(new FieldInfo(field, keyName).setByteArrayToCSONArray(valueAnnotation.byteArrayToCSONArray()));
        }
        return fieldInfos;
    }

    private static List<Field> getAllFields(Class<?> type) {
        LinkedHashMap<String, Field> fieldMap = new LinkedHashMap<>();
        while(type != null && type != Object.class) {
            for(Field field : type.getDeclaredFields()) {
                String fieldName = field.getName();
                if(fieldMap.containsKey(fieldName)) {
                    continue;
                }
                fieldMap.put(fieldName,field);
            }
            type = type.getSuperclass();
        }
        return new ArrayList<>(fieldMap.values());
    }

}
