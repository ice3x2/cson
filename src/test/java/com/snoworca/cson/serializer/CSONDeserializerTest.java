package com.snoworca.cson.serializer;

import com.snoworca.cson.CSONObject;
import org.junit.Test;

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
    public void test() {
        TestClass testClass = new TestClass();
        testClass.name = "SnowOrca";
        testClass.age = 18;
        testClass.isMale = true;

        CSONObject cson = CSONSerializer.toCSONObject(testClass);
        CSONSerializer.fromCSONObject(cson, TestClass.class);



    }
}
