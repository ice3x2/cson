package com.snoworca.cson.serialize;

import com.snoworca.cson.CSONObject;
import com.snoworca.cson.JSONOptions;
import junit.framework.TestCase;
import org.junit.Test;

import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.Assert.*;

public class TypeInfoRackTest {

    // A-Z, a-z, 0-9 의 랜덤 문자열 생성



    private static Random random = new Random(System.currentTimeMillis());

    @Cson
    public class ClassA {
        @Value
        public int a;
        @Value
        public int b;
        @Value
        public int c;

        @Value("ValueC")
        public int valueC;

        @Value("pathKey")
        public String pathKeyOrigin;

        @Value("key1.key2.key3.pathKey")
        public int pathKey;


        @Value("key1.key2.key3.pathArrayKey[0]")
        public int pathArrayKey;

    }

    @Cson
    public static class ClassB {
        @Value
        public int a;
        @Value(key = "a")
        public int b;
        @Value(key = "a")
        public int c;
    }

    @Cson
    public static class KeyNameValue {
        @Value
        public String key;
        @Value
        public String name;

    }


    @Cson
    public static class ClassCollection {
        @Value
        public Collection<TreeSet<ArrayList<Deque<Set<KeyNameValue>>>>> nestedCollection;

        @Value
        public Collection<ArrayList<Object>> collectionObject;

    }

    @Cson
    public static class ClassRoot {
        @Value
        public ArrayList<Object> list;

    }

    @Test
    public void simpleCollectionTest() {
        TypeInfo typeInfo = TypeInfoRack.getInstance().getTypeInfo(ClassRoot.class);
        FieldInfo fieldInfo = typeInfo.getFieldInfo("list");
        //TODO
        //assertEquals(5, fieldInfo.componentInfoSize());
    }

    @Test
    public void nestedCollectionTest() throws NoSuchMethodException {
        TypeInfo typeInfo = TypeInfoRack.getInstance().getTypeInfo(ClassCollection.class);
        FieldInfo fieldInfo = typeInfo.getFieldInfo("nestedCollection");


        assertEquals(5, fieldInfo.componentInfoSize());

        FieldInfo info = null;
        info = fieldInfo.getComponentInfo(0);
        assertEquals(info.getType(), DataType.TYPE_COLLECTION);
        assertEquals(info.getCollectionConstructor(), ArrayList.class.getConstructor());


        info = fieldInfo.getComponentInfo(1);
        assertEquals(info.getType(), DataType.TYPE_COLLECTION);
        assertEquals(info.getCollectionConstructor(), TreeSet.class.getConstructor());

        info = fieldInfo.getComponentInfo(2);
        assertEquals(info.getType(), DataType.TYPE_COLLECTION);
        assertEquals(info.getCollectionConstructor(), ArrayList.class.getConstructor());

        info = fieldInfo.getComponentInfo(3);
        assertEquals(info.getType(), DataType.TYPE_COLLECTION);
        assertEquals(info.getCollectionConstructor(), ArrayDeque.class.getConstructor());

        info = fieldInfo.getComponentInfo(4);
        assertEquals(info.getType(), DataType.TYPE_CSON_OBJECT);
        assertEquals(info.getCollectionConstructor(), HashSet.class.getConstructor());
        assertEquals(info.getComponentConstructor(), KeyNameValue.class.getConstructor());

        fieldInfo = typeInfo.getFieldInfo("collectionObject");


        assertEquals(2, fieldInfo.componentInfoSize());
        info = fieldInfo.getComponentInfo(0);
        assertEquals(info.getType(), DataType.TYPE_COLLECTION);
        assertEquals(info.getCollectionConstructor(), ArrayList.class.getConstructor());


        info = fieldInfo.getComponentInfo(1);
        assertEquals(info.getType(), DataType.TYPE_UNKNOWN);
        assertEquals(info.getCollectionConstructor(), ArrayList.class.getConstructor());
        assertEquals(info.getComponentConstructor(), Object.class.getConstructor());


    }





    @Test
    public void notDuplicatedKeyExceptionTest() {
        try {
            TypeInfoRack.getInstance().getTypeInfo(ClassB.class);
            fail("Expected exception");
        } catch (Exception e) {
            assertTrue(e instanceof InvalidSerializeException);
            e.printStackTrace();
        }
    }


    /*
    @Test
    public void notCSONAnnotatedExceptionTest() {
        try {
            TypeInfoRack.getInstance().getTypeInfo(ClassA.class);
            fail("Expected exception");
        } catch (Throwable e) {
            assertTrue(e instanceof InvalidSerializeException);
            e.printStackTrace();
        }
    }*/


    @Test
    public void defaultValueTest() {
        TypeInfo typeInfo = TypeInfoRack.getInstance().getTypeInfo(ClassA.class);
        FieldInfo fieldInfo = typeInfo.getFieldInfo("ValueC");
        assertEquals("valueC", fieldInfo.getField().getName());
    }


    @Test
    public void pathKeyTest() {
        TypeInfo typeInfo = TypeInfoRack.getInstance().getTypeInfo(ClassA.class);
        FieldInfo fieldInfo = typeInfo.getFieldInfo("pathKey");
        assertEquals("pathKeyOrigin", fieldInfo.getField().getName());
        assertEquals("pathKey", fieldInfo.getName());
    }


    @Cson
    public static class Grandchild {
        @Value
        String name = "grandchild";

    }

    @Cson
    public static class ChildClass {
        @Value
        String name = "child";

        @Value
        Grandchild grandchild = new Grandchild();


    }

    @Cson
    public static class ParentClass {
        @Value("child")
        ChildClass childClass = new ChildClass();
    }

    @Test
    public void parentChildPathTest() {
        TypeInfo typeInfo = TypeInfoRack.getInstance().getTypeInfo(ParentClass.class);
        assertNotNull(typeInfo.getFieldInfo("child"));




        CSONObject csonObject = CSONSerializer.toCSONObject(new ParentClass());
        System.out.println(csonObject.toString(JSONOptions.json5().setPretty(true)));



    }




}