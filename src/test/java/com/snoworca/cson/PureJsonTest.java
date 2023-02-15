package com.snoworca.cson;

import org.junit.Test;

import java.util.concurrent.Executors;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class PureJsonTest {
    @Test
    public void wrongJsonParsingTest() {
        Exception err = null;
        String json = "{\"key\": \"5\",,\"a\":\"b\"}";
        try {
            CSONObject csonObject = new CSONObject(json, Options.PureJson);
        } catch (Exception e) {
            e.printStackTrace();
            err = e;
        }
        assertNotNull(err);
        err = null;

        json = "{key: \"5\",\"a\":\"b\"}";
        try {
            CSONObject csonObject = new CSONObject(json, Options.PureJson);
        } catch (Exception e) {
            e.printStackTrace();
            err = e;
        }
        assertNotNull(err);
        err = null;
        json = "{'key': \"5\",\"a\":\"b\"}";
        try {
            CSONObject csonObject = new CSONObject(json, Options.PureJson);
        } catch (Exception e) {
            e.printStackTrace();
            err = e;
        }
        assertNotNull(err);



        err = null;
        json = "{\"key\": \"5\",\"a\":\"b\",}";
        try {
            CSONObject csonObject = new CSONObject(json, Options.PureJson);
        } catch (Exception e) {
            e.printStackTrace();
            err = e;
        }
        assertNotNull(err);
        err = null;




        err = null;
        json = "{\"key\": \"5\",\"a\":\"b\"}";
        try {
            CSONObject csonObject = new CSONObject(json, Options.PureJson);
        } catch (Exception e) {
            e.printStackTrace();
            err = e;
        }
        assertNull(err);
        err = null;

    }
}
