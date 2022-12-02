package com.snoworca.cson.serialize;

import com.snoworca.cson.CSONArray;
import com.snoworca.cson.CSONObject;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;

public class CSONSerializer {

    public static CSONObject toCSON(Object object) {
        TypeInfo typeInfo = TypeInfoRack.getInstance().getTypeInfo(object.getClass());
        CSONObject csonObject = new CSONObject();
        serialize(object, typeInfo, csonObject);
        return csonObject;
    }

    private static void serialize(Object object, TypeInfo typeInfo, CSONObject csonObject) {
        ArrayList<FieldInfo> fieldInfos = typeInfo.getFieldInfos();
        for (FieldInfo fieldInfo : fieldInfos) {
           if(fieldInfo.isError()) continue;
           else if(fieldInfo.isCSONObject()) {
               injectCSONObject(fieldInfo,object, csonObject);
           }
           else if(fieldInfo.isArray()) {
               if(fieldInfo.getComponentType() == DataType.TYPE_BYTE && !fieldInfo.isByteArrayToCSONArray()) {
                   injectCSONValue(fieldInfo,object, csonObject);
                   continue;
               }
               injectCSONArray(fieldInfo, object, csonObject);
           }
           else if(fieldInfo.isCollection()) {
               injectCSONArray(fieldInfo, object, csonObject);
           } else {
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
                                   fieldInfo.isCollection() ? collectionToCSONArray(fieldInfo, (Collection<?>)value) : null;
            csonObject.put(fieldInfo.getName(), csonArray);
            return;
        } catch (IllegalAccessException e) {}
        csonObject.put(fieldInfo.getName(), null);
    }


    private static CSONArray collectionToCSONArray(FieldInfo info, Collection<?> collectionObject) {
        CSONArray csonArray = new CSONArray();
        byte componentType = info.getComponentType();
        if(componentType == DataType.TYPE_CSON_OBJECT) {
            for(Object value : collectionObject) {
                CSONObject csonObject = CSONSerializer.toCSON(value);
                csonArray.put(csonObject);
            }
        } else if(componentType < 0) {
            for(Object value : collectionObject) {
                if(value.getClass().getAnnotation(Cson.class) != null) {
                    CSONObject csonObject = CSONSerializer.toCSON(value);
                    csonArray.put(csonObject);
                } else {
                    csonArray.put(value);
                }
            }
        } else {
            for(Object value : collectionObject) {
                csonArray.put(value);
            }
        }

        return csonArray;
    }

    private static CSONArray arrayObjectToCSONArray(FieldInfo info, Object arrayObj) {
        byte type = info.getComponentType();
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
                        CSONObject value = CSONSerializer.toCSON(obj);
                        csonArray.put(value);
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
                value = CSONSerializer.toCSON(value);
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
