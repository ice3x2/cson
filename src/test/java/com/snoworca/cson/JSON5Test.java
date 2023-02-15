package com.snoworca.cson;

import org.json.JSONArray;
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
        assertEquals("코멘트", csonObject.getTailComment());

        csonObject = new CSONObject("{key:'value' } // 코멘트 "  );
        assertEquals("코멘트", csonObject.getTailComment());
        csonObject = new CSONObject("{key:'value',} // 코멘트 \n // 코멘트2"  );
        assertEquals("코멘트\n코멘트2", csonObject.getTailComment());
    }

    @Test
    public void testArrayComment() {
        JSONArray jsonArray = new JSONArray("[1,,2,3,4,5,6,7,8,9,10,Infinity,NaN,]"  );
        Object obj =  jsonArray.get(1);

        CSONArray csonArray = null;
        csonArray = new CSONArray("[//index1\n1,2,3,4,5,6,7,8,9,10,Infinity,NaN,] // 코멘트 \n // 코멘트2"  );
        assertEquals("index1",csonArray.getCommentObject(0).getBeforeComment());


        csonArray = new CSONArray("/*테*///스\n" +
                "/*트*/[//index1\n" +
                "1\n" +
                "//index1After\n" +
                ",,/* 이 곳에 주석 가능 */,\"3 \"/*index 3*/,4,5,6,7,8,9,10,11," +
                "/*오브젝트 시작*/{/*알수없는 영역*/}/*오브젝트끝*/,13,//14\n" +
" {/*123*/123:456//456\n,},/*15배열로그*/[,,]/*15after*/,[],[],+Infinity,NaN," +
                ",[{},{}],[,/*index22*/],[,/*index23*/]/*index23after*/,24,[,]//index25after\n," +
                "{1:2,//코멘트\n}//코멘트\n,] // 코멘트 \n // 코멘트2"  );
        System.out.println(csonArray);
        assertEquals("index1",csonArray.getCommentObject(0).getBeforeComment());
        assertEquals("테\n스\n트",csonArray.getHeadComment());
        assertEquals("index1After",csonArray.getCommentObject(0).getAfterComment());
        assertEquals(1,csonArray.getInteger(0));
        assertEquals(null,csonArray.get(1));
        assertEquals(null,csonArray.get(2));
        assertEquals("이 곳에 주석 가능",csonArray.getCommentObject(2).getBeforeComment());
        assertEquals("3 ",csonArray.get(3));
        assertEquals(3,csonArray.getInteger(3));
        assertEquals("index 3",csonArray.getCommentObject(3).getAfterComment());

        assertEquals("오브젝트 시작", csonArray.getObject(12).getHeadComment());
        assertEquals("알수없는 영역", csonArray.getObject(12).getTailCommentObject().getBeforeComment());
        assertEquals("오브젝트끝", csonArray.getObject(12).getTailCommentObject().getAfterComment());

        assertEquals("오브젝트끝",csonArray.getCommentObject(12).getAfterComment());

        assertEquals("+Infinity",csonArray.get(18));
        assertEquals("NaN",csonArray.get(19));


        CSONArray idx15Array = csonArray.getArray(15);
        assertEquals("15배열로그",csonArray.getCommentObject(15).getBeforeComment());
        assertEquals("15배열로그",idx15Array.getHeadComment());
        assertEquals("15after",csonArray.getCommentObject(15).getAfterComment());
        assertEquals(null, idx15Array.get(0));
        assertEquals(null, idx15Array.get(1));


        assertTrue(Double.isInfinite(csonArray.getDouble(18)));
        assertTrue(Double.isNaN(csonArray.getDouble(19)));


    }

    @Test
    public void testKeyComment() {
        CSONObject csonObject = new CSONObject("{ \n" +
                "/* 코멘트입니다. */\n //222 \n " +
                " key: /* 값 코멘트 */ \"value\", key2: \"val/* ok */ue2\",array:[1,2,3,4,],/*코멘트array2*/array2/*코멘트array2*/:/*코멘트array2b*/[1,2,3,4]/*코멘트array2a*/,/* 오브젝트 */ object " +
                "// 오브젝트 코멘트 \n: /* 오브젝트 값 이전 코멘트 */ { p : 'ok' \n, // 이곳은? \n } // 오브젝트 코멘트 엔드 \n  , // key3comment \n 'key3'" +
                " /*이상한 코멘트*/: // 값 앞 코멘트 \n 'value3' // 값 뒤 코멘트 \n /*123 */,  \n /*123*/ } /* 꼬리 다음 코멘트 */"   );

        System.out.println(csonObject);

        assertEquals("코멘트입니다.\n222",csonObject.getCommentOfKey("key"));
        assertEquals("값 코멘트",csonObject.getCommentOfValue("key"));

        assertEquals(null,csonObject.getCommentOfKey("key2"));
        assertEquals(null,csonObject.getCommentOfValue("key2"));
        assertEquals("val/* ok */ue2",csonObject.getString("key2"));
        assertEquals("key3comment\n이상한 코멘트",csonObject.getCommentOfKey("key3"));
        assertEquals("이상한 코멘트",csonObject.getCommentObjectOfKey("key3").getAfterComment());
        assertEquals("값 앞 코멘트\n값 뒤 코멘트\n123",csonObject.getCommentOfValue("key3"));
        assertEquals("값 뒤 코멘트\n123",csonObject.getCommentObjectOfValue("key3").getAfterComment());
        assertEquals("123\n꼬리 다음 코멘트",csonObject.getTailComment());

        CommentObject keyCommentObject = csonObject.getCommentObjectOfKey("object");
        assertEquals("오브젝트", keyCommentObject.getBeforeComment());
        CSONObject subObject =  csonObject.getObject("object");
        assertEquals("ok",subObject.get("p"));

        CommentObject valueCommentObject = csonObject.getCommentObjectOfValue("object");
        assertEquals(csonObject.getCommentObjectOfValue("object").getBeforeComment(),subObject.getHeadComment());
        assertEquals("이곳은?",subObject.getTailCommentObject().getBeforeComment());
        assertEquals("오브젝트 코멘트 엔드",subObject.getTailCommentObject().getAfterComment());

        assertEquals("오브젝트 코멘트", keyCommentObject.getAfterComment());


    }




}
