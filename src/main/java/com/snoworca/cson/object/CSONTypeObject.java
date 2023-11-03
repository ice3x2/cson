package com.snoworca.cson.object;
;
import com.snoworca.cson.CSONArray;
import com.snoworca.cson.CSONObject;

import java.util.Iterator;
import java.util.Map;

public class CSONTypeObject extends CSONObject implements Cloneable {

    public CSONTypeObject() {
        super();
        setAllowRawValue(true);
    }

    @Override
    public CSONObject clone() {
        CSONTypeObject csonObject = new CSONTypeObject();
        Iterator<Map.Entry<String, Object>> iter = dataMap.entrySet().iterator();

        while(iter.hasNext()) {
            Map.Entry<String, Object> entry = iter.next();
            String key = entry.getKey();
            Object obj = entry.getValue();
            if(obj instanceof CSONTypeArray) csonObject.put(key, ((CSONTypeArray)obj).clone());
            else if(obj instanceof CSONTypeObject) csonObject.put(key, ((CSONTypeObject)obj).clone());
            else if(obj instanceof CharSequence) csonObject.put(key, ((CharSequence)obj).toString());
            else if(obj instanceof FieldRack) csonObject.put(key, ((FieldRack)obj).copy());
            else if(obj instanceof byte[]) {
                byte[] bytes = (byte[])obj;
                byte[] newBytes = new byte[bytes.length];
                System.arraycopy(bytes, 0, newBytes, 0, bytes.length);
                csonObject.put(key, newBytes);
            }
            else csonObject.put(key, obj);
        }
        return csonObject;
    }
}
