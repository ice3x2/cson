package com.snoworca.cson.serialize;

import com.snoworca.cson.serialize.PathItem;
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
        assertEquals(false, pathItems.get(3).isArrayItem());
        assertEquals(true, pathItems.get(4).isArrayItem());
        assertEquals(10, pathItems.get(4).getChildIndex());
        assertEquals("item", pathItems.get(5).getName());
        assertEquals(false, pathItems.get(5).isArrayItem());
        assertEquals(true, pathItems.get(5).isEndPoint());

/*

        pathItems = PathItem.parseMultiPath("key[10].key1.key2[2].key3[5]");
        assertEquals("key", pathItems.get(0).getName());
        assertEquals(10, pathItems.get(0).getIndex());
        assertEquals(true, pathItems.get(0).isArrayItem());
        assertEquals("key1", pathItems.get(1).getName());
        assertEquals(false, pathItems.get(1).isArrayItem());
        assertEquals(true, pathItems.get(1).isInArray());
        assertEquals(10, pathItems.get(1).getIndex());
        assertEquals("key2", pathItems.get(2).getName());
        assertEquals(2, pathItems.get(2).getIndex());
        assertEquals(false, pathItems.get(2).isInArray());
        assertEquals(true, pathItems.get(2).isArrayItem());
        assertEquals("key3", pathItems.get(3).getName());
        assertEquals(2, pathItems.get(3).getIndex());
        assertEquals(true, pathItems.get(3).isArrayItem());
        assertEquals(true, pathItems.get(3).isInArray());
        assertEquals(5, pathItems.get(3).getChildIndex());
        assertEquals(true, pathItems.get(4).isEndPoint());
        assertEquals(true, pathItems.get(4).isInArray());
        assertEquals(5, pathItems.get(4).getIndex());*/









    }

}