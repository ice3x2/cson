package com.snoworca.cson.serializer;

import com.snoworca.cson.*;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static junit.framework.TestCase.*;
import static org.junit.Assert.assertNotEquals;

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


    @CSON(comment = "루트 코멘트", commentAfter = "루트 코멘트 끝.")
    public static class SimpleComment {
        @CSONValue(key = "key1", comment = "comment1", commentAfterKey = "commentAfterKey1")
        String key1 = "value1";
        @CSONValue(key = "key2", comment = "comment2", commentAfterKey = "")
        String key2 = "value2";


        @CSONValue(key = "key3[0]", comment = "comment3", commentAfterKey = "commentAfter3")
        String key3InArray = "value3";
        @CSONValue(key = "key3[1]", comment = "comment4", commentAfterKey = "commentAfter4")
        String key4InArray = "value4";

        @CSONValue(key = "key3[2]", comment = "comment5", commentAfterKey = "commentAfter5")
        String key5InArray = null;


        @CSONValue(key = "key4", comment = "comment6", commentAfterKey = "commentAfter6")
        ArrayList<String> key4 = new ArrayList<>();


    }


    @Test
    public void simpleCommentTest() {
        CSONArray csonArray = new CSONArray();
        csonArray.addAll(new Object[]{"value3", "value4", new CSONObject()});
        System.out.println(csonArray.toString(JSONOptions.json5()));

        SimpleComment simpleComment = new SimpleComment();
        CSONObject csonObject = CSONSerializer.toCSONObject(simpleComment);
        System.out.println(csonObject.toString(JSONOptions.json5()));

        assertEquals("루트 코멘트", csonObject.getCommentThis());
        assertEquals("루트 코멘트 끝.", csonObject.getCommentAfterThis());



        assertEquals(csonObject.getArray("key3").size(), 3);

        csonObject.put("key5", new String[]{"value3", "value4", null});
        assertEquals("comment1", new CSONObject(csonObject.toString(JSONOptions.json5()), JSONOptions.json5()) .getCommentForKey("key1"));
        assertEquals("commentAfterKey1", new CSONObject(csonObject.toString(JSONOptions.json5()), JSONOptions.json5()) .getCommentAfterKey("key1"));
        assertEquals(null, new CSONObject(csonObject.toString(JSONOptions.json5()), JSONOptions.json5()) .getCommentAfterKey("key2"));
        System.out.println(csonObject.toString(JSONOptions.json5()));
        assertEquals(csonObject.toString(JSONOptions.json5()), new CSONObject(csonObject.toString(JSONOptions.json5()), JSONOptions.json5()).toString(JSONOptions.json5()));

    }


    @CSON
    public static class ByteArray {
        @CSONValue
        byte[] bytes = new byte[]{1,2,3,4,5,6,7,8,9,10};
    }

    @Test
    public void byteArrayTest() {
        ByteArray byteArray = new ByteArray();
        CSONObject csonObject = CSONSerializer.toCSONObject(byteArray);
        System.out.println(csonObject.toString(JSONOptions.json5()));
        byte[] buffer = csonObject.getByteArray("bytes");
        assertEquals(10, buffer.length);
        for(int i = 0; i < 10; i++) {
            assertEquals(i + 1, buffer[i]);
        }
        byteArray.bytes = new byte[]{5,4,3,2,1,0};
        csonObject = CSONSerializer.toCSONObject(byteArray);
        System.out.println(csonObject.toString(JSONOptions.json5()));
        buffer = csonObject.getByteArray("bytes");
        for(int i = 0; i < 6; i++) {
            assertEquals(byteArray.bytes[i], buffer[i]);
        }

        ByteArray bu = CSONSerializer.fromCSONObject(csonObject, ByteArray.class);
    }



    @CSON
    public static class MapClassTest {
        @CSONValue
        private HashMap<String, String> map = new HashMap<>();

        @CSONValue
        private HashMap<String, SimpleComment> commentMap = new HashMap<>();



    }



    @Test
    public void mapClassTest() {
        MapClassTest mapClassTest = new MapClassTest();
        mapClassTest.map.put("key1", "value1");
        mapClassTest.map.put("key2", "value2");
        mapClassTest.map.put("keyNull", null);
        HashMap subMap = new HashMap<>();
        subMap.put("key1", new ByteArray());
        subMap.put("key2", new ByteArray());




        Map<String, Integer> maps = new HashMap<>();
        maps.put("key1", 1);
        maps.put("key2", 2);
        maps.put("key3", 3);
        mapClassTest.commentMap.put("key1", new SimpleComment());
        CSONObject csonObject = CSONSerializer.toCSONObject(mapClassTest);


        System.out.println(csonObject.toString(JSONOptions.json5()));

        MapClassTest mapClassTest1 = CSONSerializer.fromCSONObject(csonObject, MapClassTest.class);




        assertEquals(mapClassTest.map.size(), mapClassTest1.map.size());

        assertEquals(csonObject.toString(JSONOptions.json5()), CSONSerializer.toCSONObject(mapClassTest1).toString(JSONOptions.json5()));




    }

    @CSON
    public static class GenericClass<T> {
        @CSONValue
        private String value = "value";

    }

    @CSON
    public static class Sim {
        @CSONValue
        Collection<GenericClass<String>> collection = new ArrayList<>();
    }

    @Test
    public void genericClassTest() {
        Sim genericClass = new Sim();
        genericClass.collection.add(new GenericClass<>());
        CSONObject csonObject = CSONSerializer.toCSONObject(genericClass);
        System.out.println(csonObject.toString(JSONOptions.json5()));
    }



    public static class TestSuperClass {
        @CSONValue
        private String name = "name";

        public String getName() {
            return name;
        }
    }
    @CSON
    static class TestChildClass extends TestSuperClass {

    }

    @Test
    public void extendsTest() {
        TestChildClass testChildClass = new TestChildClass();
        CSONObject csonObject = CSONSerializer.toCSONObject(testChildClass);
        assertEquals("name", csonObject.get("name"));
        csonObject.put("name", "name2");
        testChildClass = CSONSerializer.fromCSONObject(csonObject, TestChildClass.class);
        assertEquals("name2", testChildClass.getName());




    }





    @CSON
    public static class TestClassY {
        @CSONValue
        private int age = 29;
    }

    @CSON
    public static class TestClassP {
        @CSONValue("ageReal")
        private int age = 27;
    }

    @CSON
    public static class TestClassX {
        @CSONValue("nickname.key[10]")
        private String name = "name";

        @CSONValue(value = "nickname", comment = "닉네임 오브젝트.", commentAfterKey = "닉네임 오브젝트 끝.")
        TestClassY testClassY = new TestClassY();

        @CSONValue(value = "nickname")
        TestClassP testClassP = new TestClassP();


        @CSONValue(key="list", comment = "닉네임을 입력합니다.", commentAfterKey = "닉네임 입력 끝.")
        ArrayList<List<TestClassY>> testClassYArrayList = new ArrayList<>();


    }

    @Test
    public void testClassX() {
        TestClassX testClassX = new TestClassX();

        testClassX.testClassYArrayList.add(new ArrayList<>());
        testClassX.testClassYArrayList.get(0).add(new TestClassY());


        
        CSONObject csonObject = CSONSerializer.toCSONObject(testClassX);
        System.out.println(csonObject.toString(JSONOptions.json5()));
        assertEquals(27, csonObject.getObject("nickname").getInt("ageReal"));
        assertEquals(29, csonObject.getObject("nickname").getInt("age"));
        assertEquals(csonObject.getCommentOfKey("nickname"), "닉네임 오브젝트.");
        assertEquals(csonObject.getCommentAfterKey("nickname"), "닉네임 오브젝트 끝.");


        String json5 = csonObject.toString(JSONOptions.json5());

        System.out.println(json5);

    }



    @CSON
    class NestedValueClass {
        @CSONValue
        private String name = "name";

        private String name2 = "name2";
    }




    @CSON
    public static class NestedObjectClass {
        @CSONValue(key =  "ages", comment = "닉네임 오브젝트:testClassP", commentAfterKey = "닉네임 오브젝트 끝:testClassP")
        private TestClassP testClassP = new TestClassP();

        @CSONValue(key = "ages", comment = "닉네임 오브젝트:testClassB", commentAfterKey = "닉네임 오브젝트 끝:testClassB")
        private TestClassB testClassB = new TestClassB();

        @CSONValue("name3")
        private String name2 = "name2";

        @CSONValue("name3")
        private String name3 = "name3";


    }

    @Test
    public void  nestedValuesTest() {

        NestedObjectClass nestedObjectClass = new NestedObjectClass();
        nestedObjectClass.testClassB.testC.name = "adsfadsfadsf";
        nestedObjectClass.testClassB.name = "123123";
        CSONObject csonObject = CSONSerializer.toCSONObject(nestedObjectClass);
        System.out.println(csonObject.toString(JSONOptions.json5()));


        NestedObjectClass nestedObjectClassCopied = CSONSerializer.fromCSONObject(csonObject, NestedObjectClass.class);
        assertEquals(nestedObjectClass.testClassP.age, nestedObjectClassCopied.testClassP.age);
        assertEquals(nestedObjectClass.testClassB.name, nestedObjectClassCopied.testClassB.name);
        assertEquals(nestedObjectClass.testClassB.testC.name , nestedObjectClassCopied.testClassB.testC.name);
        assertNotEquals(nestedObjectClass.name2, nestedObjectClass.name3);
        assertEquals(nestedObjectClassCopied.name2, nestedObjectClassCopied.name3);


    }

   @CSON
   public static class SetterGetterTestClass {

        Map<String, String> nameAgeMap = null;
        Map<String, Integer> nameAgeMapInteger = null;





       Collection<ArrayList<LinkedList<String>>> nameList = null;
       ArrayDeque<LinkedList<HashSet<Integer>>> nameTypeChangedList = null;

       @CSONValue("nameList")
       Collection<Collection<Collection<Short>>> nameShortList = null;

       @CSONValueGetter
       public Collection<ArrayList<LinkedList<String>>> getNameList() {
           int x = 0;
           ArrayList result = new ArrayList();
            for(int i = 0; i < 10; i++) {
               ArrayList<LinkedList<String>> arrayList = new ArrayList<>();
               for(int j = 0; j < 10; j++) {
                   LinkedList<String> list = new LinkedList<>();
                   for(int k = 0; k < 10; k++) {
                       ++x;
                       list.add( x + "");
                   }
                   arrayList.add(list);
               }
               result.add(arrayList);
           }
            return result;

       }




       @CSONValueSetter
       public void setNameList(Collection<ArrayList<LinkedList<String>>> names) {
           nameList = names;

       }


       @CSONValueSetter(key = "nameList")
       public void setNameHashSet(ArrayDeque<LinkedList<HashSet<Integer>>> names) {
           nameTypeChangedList = names;
       }


       @CSONValueGetter
       public Map<String, String> getNameAgeMap() {
              Map<String, String> map = new HashMap<>();
              for(int i = 0; i < 10; i++) {
                  map.put("name" + i, i + "");
              }
              return map;
       }

       @CSONValueSetter(key = "nameAgeMap")
       public String setNameAgeMapInteger(Map<String, Integer> map) {
           nameAgeMapInteger = map;
           return "OK" ;
       }

       @CSONValueSetter(key = "nameAgeMap")
       public String setNameAgeMap(Map<String, String> map) {
          nameAgeMap = map;
          return "OK" ;
       }

       String inputName = "name";
       @CSONValueGetter
       public String getName() {
           return "name";
       }
       @CSONValueSetter
       public void setName(String name) {
           this.inputName = name;
       }


   }


   @Test
    public void setterGetterTest() {

        // You can change the default options. (It will be applied to all CSONObject and CONSArray)
        CSONObject.setDefaultStringFormatOption(StringFormatOption.json5());
        for(int count = 0; count < 1; ++count) {


            SetterGetterTestClass setterGetterTestClass = new SetterGetterTestClass();

            CSONObject csonObject = CSONSerializer.toCSONObject(setterGetterTestClass);
            System.out.println(csonObject.toString(JSONOptions.json5()));

            setterGetterTestClass = CSONSerializer.fromCSONObject(csonObject, SetterGetterTestClass.class);

            assertEquals(setterGetterTestClass.nameList.size(), 10);
            Iterator<ArrayList<LinkedList<String>>> iter = setterGetterTestClass.nameList.iterator();
            int x = 0;
            for (int i = 0; i < 10; i++) {
                ArrayList<LinkedList<String>> arrayList = iter.next();
                assertEquals(arrayList.size(), 10);
                Iterator<LinkedList<String>> iter2 = arrayList.iterator();
                for (int j = 0; j < 10; j++) {
                    LinkedList<String> list = iter2.next();
                    assertEquals(list.size(), 10);
                    Iterator<String> iter3 = list.iterator();
                    for (int k = 0; k < 10; k++) {
                        ++x;
                        assertEquals(x + "", iter3.next());
                    }
                }
            }

            assertTrue(setterGetterTestClass.nameTypeChangedList instanceof  ArrayDeque);
            assertEquals(setterGetterTestClass.nameTypeChangedList.size(), 10);
            Iterator<LinkedList<HashSet<Integer>>> iter4 = setterGetterTestClass.nameTypeChangedList.iterator();
            x = 0;
            for (int i = 0; i < 10; i++) {
                LinkedList<HashSet<Integer>> arrayList = iter4.next();
                assertEquals(arrayList.size(), 10);
                Iterator<HashSet<Integer>> iter5 = arrayList.iterator();
                for (int j = 0; j < 10; j++) {
                    HashSet<Integer> list = iter5.next();
                    assertEquals(list.size(), 10);
                    SortedSet<Integer> sortedSet = new TreeSet<>(list);
                    Iterator<Integer> iter6 = sortedSet.iterator();
                    for (int k = 0; k < 10; k++) {
                        ++x;
                        assertEquals(Integer.valueOf(x), iter6.next());
                    }
                }
            }

            assertEquals(setterGetterTestClass.nameAgeMap.size(), 10);
            for(int i = 0; i < 10; i++) {
                assertEquals(setterGetterTestClass.nameAgeMap.get("name" + i), i + "");
                assertEquals(setterGetterTestClass.nameAgeMapInteger.get("name" + i),Integer.valueOf(i) );
            }


            System.out.println(csonObject.toString(JSONOptions.json5()));
            assertEquals("name", csonObject.get("name"));
            csonObject.put("name", "1213123");

            setterGetterTestClass = CSONSerializer.fromCSONObject(csonObject, SetterGetterTestClass.class);
            assertEquals("1213123", setterGetterTestClass.inputName);


        }
   }



   @CSON
   public static class Addr {
        @CSONValue
        private String city;
        @CSONValue
        private String zipCode;
   }


   @CSON
   public static class User {
       @CSONValue
       private String name;
       @CSONValue
       private int age;
       @CSONValue
       private List<String> friends;
       @CSONValue
       private Addr addr;

       private User() {}
       public User(String name, int age, String... friends) {
           this.name = name;
           this.age = age;
           this.friends = Arrays.asList(friends);
       }

       public void setAddr(String city, String zipCode) {
          this.addr = new Addr();
          this.addr.city = city;
          this.addr.zipCode = zipCode;
       }

   }

   @CSON(comment = "Users", commentAfter = "Users end.")
   public static class Users {
        @CSONValue(key = "users", comment = "key: user id, value: user object")
        private HashMap<String, User> idUserMap = new HashMap<>();
   }


   @Test
   public void exampleTest() {
       Users users = new Users();

       User user1 = new User("MinJun", 28, "YoungSeok", "JiHye", "JiHyeon", "MinSu");
       user1.setAddr("Seoul", "04528");
       User user2 = new User("JiHye", 27, "JiHyeon","Yeongseok","Minseo");
       user2.setAddr("Cheonan", "31232");
       users.idUserMap.put("qwrd", user1);
       users.idUserMap.put("ffff", user2);

       CSONObject csonObject = CSONSerializer.toCSONObject(users);
       csonObject.setStringFormatOption(StringFormatOption.json5());
       System.out.println(csonObject);
       // Output
       /*
            //Users
            {
                //key: user id, value: user object
                users:{
                    qwrd:{
                        name:'MinJun',
                        age:28,
                        friends:['YoungSeok','JiHye','JiHyeon','MinSu'],
                        addr:{
                            city:'Seoul',
                            zipCode:'04528'
                        }
                    },
                    ffff:{
                        name:'JiHye',
                        age:27,
                        friends:['JiHyeon','Yeongseok','Minseo'],
                        addr:{
                            city:'Cheonan',
                            zipCode:'31232'
                        }
                    }
                }
            }
            //Users end.
        */


       //  Parse CSONObject to Users
       // Option 1.
       Users parsedUsers = CSONSerializer.fromCSONObject(csonObject, Users.class);

       // Option 2. Can be used even without a default constructor.
       //Users parsedUsers = new Users();
       //CSONSerializer.fromCSONObject(csonObject, parsedUsers);






   }


}