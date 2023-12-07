package com.snoworca.cson;

import com.snoworca.cson.util.NoSynchronizedStringReader;
import com.clipsoft.org.json.simple.parser.JSONParser;
import com.clipsoft.org.json.simple.parser.ParseException;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;

import static org.junit.Assert.*;

public class PureJsonTest {


    @Test
    public void dutyJSONArray() {
        String testJSON = "[10,20,\n" +
                "    {\n" +
                "      \"name\": \"Alice\",\n" +
                "      \"age\": 30,\n" +
                "      \"address\": {\n" +
                "        \"street\": \"123 Main St\",\n" +
                "        \"city\": \"Wonderland\",\n" +
                "        \"country\": \"Fairyland\"\n" +
                "      },\n" +
                "      \"contacts\": [\n" +
                "        {\n" +
                "          \"type\": \"email\",\n" +
                "          \"contact\": \"alice@example.com\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"type\": \"phone\",\n" +
                "          \"contact\": \"+123456789\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"Bob\",\n" +
                "      \"age\": 25,\n" +
                "      \"address\": {\n" +
                "        \"street\": \"456 Elm St\",\n" +
                "        \"city\": \"Dreamville\",\n" +
                "        \"country\": \"Imaginationland\"\n" +
                "      },\n" +
                "      \"contacts\": [\n" +
                "        {\n" +
                "          \"type\": \"email\",\n" +
                "          \"contact\": \"bob@example.com\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"type\": \"phone\",\n" +
                "          \"contact\": \"+987654321\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"Charlie\",\n" +
                "      \"age\": 35,\n" +
                "      \"address\": {\n" +
                "        \"street\": \"789 Oak St\",\n" +
                "        \"city\": \"Fantasytown\",\n" +
                "        \"country\": \"Whimsyville\"\n" +
                "      },\n" +
                "      \"contacts\": [\n" +
                "        {\n" +
                "          \"type\": \"email\",\n" +
                "          \"contact\": \"charlie@example.com\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"type\": \"phone\",\n" +
                "          \"contact\": \"+246813579\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]";


        CSONArray csonArraryOrigin = new CSONArray(testJSON, StringFormatOption.jsonPure());
        CSONArray csonArrary = new CSONArray(testJSON, StringFormatOption.json());

        assertEquals(csonArraryOrigin.toString(), csonArrary.toString());

        System.out.println(csonArraryOrigin.toString());

    }

    @Test
    public void dutyJSON() throws IOException {
        String testJSON = "{\n" +
                "  \"user\": {\n" +
                "    \"id\": 12345,\n" +
                "    \"username\": \"mysteriousCoder\",\n" +
                "    \"email\": \"mysterious@example.com\",\n" +
                "    \"profile\": {\n" +
                "      \"name\": \"Mr. Mysterious\",\n" +
                "      \"age\": 30,\n" +
                "      \"description\": \"A person of enigmatic nature\",\n" +
                "      \"address\": {\n" +
                "        \"street\": \"Shadowy Lane\",\n" +
                "        \"city\": \"Crypticville\",\n" +
                "        \"country\": \"Enigmatica\"\n" +
                "      }\n" +
                "    },\n" +
                "    \"preferences\": {\n" +
                "      \"theme\": \"Dark\",\n" +
                "      \"notifications\": {\n" +
                "        \"email\": true,\n" +
                "        \"push\": true,\n" +
                "        \"sms\": false\n" +
                "      },\n" +
                "      \"settings\": [\n" +
                "        {\n" +
                "          \"name\": \"Display\",\n" +
                "          \"value\": \"Night mode\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"name\": \"Language\",\n" +
                "          \"value\": \"Cryptic\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"name\": \"Sounds\",\n" +
                "          \"value\": \"Eerie\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    \"orders\": [\n" +
                "      {\n" +
                "        \"id\": \"ORD001\",\n" +
                "        \"date\": \"2023-12-01\",\n" +
                "        \"total\": 00150.25,\n" +
                "        \"items\": [\n" +
                "          {\n" +
                "            \"name\": \"Mystery Box\",\n" +
                "            \"quantity\": 1,\n" +
                "            \"price\": 75.50\n" +
                "          },\n" +
                "          {\n" +
                "            \"name\": \"Cryptic\\nScroll\",\n" +
                "            \"quantity\": 2,\n" +
                "            \"price\": 37.75,\n" +
                "            \"zero\": -0,\n" +
                "            \"zeroPoint\": -0.0\n" +
                "          }\n" +
                "        ]\n" +
                "      },\n" +
                "      {\n" +
                "        \"id\": \"ORD002\",\n" +
                "        \"date\": \"2023-12-10\",\n" +
                "        \"total\": 255,\n" +
                "        \"items\": [\n" +
                "          {\n" +
                "            \"name\": \"Enigmatic Puzzle\",\n" +
                "            \"quantity\": 1,\n" +
                "            \"price\": 180.0\n" +
                "          },\n" +
                "          {\n" +
                "            \"name\": \"Secret Cipher\",\n" +
                "            \"quantity\": 1,\n" +
                "            \"price\": 40.0\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}";


        testJSON = testJSON.replace(" ", "").replace("\n", "");

        CSONElement csonElement = new CSONObject(testJSON, StringFormatOption.jsonPure());
        CSONObject csonObject = (CSONObject) csonElement;
        System.out.println(csonObject.toString());
        //assertEquals(testJSON.replace("0xff", "255").replace("75.50", "75.5"), csonObject.toString());

        NoSynchronizedStringReader stringReader2 = new NoSynchronizedStringReader(testJSON);
        CSONObject csonObjectPure = (CSONObject)PureJSONParser.parsePureJSON(stringReader2);
        stringReader2.close();


        CSONObject csonObjectJson = new CSONObject(testJSON, JSONOptions.json());
        assertEquals(csonObjectPure.toString(), csonObjectJson.toString());




        System.out.println("워밍업");

        for(int i = 0; i < 100000; ++i) {
            try {
                com.clipsoft.org.json.simple.JSONObject jsonObject = (com.clipsoft.org.json.simple.JSONObject) new JSONParser().parse(testJSON);
                jsonObject.toJSONString();
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            //jsonObject.toString();
            String aaa = "";
            aaa.trim();
        }
        for(int i = 0; i < 100000; ++i) {
            JSONObject jsonObject = new JSONObject(testJSON);
            String aaa = "";
            aaa.trim();
        }
        for(int i = 0; i < 100000; ++i) {
            NoSynchronizedStringReader stringReader = new NoSynchronizedStringReader(testJSON);
            csonObject = (CSONObject)PureJSONParser.parsePureJSON(stringReader);
            stringReader.close();
            String aaa = "";
            aaa.trim();
        }
        System.out.println("워밍업완료");



        long peekMemoryForJSON = 0;
        long useMemoryForJSON = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long startjson = System.currentTimeMillis();
        for(int i = 0; i < 1000000; ++i) {
            useMemoryForJSON = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            JSONObject jsonObject = new JSONObject(testJSON);
            jsonObject.toString();
            //long memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() - useMemoryForJSON;
           // if(peekMemoryForJSON < memory) {
           //     peekMemoryForJSON = memory;
           // }

            String aaa = "";
            aaa.trim();
        }
        System.out.println("json : " + (System.currentTimeMillis() - startjson));

        //////////////////////////////////////////////////////////////////////////////////////////////
        long peekMemoryForCSON = 0;
        long  useMemoryForCSON = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long startcson = System.currentTimeMillis();
        for(int i = 0; i < 1000000; ++i) {
            useMemoryForCSON = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            CSONObject ss = new CSONObject(testJSON, StringFormatOption.jsonPure());
            ss.toString(JSONOptions.json());
            //long memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() - useMemoryForCSON;
            //if(peekMemoryForCSON < memory) {
             //   peekMemoryForCSON = memory;
            //}
            String aaa = "";
            aaa.trim();
        }
        System.out.println("cson : " + (System.currentTimeMillis() - startcson));
        //////////////////////////////////////////////////////////////////////////////////////////////


        long peekMemoryForSimple = 0;
        long useMemoryForSimple = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long jsonSimple = System.currentTimeMillis();
        for(int i = 0; i < 1000000; ++i) {
            useMemoryForSimple = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            try {
                com.clipsoft.org.json.simple.JSONObject jsonObject = (com.clipsoft.org.json.simple.JSONObject) new JSONParser().parse(testJSON);
                jsonObject.toJSONString();
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            //long memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() - useMemoryForSimple;
            //if(peekMemoryForSimple < memory) {
             //   peekMemoryForSimple = memory;
            //}
            //jsonObject.toString();
            String aaa = "";
            aaa.trim();
        }
        System.out.println("jsonSimple : " + (System.currentTimeMillis() - jsonSimple));

    }

    @Test
    public void parsingCommentJson() {
        Exception err = null;
        String json = "{\"key\": \"5\"/*주석입니다*/,\"a\":\"b\",}";
        CSONObject csonObject = new CSONObject(json, JSONOptions.json()
                .setAllowTrailingComma(true)
                .setAllowComments(true)
                .setSkipComments(false));

        assertEquals("주석입니다", csonObject.getCommentObjectOfValue ("key").getAfterComment());

    }

    @Test
    public void wrongJsonParsingTest2() {
        Exception err = null;
        String json = "{\"key\": \"5\",\"a\":\"b\",}";
        try {
            CSONObject csonObject = new CSONObject(json, JSONOptions.json());
        } catch (Exception e) {
            e.printStackTrace();
            err = e;
        }
        assertNotNull(err);
        err = null;
    }
    @Test
    public void wrongJsonParsingTest() {
        Exception err = null;
        String json = "{\"key\": \"5\",,\"a\":\"b\"}";
        try {
            CSONObject csonObject = new CSONObject(json, StringFormatOption.jsonPure());
        } catch (Exception e) {
            e.printStackTrace();
            err = e;
        }
        assertNotNull(err);
        err = null;
        try {
            CSONObject csonObject = new CSONObject(json, StringFormatOption.json());
        } catch (Exception e) {
            e.printStackTrace();
            err = e;
        }
        assertNotNull(err);
        err = null;


        json = "{key: \"5\",\"a\":\"b\"}";
        try {
            CSONObject csonObject = new CSONObject(json, JSONOptions.json());
        } catch (Exception e) {
            e.printStackTrace();
            err = e;
        }
        assertNotNull(err);
        err = null;
        try {
            CSONObject csonObject = new CSONObject(json, StringFormatOption.jsonPure());
        } catch (Exception e) {
            e.printStackTrace();
            err = e;
        }
        assertNotNull(err);
        err = null;
        json = "{'key': \"5\",\"a\":\"b\"}";
        try {
            CSONObject csonObject = new CSONObject(json, JSONOptions.json());
        } catch (Exception e) {
            e.printStackTrace();
            err = e;
        }
        assertNotNull(err);



        err = null;
        json = "{\"key\": \"5\",\"a\":\"b\",}";
        try {
            CSONObject csonObject = new CSONObject(json, JSONOptions.json());
        } catch (Exception e) {
            e.printStackTrace();
            err = e;
        }
        assertNotNull(err);
        err = null;




        err = null;
        json = "{\"key\": \"5\",\"a\":\"b\"}";
        try {
            CSONObject csonObject = new CSONObject(json, JSONOptions.json());
        } catch (Exception e) {
            e.printStackTrace();
            err = e;
        }
        assertNull(err);
        err = null;

    }
}
