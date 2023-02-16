package com.snoworca.cson;

import org.junit.Test;

import static org.junit.Assert.*;

public class PureJsonTest {

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
