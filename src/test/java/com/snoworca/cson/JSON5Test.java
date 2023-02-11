package com.snoworca.cson;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JSON5Test {

    @Test
    public void test() {
        CSONObject csonObject = new CSONObject("{key: \"value\", key 2: \"value2\", key3: 'value3'," +
                " key4: value4 ," +
                " 'key5': \"value5!\\\r\nbreak line\", object: {key: value,}, 'byte[]': [+1,+2,+3,+4,5,6,7,8,9,10,Infinity,NaN,],  }");

        assertEquals("value",csonObject.get("key"));
        assertEquals("value2",csonObject.get("key 2"));
        assertEquals("value3",csonObject.get("key3"));
        assertEquals("value4",csonObject.get("key4"));
        assertEquals("value5!\r\nbreak line",csonObject.get("key5"));
        assertEquals(12,csonObject.getArray("byte[]").size());
        CSONArray array =  csonObject.getArray("byte[]");
        assertTrue(Double.isInfinite(array.getDouble(10)));
        assertTrue(Double.isNaN(array.getDouble(11)));

    }

    @Test
    public void testComment() {
        CSONObject csonObject = new CSONObject("{ \n" +
                "// 코멘트입니다. \n " +
                " key: \"value\" }"   );



    }


}
