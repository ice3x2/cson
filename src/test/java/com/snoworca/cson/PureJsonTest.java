package com.snoworca.cson;

import org.junit.Test;

import java.io.StringReader;

import static org.junit.Assert.*;

public class PureJsonTest {


    @Test
    public void dutyJSON() {
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
                "        \"total\": 150.25,\n" +
                "        \"items\": [\n" +
                "          {\n" +
                "            \"name\": \"Mystery Box\",\n" +
                "            \"quantity\": 1,\n" +
                "            \"price\": 75.50\n" +
                "          },\n" +
                "          {\n" +
                "            \"name\": \"Cryptic Scroll\",\n" +
                "            \"quantity\": 2,\n" +
                "            \"price\": 37.75\n" +
                "          }\n" +
                "        ]\n" +
                "      },\n" +
                "      {\n" +
                "        \"id\": \"ORD002\",\n" +
                "        \"date\": \"2023-12-10\",\n" +
                "        \"total\": 0xff,\n" +
                "        \"items\": [\n" +
                "          {\n" +
                "            \"name\": \"Enigmatic Puzzle\",\n" +
                "            \"quantity\": 1,\n" +
                "            \"price\": 180.00\n" +
                "          },\n" +
                "          {\n" +
                "            \"name\": \"Secret Cipher\",\n" +
                "            \"quantity\": 1,\n" +
                "            \"price\": 40.00\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}";

        CSONElement csonElement = PureJSONParser.parsePureJSON(new StringReader(testJSON));

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
            CSONObject csonObject = new CSONObject(json, JSONOptions.json());
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
