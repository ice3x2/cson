package com.snoworca.cson.serialize;

import com.snoworca.cson.CSONObject;
import junit.framework.TestCase;
import org.junit.Test;

import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import static org.junit.Assert.*;

public class TypeInfoRackTest {

    // A-Z, a-z, 0-9 의 랜덤 문자열 생성



    private static Random random = new Random(System.currentTimeMillis());

    public class ClassA {
        @Value
        public int a;
        @Value
        public int b;
        @Value
        public int c;
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

    @Test
    public void notCSONAnnotatedExceptionTest() {
        try {
            TypeInfoRack.getInstance().getTypeInfo(ClassA.class);
            fail("Expected exception");
        } catch (Exception e) {
            assertTrue(e instanceof InvalidSerializeException);
            e.printStackTrace();
        }
    }



}