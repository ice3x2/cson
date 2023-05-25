package com.snoworca.cson.serialize;

import com.snoworca.cson.CSONArray;
import com.snoworca.cson.CSONObject;


import com.snoworca.cson.JSONOptions;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


import static org.junit.Assert.assertEquals;

public class


CSONSerializerTest  {

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


        /**TODO
        @Value
        private Collection<ConcurrentLinkedQueue Queue<Set<Child>>> childCollection = new ArrayList<>();
         **/

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


    @Cson
    public static class MultiCollectionChild {


        public MultiCollectionChild() {
        }
        @Value
        int value;

        @Value
        public MultiCollectionChild[] childrens = null;
        @Value
        public ConcurrentMap<String, String> map = new ConcurrentHashMap<>();
        @Value
        public Map<String, MultiCollectionChild> mapChild = new HashMap<>();
        @Value
        public Map<String, Collection<MultiCollectionChild>> mapChildCollection = new HashMap<>();
        @Value
        public Set<MultiCollectionChild> set = new HashSet<>();

        // 내부 값을들 랜덤하게 채워주는 init() 메서드 하나 만들어줘.
        public void init() {
            init(4);
        }

        public void init(int num) {

            value = num;


            --num;

            childrens = new MultiCollectionChild[num];

            int randomCount= num;//random.nextInt(num);
            for(int i=0; i<randomCount; i++) {
                MultiCollectionChild child = new MultiCollectionChild();
                child.init(num);
                childrens[i] = child;
            }
            randomCount= num;//random.nextInt(num);
            for(int i=0; i<randomCount; i++) {
                map.put("key" + i, "value" + i);
            }
            randomCount= num;//random.nextInt(num);
            for(int i=0; i<randomCount; i++) {
                MultiCollectionChild child = new MultiCollectionChild();
                child.init(num);
                mapChild.put("key" + i, child);
            }
            randomCount= num;//random.nextInt(num);
            for(int i=0; i<randomCount; i++) {
                Collection<MultiCollectionChild> childCollection = new ArrayList<>();
                randomCount= num;//random.nextInt(num);
                for(int j=0; j<randomCount; j++) {
                    MultiCollectionChild child = new MultiCollectionChild();
                    child.init(num);
                    childCollection.add(child);
                }
                mapChildCollection.put("key" + i, childCollection);
            }

            randomCount= num;//random.nextInt(num);
            for(int i=0; i<randomCount; i++) {
                MultiCollectionChild child = new MultiCollectionChild();
                child.init(num);
                set.add(child);
            }


        }


    }

    @Test
    public void multiCollectionChildSerializeTest() {
        MultiCollectionChild mcc = new MultiCollectionChild();
        mcc.init();
        CSONObject csonObject =  CSONSerializer.toCSONObject(mcc);
        System.out.println(csonObject.toString(JSONOptions.json().setPretty(true)));

    }


    @Cson
    public static class ChildMap {
        @Value
        int value = 1000;
        @Value
        public Map<String, MapTestClass> childMap = new HashMap<>();


    }


    @Cson
    public static class Key {

        @Value
        public String key;
        public Key(String key) {
            this.key = key;
        }

        public Key() {
        }

        @Override
        public String toString() {
            return key;
        }
    }


    @Cson
    public static class MapTestClass {

        @Value
         Map<String, Integer> integerMap;

         @Value
         Map<String, String> stringMap;


         @Value
         Map<String, Map<String, Boolean>> stringBooleanMap;


         @Value
         Map<String, Map<String, Collection<Integer>>> randomCollectionMap = new HashMap<>();

         @Value
         Map<String, Map<String, Integer[]>> randomArrayMap = new HashMap<>();

         @Value
         Map<String, Map<String, Map<String, String>>> mapmapmap = new HashMap<>();

         @Value
        Map<Key, String> keyMap = new HashMap<>();


        @Value
        ChildMap childMap = new ChildMap();

        @Value
        ArrayList<Map<String, Key>> mapInList = new ArrayList<>();



        public MapTestClass init() {

            integerMap = new HashMap<>();
            integerMap.put("v1", 1);
            integerMap.put("v2", 2);
            integerMap.put("v3", 3);
            integerMap.put("v4", 4);

            stringMap = new HashMap<>();
            stringMap.put("str1", "1");
            stringMap.put("str2", "2");
            stringMap.put("str3", "3");
            stringMap.put("str4", "4");


            stringBooleanMap = new LinkedHashMap<>();
            Map<String, Boolean> boolMap = new HashMap<>();
            boolMap.put("true", true);
            boolMap.put("false", false);
            stringBooleanMap.put("bool1", boolMap);


            HashMap<String, Collection<Integer>> collectionMap = new HashMap<>();
            collectionMap.put("collection1", Arrays.asList(6,5,4,3,2,1));
            collectionMap.put("collection2", Arrays.asList(8,9,10,11,12,13));
            randomCollectionMap.put("randomCollectionMap", collectionMap);

            HashMap<String, Integer[]> arrayMap = new HashMap<>();
            arrayMap.put("array1", new Integer[]{6,5,4,3,2,1});
            arrayMap.put("array2", new Integer[]{8,9,10,11,12,13});
            randomArrayMap.put("randomArrayMap", arrayMap);

            mapmapmap = new HashMap<>();
            HashMap<String, Map<String, String>> mapmap = new HashMap<>();
            HashMap<String, String> map = new HashMap<>();
            map.put("key1", "value1");
            map.put("key2", "value2");
            map.put("key3", "value3");
            map.put("key4", "value4");
            mapmap.put("map1", map);
            mapmap.put("map2", map);
            mapmap.put("map3", map);
            mapmap.put("map4", map);
            mapmapmap.put("mapmapmap", mapmap);

            keyMap.put(new Key("key1"), "value1");
            keyMap.put(new Key("key2"), "value2");
            keyMap.put(new Key("key3"), "value3");

            childMap.childMap.put("child1", new MapTestClass());


            Map<String, Key> mapt = new HashMap<>();
            mapt.put("key1", new Key("key1"));
            mapt.put("key2", new Key("key2"));
            mapt.put("key3", new Key("key3"));
            mapInList.add(mapt);

            mapt = new HashMap<>();
            mapt.put("key4", new Key("key4"));
            mapt.put("key5", new Key("key5"));
            mapt.put("key6", new Key("key6"));
            mapInList.add(mapt);




            return this;

        }

    }


    @Test
    public void mapTest() {


        CSONObject csonObject =  CSONSerializer.toCSONObject(new MapTestClass().init());
        System.out.println(csonObject.toString(JSONOptions.json().setPretty(true)));

        assertEquals(1, csonObject.get("v1"));
        assertEquals(2, csonObject.get("v2"));
        assertEquals(3, csonObject.get("v3"));
        assertEquals(4, csonObject.get("v4"));


        assertEquals("1", csonObject.get("str1"));
        assertEquals("2", csonObject.get("str2"));
        assertEquals("3", csonObject.get("str3"));
        assertEquals("4", csonObject.get("str4"));

        assertEquals(true, csonObject.getObject("bool1").get("true"));

        assertEquals(6, csonObject.getObject("randomCollectionMap").getArray("collection1").getInteger(0) );
        assertEquals(11, csonObject.getObject("randomCollectionMap").getArray("collection2").getInteger(3) );

        assertEquals(6, csonObject.getObject("randomArrayMap").getArray("array1").getInteger(0) );
        assertEquals(11, csonObject.getObject("randomArrayMap").getArray("array2").getInteger(3) );


        assertEquals("value1", csonObject.getObject("mapmapmap").getObject("map1").get("key1") );
        assertEquals("value2", csonObject.getObject("mapmapmap").getObject("map2").get("key2") );
        assertEquals("value3", csonObject.getObject("mapmapmap").getObject("map3").get("key3") );
        assertEquals("value4", csonObject.getObject("mapmapmap").getObject("map4").get("key4") );


        assertEquals("value1", csonObject.getString("key1"));
        assertEquals("value2", csonObject.getString("key2"));
        assertEquals("value3", csonObject.getString("key3"));

        assertEquals(1000, csonObject.getObject("childMap").getObject("child1").getObject("childMap").getInteger("value") );

        assertEquals("key1", csonObject.getArray("mapInList").getObject(0).getObject("key1").getString("key"));
        assertEquals("key6", csonObject.getArray("mapInList").getObject(1).getObject("key6").getString("key"));



    }


    @Cson
    public static class MultiPath {
        @Value("path1.path2.path3")
        private String value = "100";
    }


    @Test
    public void multiPathTest() {
        MultiPath multiPath = new MultiPath();
        CSONObject csonObject = CSONSerializer.toCSONObject(multiPath);
        System.out.println(csonObject.toString(JSONOptions.json().setPretty(true)));
        assertEquals("100", csonObject.getObject("path1").getObject("path2").getString("path3"));
    }





}