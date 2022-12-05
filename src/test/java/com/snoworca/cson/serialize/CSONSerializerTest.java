package com.snoworca.cson.serialize;

import com.snoworca.cson.CSONArray;
import com.snoworca.cson.CSONObject;

import org.junit.Test;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.junit.Assert.assertEquals;

public class CSONSerializerTest  {

    private static Random random = new Random(System.currentTimeMillis());

    private static  String makeRandomString() {
        Random random = new Random(System.currentTimeMillis());
        int length = random.nextInt(64) + 2;
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<length; i++) {
            int randomInt = random.nextInt(62);
            if(randomInt < 10) {
                sb.append((char)('0' + randomInt));
            } else if(randomInt < 36) {
                sb.append((char)('A' + randomInt - 10));
            } else {
                sb.append((char)('a' + randomInt - 36));
            }
        }
        return sb.toString();
    }

    @Cson
    public static class Child {
        @Value
        public String value = makeRandomString();

        @Value
        public String key = makeRandomString();

        private Child() {

        }
    }

    @Cson
    public static class TestClass {

        @Value
        public boolean vb = true;
        @Value(key = "ib1")
        public byte ib = (byte) random.nextInt();
        @Value
        public short is = (short) random.nextInt();
        @Value
        public char vc = 'B';
        @Value
        public int iv = random.nextInt();

        @Value
        public float fv = random.nextFloat();

        @Value
        public float lv = random.nextLong();

        @Value
        public double dv = random.nextDouble();

        @Value
        public String str = makeRandomString();

        @Value
        public byte[] byteArray;

        @Value(byteArrayToCSONArray = true)
        public byte[] byteBuffer;

        @Value
        public int[] intArray;

        @Value
        public Integer[] integerArray;

        @Value
        public String[] stringArray;

        @Value
        public ArrayList<Object> list = new ArrayList<>();

        @Value
        public TreeSet<Integer> treeSet = new TreeSet<>();

        @Value
        private Child child = new Child();

        @Value
        private Child[] childArray;

        @Value
        private Collection<Child> childCollection = new ArrayList<>();


        @Value
        private Collection<ConcurrentLinkedQueue Queue<Set<Child>>> childCollection = new ArrayList<>();

        @Value
        private Collection childObjectCollection = new ArrayList<>();

        private TestClass() {
            byteArray = new byte[random.nextInt(64) + 2];
            random.nextBytes(byteArray);
            System.arraycopy(byteArray, 0, byteBuffer = new byte[byteArray.length], 0, byteArray.length);
            intArray = new int[random.nextInt(64) + 2];
            integerArray = new Integer[intArray.length];
            stringArray = new String[intArray.length];
            childArray = new Child[intArray.length];
            for(int i = 0; i < intArray.length; ++i) {
                integerArray[i] = intArray[i] = random.nextInt();
                stringArray[i] = makeRandomString();
                childArray[i] = new Child();
                childCollection.add(new Child());
                childObjectCollection.add(new Child());
                treeSet.add(integerArray[i]);
                int typeValue = random.nextInt() % 4;
                if(typeValue == 0) {
                    list.add(random.nextInt());
                } else if(typeValue == 1) {
                    list.add(random.nextBoolean());
                } else if(typeValue == 2){
                    list.add(makeRandomString());
                } else {
                    list.add(new Child());
                }

            }

        }
    }

    @Test
    public void testClassSerializeTest() {
        TestClass tc = new TestClass();
        CSONObject csonObject = CSONSerializer.toCSONObject(tc);
        assertEquals(tc.iv, csonObject.get("iv"));
        assertEquals(tc.ib, csonObject.get("ib1"));
        assertEquals(tc.is, csonObject.get("is"));
        assertEquals(tc.vb, csonObject.get("vb"));
        assertEquals(tc.vc, csonObject.get("vc"));
        assertEquals(tc.fv, (float)csonObject.get("fv"), 0.0001f);
        assertEquals(tc.dv, (double)csonObject.get("dv"), 0.0001f);
        assertEquals(tc.lv, csonObject.get("lv"));
        assertEquals(tc.str, csonObject.get("str"));
        assertEquals(tc.byteArray, csonObject.get("byteArray"));

        CSONArray csonArray = (CSONArray) csonObject.get("byteBuffer");
        assertEquals(tc.byteBuffer.length, csonArray.size());
        for(int i=0; i<tc.byteBuffer.length; i++) {
            assertEquals(tc.byteBuffer[i], csonArray.get(i));
        }

        csonArray = (CSONArray) csonObject.get("intArray");
        assertEquals(tc.intArray.length, csonArray.size());
        for(int i=0; i<tc.intArray.length; i++) {
            assertEquals(tc.intArray[i], csonArray.get(i));
        }

        csonArray = (CSONArray) csonObject.get("integerArray");
        assertEquals(tc.intArray.length, csonArray.size());
        for(int i=0; i<tc.integerArray.length; i++) {
            assertEquals(tc.integerArray[i], csonArray.get(i));
        }

        csonArray = (CSONArray) csonObject.get("stringArray");
        assertEquals(tc.intArray.length, csonArray.size());
        for(int i=0; i<tc.stringArray.length; i++) {
            assertEquals(tc.stringArray[i], csonArray.get(i));
        }

        csonArray = (CSONArray) csonObject.get("list");
        assertEquals(tc.list.size(), csonArray.size());
        for(int i=0; i< tc.list.size(); i++) {
            Object value = tc.list.get(i);
            if(value instanceof  Child) {
                Child child = (Child)value;
                CSONObject co = (CSONObject) csonArray.get(i);
                assertEquals(child.key,  co.get("key"));
                assertEquals(child.value,  co.get("value"));
            } else {
                assertEquals(value, csonArray.get(i));
            }

        }

        csonArray = (CSONArray) csonObject.get("treeSet");
        assertEquals(tc.treeSet.size(), csonArray.size());
        int cnt = 0;
        for(Integer v : tc.treeSet) {
            assertEquals(v, csonArray.get(cnt));
            cnt++;
        }


        CSONObject childObject = (CSONObject)csonObject.get("child");
        assertEquals(tc.child.key,  childObject.get("key"));
        assertEquals(tc.child.value,  childObject.get("value"));

        csonArray = (CSONArray) csonObject.get("childArray");
        assertEquals(tc.childArray.length, csonArray.size());
        cnt = 0;
        for(Child v : tc.childArray) {
            assertEquals(v.key,  csonArray.getObject(cnt).get("key"));
            assertEquals(v.value,  csonArray.getObject(cnt).get("value"));
            cnt++;
        }


        csonArray = (CSONArray) csonObject.get("childCollection");
        assertEquals(tc.childCollection.size(), csonArray.size());
        cnt = 0;
        for(Child v : tc.childCollection) {
            assertEquals(v.key,  csonArray.getObject(cnt).get("key"));
            assertEquals(v.value,  csonArray.getObject(cnt).get("value"));
            cnt++;
        }

        csonArray = (CSONArray) csonObject.get("childObjectCollection");
        assertEquals(tc.childObjectCollection.size(), csonArray.size());
        cnt = 0;
        for(Object o : tc.childObjectCollection) {
            Child v = (Child) o;
            assertEquals(v.key,  csonArray.getObject(cnt).get("key"));
            assertEquals(v.value,  csonArray.getObject(cnt).get("value"));
            cnt++;
        }

    }

}