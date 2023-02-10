package com.snoworca.cson;

import org.junit.Test;

import java.util.LinkedHashMap;

import static org.junit.Assert.*;

public class YAMLParserTest {

        @Test
        public void test() {
            CSONObject csonObject = YAMLParser.parse("key: value\n");
            assertEquals(csonObject.get("key").toString(), "value");
        }
}