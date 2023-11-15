package com.snoworca.cson.serializer;

import com.snoworca.cson.CSONObject;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;

public class CSONDeserializerTest {

    @CSON
    public static class TestClass {
        @CSONValue
        public String name;
        @CSONValue
        public int age;
        @CSONValue
        public boolean isMale;
    }

    @Test
    public void test() throws NoSuchFieldException, IllegalAccessException {
        TestClass testClass = new TestClass();
        testClass.name = "SnowOrca";
        testClass.age = 18;
        testClass.isMale = true;

        CSONObject cson = CSONSerializer.toCSONObject(testClass);
        TestClass newClass = (TestClass) CSONSerializer.fromCSONObject(cson, new TestClass());
        assertEquals(testClass.name, newClass.name);
        assertEquals(testClass.age, newClass.age);
        assertEquals(testClass.isMale, newClass.isMale);






    }
}
