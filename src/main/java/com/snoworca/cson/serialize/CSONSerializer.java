package com.snoworca.cson.serialize;

import com.snoworca.cson.CSONArray;
import com.snoworca.cson.CSONObject;

import javax.swing.text.html.CSS;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class CSONSerializer {

    public static CSONObject toCSONObject(Object object) {
        return toCSONObject(object, 0);
    }


    private static CSONObject toCSONObject(Object object, int index) {
        TypeInfo typeInfo = TypeInfoRack.getInstance().getTypeInfo(object.getClass());
        CSONObject csonObject = new CSONObject();
        serialize(object, typeInfo, csonObject);
        return csonObject;
    }

    public static CSONArray toCSONArray(Collection<?> collection) {
        CSONArray csonArray = new CSONArray();
        for(Object object : collection) {
            if(object instanceof Number || object instanceof Character ||
                    object instanceof Boolean || object instanceof String || object instanceof byte[]) {
                csonArray.add(object);
            }
            else if(object instanceof CharSequence) {
                csonArray.add(object.toString());
            }
            else if(object instanceof Collection) {
                CSONArray childArray = toCSONArray((Collection<?>)object);
                csonArray.add(childArray);
            }
            else if(object instanceof Map) {
                //TODO
                String aa ;
            }

            else {
                CSONObject csonObject = toCSONObject(object);
                csonArray.add(csonObject);
            }
        }
        return csonArray;
    }

    private static void serialize(Object object, TypeInfo typeInfo, CSONObject csonObject) {
        ArrayList<FieldInfo> fieldInfos = typeInfo.getFieldInfos();
        for (FieldInfo fieldInfo : fieldInfos) {
           if(fieldInfo.isError()) continue;
           else if(fieldInfo.isCSONObject() || fieldInfo.isMap()) {
               injectCSONObject(fieldInfo,object, csonObject);
           }
           else if(fieldInfo.isArray()) {
               FieldInfo componentInfo = fieldInfo.getComponentInfo(0);
               if(componentInfo.getType() == DataType.TYPE_BYTE && !fieldInfo.isByteArrayToCSONArray()) {
                   injectCSONValue(fieldInfo,object, csonObject);
                   continue;
               }
               if(fieldInfo.isByteArrayToCSONArray()) {
                   injectCSONArray(fieldInfo,object, csonObject);
                     continue;
               }
               injectCSONArray(fieldInfo, object, csonObject);
           }
           else if(fieldInfo.isCollection()) {
               injectCSONArray(fieldInfo, object, csonObject);
           }
           else {
               injectCSONValue(fieldInfo,object, csonObject);
           }
        }

    }


    private static void injectCSONArray(FieldInfo fieldInfo,Object object, CSONObject csonObject) {
        Field field = fieldInfo.getField();
        Object value = null;
        try {
            value = field.get(object);
            if(value == null) {
                csonObject.put(fieldInfo.getName(), null);
                return;
            }
            CSONArray csonArray =  fieldInfo.isArray() ? arrayObjectToCSONArray(fieldInfo, value) :
                                   fieldInfo.isCollection() ? collectionToCSONArray(fieldInfo, (Collection<?>)value, 0) : null;
            csonObject.put(fieldInfo.getName(), csonArray);
            return;
        } catch (IllegalAccessException e) {}
        csonObject.put(fieldInfo.getName(), null);
    }



    public static void injectFromMap(FieldInfo fieldInfo, Map<?, ?> map, CSONObject csonObject, int index) {
        Set<? extends Map.Entry<?, ?>> entrySet = map.entrySet();
        byte fieldType = fieldInfo.getType();
        boolean isNestedCollection = fieldInfo.isNestedCollection();
        FieldInfo componentInfo = fieldInfo.getComponentInfo(index);
        fieldType = componentInfo.getType();
        for(Map.Entry<?, ?> entry : entrySet) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            if(DataType.isJsonDefaultType(fieldType) && !componentInfo.isArray())  {
                csonObject.put(key.toString(), value);
            } else if(value instanceof Map && isNestedCollection)  {
                CSONObject subObject = new CSONObject();
                ++index;
                injectFromMap(componentInfo, (Map<?, ?>)value, subObject, index);
                csonObject.put(key.toString(), subObject);
                --index;
            } else if(value instanceof Collection && isNestedCollection) {
                 ++index;
                CSONArray subArray = collectionToCSONArray(componentInfo,(Collection<?>)value, index);
                csonObject.put(key.toString(), subArray);
                --index;
            }  else if(componentInfo.isArray()) {
                CSONArray subArray = arrayObjectToCSONArray(componentInfo,value);
                csonObject.put(key.toString(), subArray);
            } else {
                try {
                    CSONObject subObject = CSONSerializer.toCSONObject(value);
                    csonObject.put(key.toString(), subObject);
                } catch (StackOverflowError e) {
                    // todo 메시지 출력
                    throw new RuntimeException(e);
                }
            }


        }
    }

    private static CSONArray collectionToCSONArray(FieldInfo fieldInfo,Collection<?> collectionObject, int index) {
        CSONArray csonArray = new CSONArray();
        FieldInfo componentInfo = fieldInfo.getComponentInfo(index);
        byte componentType = componentInfo.getType();
        if(componentType == DataType.TYPE_CSON_OBJECT) {
            for(Object value : collectionObject) {
                CSONObject csonObject = CSONSerializer.toCSONObject(value);
                csonArray.put(csonObject);
            }
        } else if(componentType < 0) {
            for(Object value : collectionObject) {
                if(value.getClass().getAnnotation(Cson.class) != null) {
                    CSONObject csonObject = CSONSerializer.toCSONObject(value);
                    csonArray.put(csonObject);
                }
                else if(value instanceof Collection) {
                    CSONArray childArray = toCSONArray((Collection<?>) value);
                    csonArray.put(childArray);
                }
                else {
                    csonArray.put(value);
                }
            }
        } else if(componentType == DataType.TYPE_COLLECTION) {
            for(Object value : collectionObject) {
                Collection<?> collectionValue = (Collection<?>)value;
                CSONArray csonArrayValue = collectionToCSONArray(componentInfo, collectionValue, index + 1);
                csonArrayValue.put(csonArrayValue);
            }
        } else if(componentType == DataType.TYPE_MAP) {
            for(Object value : collectionObject) {
                CSONObject csonObject = new CSONObject();
                injectFromMap(componentInfo, (Map<?, ?>)value, csonObject, index + 1);
                csonArray.put(csonObject);
            }
        }
        else {
            for(Object value : collectionObject) {
                csonArray.put(value);
            }
        }
        return csonArray;
    }



    private static CSONArray arrayObjectToCSONArray(FieldInfo info, Object arrayObj) {
        FieldInfo componentType = info.getComponentInfo(0);
        byte type = componentType.getType(); //info.getType();

        CSONArray csonArray = new CSONArray();
        if(info.isPrimitive()) {
            if (type == DataType.TYPE_BYTE) {
                byte[] array = (byte[]) arrayObj;
                for (int i = 0; i < array.length; i++) {
                    csonArray.put(array[i]);
                }
            } else if (type == DataType.TYPE_BOOLEAN) {
                boolean[] array = (boolean[]) arrayObj;
                for (int i = 0; i < array.length; i++) {
                    csonArray.put(array[i]);
                }
            } else if (type == DataType.TYPE_SHORT) {
                short[] array = (short[]) arrayObj;
                for (int i = 0; i < array.length; i++) {
                    csonArray.put(array[i]);
                }
            } else if (type == DataType.TYPE_CHAR) {
                char[] array = (char[]) arrayObj;
                for (int i = 0; i < array.length; i++) {
                    csonArray.put(array[i]);
                }
            } else if (type == DataType.TYPE_INT) {
                int[] array = (int[]) arrayObj;
                for (int i = 0; i < array.length; i++) {
                    csonArray.put(array[i]);
                }
            } else if (type == DataType.TYPE_LONG) {
                long[] array = (long[]) arrayObj;
                for (int i = 0; i < array.length; i++) {
                    csonArray.put(array[i]);
                }
            } else if (type == DataType.TYPE_FLOAT) {
                float[] array = (float[]) arrayObj;
                for (int i = 0; i < array.length; i++) {
                    csonArray.put(array[i]);
                }
            } else if (type == DataType.TYPE_DOUBLE) {
                double[] array = (double[]) arrayObj;
                for (int i = 0; i < array.length; i++) {
                    csonArray.put(array[i]);
                }
            }
        } else {
            if (type == DataType.TYPE_BYTE) {
                Byte[] array = (Byte[]) arrayObj;
                for (int i = 0; i < array.length; i++) {
                    csonArray.put(array[i]);
                }
            } else if (type == DataType.TYPE_BOOLEAN) {
                Boolean[] array = (Boolean[]) arrayObj;
                for (int i = 0; i < array.length; i++) {
                    csonArray.put(array[i]);
                }
            } else if (type == DataType.TYPE_SHORT) {
                Short[] array = (Short[]) arrayObj;
                for (int i = 0; i < array.length; i++) {
                    csonArray.put(array[i]);
                }
            } else if (type == DataType.TYPE_CHAR) {
                Character[] array = (Character[]) arrayObj;
                for (int i = 0; i < array.length; i++) {
                    csonArray.put(array[i]);
                }
            } else if (type == DataType.TYPE_INT) {
                Integer[] array = (Integer[]) arrayObj;
                for (int i = 0; i < array.length; i++) {
                    csonArray.put(array[i]);
                }
            } else if (type == DataType.TYPE_LONG) {
                Long[] array = (Long[]) arrayObj;
                for (int i = 0; i < array.length; i++) {
                    csonArray.put(array[i]);
                }
            } else if (type == DataType.TYPE_FLOAT) {
                Float[] array = (Float[]) arrayObj;
                for (int i = 0; i < array.length; i++) {
                    csonArray.put(array[i]);
                }
            } else if (type == DataType.TYPE_DOUBLE) {
                Double[] array = (Double[]) arrayObj;
                for (int i = 0; i < array.length; i++) {
                    csonArray.put(array[i]);
                }
            }
            else if(type == DataType.TYPE_STRING) {
                String[] array = (String[])arrayObj;
                for(int i = 0; i < array.length; i++) {
                    csonArray.put(array[i]);
                }
            }
            else if(type == DataType.TYPE_CSON_OBJECT) {
                int length = Array.getLength(arrayObj);
                for(int i = 0; i < length; i++) {
                    Object obj = Array.get(arrayObj,i);
                    if(obj == null) {
                        csonArray.put(null);
                    } else {
                        CSONObject value = CSONSerializer.toCSONObject(obj);
                        csonArray.put(value);
                    }
                }
            }
            else if(type == DataType.TYPE_MAP) {
                int length = Array.getLength(arrayObj);
                for(int i = 0; i < length; i++) {
                    Object obj = Array.get(arrayObj,i);
                    if(obj == null) {
                        csonArray.put(null);
                    } else {
                        CSONObject csonObject = new CSONObject();
                        injectFromMap(info,(Map)obj, csonObject, i);
                        csonArray.put(csonObject);
                    }
                }
            }
        }
        return csonArray;
    }


    private static void injectCSONObject(FieldInfo fieldInfo,Object object, CSONObject csonObject) {
        Field field = fieldInfo.getField();
        Object value = null;
        try {
            value = field.get(object);
            if(value != null) {
                if(value instanceof Map) {
                    injectFromMap(fieldInfo,(Map)value, csonObject, 0);
                } else {
                    value = CSONSerializer.toCSONObject(value);
                }
            }
        } catch (IllegalAccessException e) {}
        csonObject.put(fieldInfo.getName(), value);
    }

    private static void injectCSONValue(FieldInfo fieldInfo,Object object, CSONObject csonObject) {
        Field field = fieldInfo.getField();
        Object value = null;
        try {
            value = field.get(object);
        } catch (IllegalAccessException e) {}
        csonObject.put(fieldInfo.getName(), value);
    }



}
