package com.snoworca.cson.serialize;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

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
        assertTrue(pathItemsB.get(0).isArrayItem());
        assertFalse(pathItemsB.get(0).isInArray());
        assertEquals(0, pathItemsB.get(1).getIndex());
        assertEquals("key2", pathItemsB.get(1).getName());
        assertTrue(pathItemsB.get(1).isArrayItem());
        assertTrue(pathItemsB.get(1).isInArray());
        assertEquals("key3", pathItemsB.get(2).getName());
        assertTrue(pathItemsB.get(2).isArrayItem());
        assertTrue(pathItemsB.get(2).isInArray());
        assertEquals("pathKey", pathItemsB.get(3).getName());
        assertTrue(pathItemsB.get(3).isArrayItem());
        assertTrue(pathItemsB.get(3).isInArray());
        assertEquals(3, pathItemsB.get(4).getIndex());
        asserT



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


}