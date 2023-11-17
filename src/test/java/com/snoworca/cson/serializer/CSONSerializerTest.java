package com.snoworca.cson.serializer;

import com.snoworca.cson.CSONArray;
import com.snoworca.cson.CSONObject;
import com.snoworca.cson.JSONOptions;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static junit.framework.TestCase.*;

public class CSONSerializerTest {

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
        @CSONValue
        private String name = "A";

        @CSONValue("testB.testC.float")
        private float pi = 3.14f;

        @CSONValue("testB.testB.testC.pi")
        private float pi2 = 3.14f;
        @CSONValue("value.int")
        private int value = 1;

        @CSONValue
        private TestClassB testB = new TestClassB();

        @CSONValue("testB.testB")
        private TestClassB testBInTestB = new TestClassB();

        @CSONValue("testB.testB.testC.nullValue")
        private String nullValue = null;

        @CSONValue
        private ArrayList<String> strArray = new ArrayList<>();
        @CSONValue
        private ArrayList<LinkedList<Deque<String>>> strArraySet = new ArrayList<>();

        @CSONValue
        private ArrayList<TestClassB> testBArray = new ArrayList<>();
        @CSONValue
        private Deque<ArrayList<TestClassB>> testBInTestBArray = new ArrayDeque<>();

    }



    private static String makeRandomString(int length) {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < length; i++) {
            sb.append((char) (Math.random() * 26 + 'a'));
        }
        return sb.toString();
    }


    public void fillRandomValues(ArrayList<LinkedList<Deque<String>>> strArraySet) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int numSets = random.nextInt(5) + 1; // 랜덤한 개수의 HashSet 추가
        for (int i = 0; i < numSets; i++) {
            LinkedList<Deque<String>> hashSet = new LinkedList<>();
            int numDeques = random.nextInt(5) + 1; // 랜덤한 개수의 Deque 추가
            for (int j = 0; j < numDeques; j++) {
                Deque<String> deque = new LinkedList<>();
                int numStrings = random.nextInt(5) + 1; // 랜덤한 개수의 문자열 추가
                for (int k = 0; k < numStrings; k++) {
                    deque.add("Value" + random.nextInt(100)); // 랜덤한 문자열 값 추가
                }
                hashSet.add(deque); // Deque를 HashSet에 추가
            }
            strArraySet.add(hashSet); // HashSet를 ArrayList에 추가
        }
    }

    public void fillRandomTestBCalss(Collection<TestClassB> testBObjectList) {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            int numSets = random.nextInt(5,10); // 랜덤한 개수의 HashSet 추가
            for (int i = 0; i < numSets; i++) {
                TestClassB testClassB = new TestClassB();
                testClassB.name = makeRandomString(ThreadLocalRandom.current().nextInt(1,50));
                if(random.nextBoolean()) {
                    testClassB.testC = null;
                } else {
                    testClassB.testC = new TestClassC();
                    testClassB.testC.name = makeRandomString(ThreadLocalRandom.current().nextInt(1,50));
                }
                testBObjectList.add(testClassB);
            }
    }

    @Test
    public void serializeTest() {

        String line = "     documentPageCountWrite(config, key, 0, false, log);";
        System.out.println(line.matches("^[\t|' ']{1,}documentPageCountWrite.*"));

        TestClassA testClassA = new TestClassA();

        ArrayList<String> strArray = new ArrayList<>();
        for(int i = 0; i < 10; i++) {
            testClassA.strArray.add(makeRandomString(ThreadLocalRandom.current().nextInt(1,50)));
            strArray.add(makeRandomString(ThreadLocalRandom.current().nextInt(1,50)));
        }

        fillRandomTestBCalss(testClassA.testBArray);
        for(int i = 0, n = ThreadLocalRandom.current().nextInt(5,10); i < n; ++i) {
            ArrayList<TestClassB> testBInTestBArray = new ArrayList<>();
            fillRandomTestBCalss(testBInTestBArray);
            testClassA.testBInTestBArray.add(testBInTestBArray);
        }

        this.fillRandomValues(testClassA.strArraySet);

        testClassA.testBInTestB.name="BInB";
        CSONObject csonObject = CSONSerializer.toCSONObject(testClassA);
        System.out.println(csonObject.toString(JSONOptions.json5()));
        assertEquals("A", csonObject.get("name"));
        assertEquals(1, csonObject.getObject("value").get("int"));
        assertEquals("B", csonObject.getObject("testB").get("name"));
        assertEquals("BInB", csonObject.getObject("testB").getObject("testB").get("name"));
        assertEquals("C", csonObject.getObject("testB").getObject("testB").getObject("testC").get("name"));
        assertEquals(3.14f, csonObject.getObject("testB").getObject("testC").get("float"));
        assertEquals(3.14f, csonObject.getObject("testB").getObject("testB").getObject("testC").get("pi"));
        assertEquals(null, csonObject.getObject("testB").getObject("testB").getObject("testC").get("nullValue"));


        for(int i = 0; i < 10; i++) {
            assertEquals(testClassA.strArray.get(i), csonObject.getArray("strArray").get(i));
        }

        for(int i = 0; i < testClassA.strArraySet.size(); i++) {
            LinkedList<Deque<String>> linkedList = testClassA.strArraySet.get(i);
            CSONArray csonArray = csonObject.getArray("strArraySet").getArray(i);
            assertEquals(linkedList.size(), csonArray.size());
            Iterator<Object> csonArrayIter = csonArray.iterator();
            for(Deque<String> deque : linkedList) {
                CSONArray array2 = (CSONArray)csonArrayIter.next();
                assertEquals(deque.size(), array2.size());
                Iterator<Object> array2Iter = array2.iterator();
                for(String str : deque) {
                    assertEquals(str, array2Iter.next());
                }
            }
        }

        assertEquals(testClassA.testBArray.size(), csonObject.getArray("testBArray").size());

        for(int i = 0; i < testClassA.testBArray.size(); i++) {
            TestClassB testClassB = testClassA.testBArray.get(i);
            CSONObject csonObject1 = csonObject.getArray("testBArray").getObject(i);
            assertEquals(testClassB.name, csonObject1.get("name"));
            if(testClassB.testC == null) {
                assertNull(csonObject1.get("testC"));
            } else {
                assertEquals(testClassB.testC.name, csonObject1.getObject("testC").get("name"));
            }
        }

        assertEquals(testClassA.testBInTestBArray.size(), csonObject.getArray("testBInTestBArray").size());

        Iterator<ArrayList<TestClassB>> iter = testClassA.testBInTestBArray.iterator();
        for(int i = 0, n = testClassA.testBInTestBArray.size(); i< n; ++i) {
            ArrayList<TestClassB> testBInTestBArray = iter.next();
            CSONArray csonArray = csonObject.getArray("testBInTestBArray").getArray(i);
            assertEquals(testBInTestBArray.size(), csonArray.size());
            Iterator<Object> csonArrayIter = csonArray.iterator();
            for(TestClassB testClassB : testBInTestBArray) {
                CSONObject csonObject1 = (CSONObject)csonArrayIter.next();
                assertEquals(testClassB.name, csonObject1.get("name"));
                if(testClassB.testC == null) {
                    assertNull(csonObject1.get("testC"));
                } else {
                    assertEquals(testClassB.testC.name, csonObject1.getObject("testC").get("name"));
                }
            }

        }
    }








    @CSON
    private static class TestClassNull {

        @CSONValue
        private TestClassA testClassA0 = null;
        @CSONValue
        private TestClassB testClassB1 = new TestClassB();

        @CSONValue("testClassB1.testC.name")
        private String classCName = "nameC";

        @CSONValue("testClassB1.testC.int")
        private int d = 2000;


        @CSONValue("testClassB1.testClassA1")
        private TestClassA testClassA1 = null;

        @CSONValue("testClassB1.testClassA2")
        private TestClassA testClassA2 = new TestClassA();

    }

    @Test
    public void nullObjectSerializeTest() {
        //JSONObject jsonObject = new JSONObject("{\"ok\": null}");
        //Object op = jsonObject.getJSONObject("ok");

        TestClassNull testClassNull = new TestClassNull();

        testClassNull.testClassB1.testC = null;
        testClassNull.testClassA2.testB = null;
        testClassNull.testClassA2.pi2 = 41.3f;
        testClassNull.testClassA2.testBInTestB = null;

        CSONObject csonObject = CSONSerializer.toCSONObject(testClassNull);
        System.out.println(csonObject.toString(JSONOptions.json5()));

        assertNotNull(csonObject.getObject("testClassB1").getObject("testC"));
        assertEquals("nameC", csonObject.getObject("testClassB1").getObject("testC").getString("name"));
        assertEquals(2000, csonObject.getObject("testClassB1").getObject("testC").getInt("int"));
        assertEquals(2000, csonObject.getObject("testClassB1").getObject("testC").getInt("int"));

        assertNull(csonObject.getObject("testClassB1").get("testClassA1"));
        assertEquals(3.14f, csonObject.getObject("testClassB1").getObject("testClassA2").getObject("testB").getObject("testC").getFloat("float"));


        assertNull(csonObject.get("testClassA0"));




        //assertNull(csonObject.getObject("testClassB1").get("testC"));
    }


    @CSON
    public static class Item {
        @CSONValue
        private String name = "item";
        @CSONValue
        private int value = 1;
    }

    @CSON
    public static class ArrayTestClass {
        @CSONValue("array[10]")
        int array10 = 10;


        @CSONValue("arrayInArray[10].[3]")
        int array10array3 = 3;

        @CSONValue("arrayInArray[10][2]")
        int array10array2 = 2;

        @CSONValue("arrayInArray[10][1].name")
        String name = "name";

        @CSONValue("arrayInArray[10][0]")
        Item item = new Item();

        @CSONValue("arrayInArray[10][0].itemInItem")
        Item itemInItem = new Item();

        @CSONValue("arrayInArray[10][0].stringValue")
        String strValue = "1";


    }

    @Test
    public void arraySerializeTest() {
        ArrayTestClass arrayTestClass = new ArrayTestClass();
        CSONObject csonObject = CSONSerializer.toCSONObject(arrayTestClass);
        System.out.println(csonObject.toString(JSONOptions.json5()));
        assertEquals(11, csonObject.getArray("array").size());
        assertEquals(10, csonObject.getArray("array").getInt(10));

        assertEquals(11, csonObject.getArray("arrayInArray").size());
        assertEquals(4, csonObject.getArray("arrayInArray").getArray(10).size());
        assertEquals(3, csonObject.getArray("arrayInArray").getArray(10).getInt(3));
        assertEquals(2, csonObject.getArray("arrayInArray").getArray(10).getInt(2));

        assertEquals("name", csonObject.getArray("arrayInArray").getArray(10).getObject(1).getString("name"));
        assertEquals("item", csonObject.getArray("arrayInArray").getArray(10).getObject(0).getString("name"));
        assertEquals("item", csonObject.getArray("arrayInArray").getArray(10).getObject(0).getObject("itemInItem").getString("name"));
        assertEquals("1", csonObject.getArray("arrayInArray").getArray(10).getObject(0).getString("stringValue"));



    }






}