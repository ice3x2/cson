package com.snoworca.cson.serialize;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class PathItemTest {

    @Test
    public void multiArrayPathParseTest() {
        List<PathItem> pathItemBasic = PathItem.parseMultiPath2("key1.key3.key2.key4");
        assertEquals("key1", pathItemBasic.get(0).getName());
        assertEquals("key3", pathItemBasic.get(1).getName());
        assertEquals("key2", pathItemBasic.get(2).getName());
        assertEquals("key4", pathItemBasic.get(3).getName());

        List<PathItem> pathItems = PathItem.parseMultiPath2("key1[0][1][2][3]");
        assertEquals("key1", pathItems.get(0).getName());
        assertEquals(0, pathItems.get(1).getIndex());
        assertEquals(1, pathItems.get(2).getIndex());
        assertEquals(2, pathItems.get(3).getIndex());
        assertEquals(3, pathItems.get(4).getIndex());

        List<PathItem>  pathItemsB = PathItem.parseMultiPath2("key1[0].key2[1].key3[2].pathKey[3]");
        assertEquals("key1", pathItemsB.get(0).getName());
        assertEquals(0, pathItemsB.get(1).getIndex());
        assertEquals("key2", pathItemsB.get(2).getName());
        assertEquals(1, pathItemsB.get(3).getIndex());
        assertEquals("key3", pathItemsB.get(4).getName());
        assertEquals(2, pathItemsB.get(5).getIndex());
        assertEquals("pathKey", pathItemsB.get(6).getName());
        assertEquals(3, pathItemsB.get(7).getIndex());

        pathItemsB = PathItem.parseMultiPath2("key1[0]key2[1]key3[2]pathKey[3]");
        assertEquals("key1", pathItemsB.get(0).getName());
        assertEquals(0, pathItemsB.get(1).getIndex());
        assertEquals("key2", pathItemsB.get(2).getName());
        assertEquals(1, pathItemsB.get(3).getIndex());
        assertEquals("key3", pathItemsB.get(4).getName());
        assertEquals(2, pathItemsB.get(5).getIndex());
        assertEquals("pathKey", pathItemsB.get(6).getName());
        assertEquals(3, pathItemsB.get(7).getIndex());

        List<PathItem> pathItemsC = PathItem.parseMultiPath2("[100][0][1][2][3]");
        assertEquals(100, pathItemsC.get(0).getIndex());
        assertEquals(0, pathItemsC.get(1).getIndex());
        assertEquals(1, pathItemsC.get(2).getIndex());
        assertEquals(2, pathItemsC.get(3).getIndex());
        assertEquals(3, pathItemsC.get(4).getIndex());


    }
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