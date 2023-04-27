package com.snoworca.cson.serialize;

import com.snoworca.cson.CSONArray;
import com.snoworca.cson.CSONElement;
import com.snoworca.cson.CSONObject;
import com.snoworca.cson.CSONPath;

import java.util.List;

public class CsonGeneratorViaPathItem {

    public static void generate(CSONElement parent, List<PathItem> itemList, Object value) {
        CSONElement parentElement = parent;
        for (int i = 0; i < itemList.size(); i++) {
            PathItem item = itemList.get(i);
            if(item.isEndPoint()) {
                if(item.isInArray()) {
                    ((CSONArray)parentElement).put(item.getIndex(), value);
                } else {
                    ((CSONObject)parentElement).put(item.getName(), value);
                }
            } else if(item.isInArray()) {
                if (parentElement instanceof CSONArray) {
                    CSONArray array = (CSONArray) parentElement;
                    int index = item.getIndex();
                    Object indexValue = array.opt(index);
                    if (item.isArrayItem() && !(indexValue instanceof CSONArray)) {
                        indexValue = new CSONArray();
                        array.put(index, indexValue);
                    } else if (!item.isArrayItem() && !(indexValue instanceof CSONObject)) {
                        indexValue = new CSONObject();
                        array.put(index, indexValue);
                    }
                    //TODO 적절한 예외처리
                } else {
                    //TODO 적절한 예외처리
                    throw new RuntimeException("CSONElement is not CSONArray");
                }
            } else {
                if (parentElement instanceof CSONObject) {
                    CSONObject object = (CSONObject) parentElement;
                    String name = item.getName();
                    Object childObject = object.opt(name);
                    //TODO 적절한 예외처리
                    if (item.isArrayItem() && !(childObject instanceof CSONArray)) {
                        childObject = new CSONArray();
                        object.put(name, childObject);
                    } else if (!item.isArrayItem() && !(childObject instanceof CSONObject)) {
                        childObject = new CSONObject();
                        object.put(name, childObject);
                    }
                    parentElement = (CSONElement) childObject;
                } else {
                    //TODO 적절한 예외처리
                    throw new RuntimeException("CSONElement is not CSONObject");
                }
            }
        }
    }





}
