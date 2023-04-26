package com.snoworca.cson.serialize;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class PathItemTest {

    @Test
    public void multiPathParseTest() {
        List<PathItem> pathItems = PathItem.parseMultiPath("key1.key2.key3.pathKey");
        assertEquals("key1", pathItems.get(0).getName());
        assertEquals("key2", pathItems.get(1).getName());
        assertEquals("key3", pathItems.get(2).getName());
        assertEquals("pathKey", pathItems.get(3).getName());

        pathItems = PathItem.parseMultiPath("..key1.[key2.key3].pathKey..");
        assertEquals("key1", pathItems.get(0).getName());
        assertEquals("[key2", pathItems.get(1).getName());
        assertEquals("key3]", pathItems.get(2).getName());
        assertEquals("pathKey", pathItems.get(3).getName());


        pathItems = PathItem.parseMultiPath("..key1...[key2\\.key3].pathKey..");
        assertEquals("key1", pathItems.get(0).getName());
        assertEquals("[key2.key3]", pathItems.get(1).getName());
        assertEquals("pathKey", pathItems.get(2).getName());

        pathItems = PathItem.parseMultiPath("..key1...[key2\\.key3].pathKey.array[10].item");
        assertEquals("key1", pathItems.get(0).getName());
        assertEquals(false, pathItems.get(0).isArrayItem());
        assertEquals("[key2.key3]", pathItems.get(1).getName());
        assertEquals(false, pathItems.get(1).isArrayItem());
        assertEquals("pathKey", pathItems.get(2).getName());
        assertEquals(false, pathItems.get(2).isArrayItem());
        assertEquals("array", pathItems.get(3).getName());
        assertEquals(10, pathItems.get(3).getIndex());
        assertEquals(true, pathItems.get(3).isArrayItem());
        assertEquals("item", pathItems.get(4).getName());
        assertEquals(false, pathItems.get(4).isArrayItem());




    }

}