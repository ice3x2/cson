package com.snoworca.cson.serializer;

import com.snoworca.cson.CSONObject;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class CSONDeserializerTest {

    @CSON
    public static class TestClass {
        @CSONValue
        public String name;
        @CSONValue
        public int age;
        @CSONValue
        public boolean isMale;
        @CSONValue
        public String nullValue;

        @CSONValue
        public ArrayList<String> childrenNames = new ArrayList<>();
        @CSONValue
        public ArrayList<ArrayList<Integer>> tourDates  = new ArrayList<>();

    }

    @Test
    public void test() throws NoSuchFieldException, IllegalAccessException {
        TestClass testClass = new TestClass();
        testClass.name = "SnowOrca";
        testClass.age = 18;
        testClass.isMale = true;
        testClass.childrenNames.add("김철수");
        testClass.childrenNames.add("김영희");
        testClass.childrenNames.add("김영수");

        ArrayList list = new ArrayList<>();
        list.add(20180111);
        list.add(20180112);
        list.add(20180113);
        testClass.tourDates.add(list);

        list = new ArrayList<>();
        list.add(20190301);
        list.add(20190302);
        list.add(20190303);
        testClass.tourDates.add(list);


        CSONObject cson = CSONSerializer.toCSONObject(testClass);
        TestClass newClass = (TestClass) CSONSerializer.fromCSONObject(cson, new TestClass());
        assertEquals(testClass.name, newClass.name);
        assertEquals(testClass.age, newClass.age);
        assertEquals(testClass.isMale, newClass.isMale);
        assertNull(cson.get("nullValue"));
        assertEquals(testClass.nullValue, newClass.nullValue);

        assertEquals(testClass.childrenNames.size(), newClass.childrenNames.size());
        for(int i = 0; i < testClass.childrenNames.size(); i++) {
            assertEquals(testClass.childrenNames.get(i), newClass.childrenNames.get(i));
        }




    }
}
