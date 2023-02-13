package com.snoworca.cson;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JSON5Test {

    @Test
    public void test() {
        CSONObject csonObject = new CSONObject("{key: \"value\", key 2: \"value2\", key3: 'value3'," +
                " key4: value4 ," +
                " 'key5': \"value5!\\\r\n\tbreak line\", object: {key: value,}, 'byte[]': [+1,+2,+3,+4,5,6,7,8,9,10,Infinity,NaN,],  }");

        assertEquals("value",csonObject.get("key"));
        assertEquals("value2",csonObject.get("key 2"));
        assertEquals("value3",csonObject.get("key3"));
        assertEquals("value4",csonObject.get("key4"));
        assertEquals("value5!\tbreak line",csonObject.get("key5"));
        assertEquals(12,csonObject.getArray("byte[]").size());
        CSONArray array =  csonObject.getArray("byte[]");
        assertTrue(Double.isInfinite(array.getDouble(10)));
        assertTrue(Double.isNaN(array.getDouble(11)));

    }

    @Test
    public void testTopComment() {
        CSONObject csonObject = new CSONObject("// 루트코멘트 \n { \n" +
                "// 코멘트입니다. \n " +
                " key: \"value\" }"   );

        assertEquals("루트코멘트",csonObject.getHeadComment());

        csonObject = new CSONObject("// 루트코멘트 \n// 코멘트입니다222. \n" +
                "  /* 코\n멘\n트\r\n는\n\t주\n석\n\n\n\n*/" +
                "{ \n" +
                "// 코멘트입니다. \n" +
                " key: \"value\" }\n\n\n\n\n\r\n\t\t\t\t\t "   );

        assertEquals("루트코멘트\n코멘트입니다222.\n코\n" +
                "멘\n" +
                "트\r\n" +
                "는\n" +
                "\t주\n" +
                "석",csonObject.getHeadComment());

        assertEquals("value",csonObject.get("key"));
    }

    @Test
    public void testKeyCommentSimple() {
        //CSONObject csonObject = new CSONObject("{key:'value', /* 여기에도 코멘트가 존재 가능*/ } /** 여기도 존재가능 **/"  );
        CSONObject  csonObject = new CSONObject("{key:'value',} "  );
        assertEquals("value", csonObject.get("key"));

        csonObject = new CSONObject("{key:'value', // 코멘트 \n } "  );
        assertEquals("value", csonObject.get("key"));
        assertEquals("코멘트", csonObject.getTailCommentObject().getKeyComment());

        csonObject = new CSONObject("{key:'value' } // 코멘트 "  );
        assertEquals("코멘트", csonObject.getTailCommentObject().getKeyComment());
        csonObject = new CSONObject("{key:'value',} // 코멘트 \n // 코멘트2"  );
        assertEquals("코멘트\n코멘트2", csonObject.getTailCommentObject().getKeyComment());




    }

    @Test
    public void testKeyComment() {
        CSONObject csonObject = new CSONObject("{ \n" +
                "/* 코멘트입니다. */\n //222 \n " +
                " key: /* 값 코멘트 */ \"value\", key2: \"val/* ok */ue2\", /* 오브젝트 */ object // 오브젝트 코멘트 \n: /* 오브젝트 값 이전 코멘트 */ { p : 'ok' \n, // 이곳은? \n } // 오브젝트 코멘트 엔드 \n  , // key3comment \n 'key3' /*이상한 코멘트*/: // 값 앞 코멘트 \n 'value3' // 값 뒤 코멘트 \n /*123*/,  \n /*123*/ } /* 꼬리 다음 코멘트 */"   );

        assertEquals("코멘트입니다.\n222",csonObject.getKeyComment("key"));
        assertEquals("값 코멘트",csonObject.getValueComment("key"));

        assertEquals(null,csonObject.getKeyComment("key2"));
        assertEquals(null,csonObject.getValueComment("key2"));
        assertEquals("val/* ok */ue2",csonObject.getString("key2"));
        assertEquals("key3comment\n이상한 코멘트",csonObject.getKeyComment("key3"));
        assertEquals("이상한 코멘트",csonObject.getCommentObject("key3").getAfterKey());
        assertEquals("값 앞 코멘트\n값 뒤 코멘트\n123",csonObject.getCommentObject("key3").getValueComment());
        assertEquals("값 뒤 코멘트\n123",csonObject.getCommentObject("key3").getAfterValue());
        assertEquals("123\n꼬리 다음 코멘트",csonObject.getTailCommentObject().getKeyComment());

        CommentObject commentObject = csonObject.getCommentObject("object");
        assertEquals("오브젝트",commentObject.getBeforeKey());
        CSONObject subObject =  csonObject.getObject("object");
        assertEquals("ok",subObject.get("p"));
        assertEquals(null,subObject.getHeadComment());
        assertEquals("이곳은?",subObject.getTailComment());

        assertEquals("오브젝트 코멘트",commentObject.getAfterKey());


    }




}
