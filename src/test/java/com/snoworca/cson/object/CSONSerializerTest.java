package com.snoworca.cson.object;

import com.snoworca.cson.CSONObject;
import junit.framework.TestCase;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class CSONSerializerTest  {

    @CSON
    public static class TestClassA {
        @CSONValue
        private String name = "1";
        @CSONValue("value.int")
        private int value = 1;

    }

    @Test
    public void serializeTest() {

        TestClassA testClassA = new TestClassA();
        CSONObject csonObject = CSONSerializer.serialize(testClassA);
        assertEquals("1", csonObject.get("name"));
        assertEquals(1, csonObject.getObject("value").get("int"));

    }

}