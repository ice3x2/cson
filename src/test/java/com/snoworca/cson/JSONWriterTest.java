package com.snoworca.cson;

import org.junit.Test;


public class JSONWriterTest {
    @Test
    public void test() {
        CSONObject csonObject = new CSONObject();
        csonObject.put("1", 1);
        csonObject.put("1.1", 1.1f);
        csonObject.put("2.2", 1.1);
        csonObject.put("333333L", 333333L);
        csonObject.put("boolean", true);
        csonObject.put("char", 'c');
        csonObject.put("short", (short)32000);
        csonObject.put("byte", (byte)128);
        csonObject.put("string", "string");
        csonObject.put("string", "string");




        String jsonString = csonObject.toString();
        System.out.println(jsonString);




    }
}
