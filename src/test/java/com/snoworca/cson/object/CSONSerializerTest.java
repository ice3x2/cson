package com.snoworca.cson.object;

import com.snoworca.cson.CSONArray;
import com.snoworca.cson.CSONObject;
import junit.framework.TestCase;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class CSONSerializerTest  {

    @CSON
    static class TestClassC {
        @CSONValue
        private String name = "C";
    }

    @CSON
    public static class TestClassB {
        @CSONValue
        private String name = "B";

        @CSONValue
        private TestClassC testC = new TestClassC();

    }

    @CSON
    public static class TestClassA {
        /*@CSONValue
        private String name = "A";
        @CSONValue("value.int")
        private int value = 1;*/

        @CSONValue
        private TestClassB testB = new TestClassB();

        @CSONValue("testB.testB")
        private TestClassB testBInTestB = new TestClassB();


    }

    @Test
    public void serializeTest() {

        TestClassA testClassA = new TestClassA();
        testClassA.testBInTestB.name="BInB";
        CSONObject csonObject = CSONSerializer.serialize(testClassA);
        System.out.println(csonObject);
        //assertEquals("A", csonObject.get("name"));
        //assertEquals(1, csonObject.getObject("value").get("int"));
        assertEquals("B", csonObject.getObject("testB").get("name"));
        assertEquals("BInB", csonObject.getObject("testB").getObject("testB").get("name"));

    }

}