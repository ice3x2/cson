package com.snoworca.cson.serialize;

import java.util.ArrayList;
import java.util.List;

class PathItem {

    enum PathItemType {
        ARRAY_ITEM,
        IN_ARRAY_ARRAY_ITEM,
        IN_ARRAY_OBJECT_ITEM,
        OBJECT_ITEM,
        END_POINT
    }

    private PathItemType pathItemType;
    private String name;
    private int index;

    private PathItem(String name, int index, PathItemType pathItemType) {
        this.name = name;
        this.index = index;
        this.pathItemType = pathItemType;

    }


    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public boolean isArrayItem() {
        return this.pathItemType == PathItemType.ARRAY_ITEM;
    }

    public boolean isInArray() {
        return this.pathItemType == PathItemType.IN_ARRAY_ARRAY_ITEM || this.pathItemType == PathItemType.IN_ARRAY_OBJECT_ITEM;
    }


    // key.arrayKey[0].array2[1][2]
    // (ro) key(o) arrayKey

    // name(key) : value(object) <- name(arrayKey) : value(array) <- index(0) : array2 <- index(1) : array2 <- index(2) : array2

    // key 는 object 타입안에 있는 object value => name 끝에 . 이 있음 object value
    // arrayKey 는 object type 안에 있는 array

    //
    private final static int READ_MODE_KEY = 0;
    private final static int READ_MODE_INDEX = 1;
    private final static int READ_MODE_UNDEFINED = -1;


    protected static List<PathItem> parseMultiPath2(String path) {
        ArrayList<PathItem> itemList = new ArrayList<>();
        char[] chars = path.toCharArray();
        StringBuilder builder = new StringBuilder(path.length());
        PathItem lastItem = null;
        int readMode = READ_MODE_UNDEFINED;
        for(int i = 0, n = chars.length; i < n; i++) {
            char c = chars[i];
            if(c == '.') {
                String key = builder.toString();
                key = key.trim();
                builder.setLength(0);
                if(!key.isEmpty()) {
                    PathItem item = new PathItem(key, -1, PathItemType.OBJECT_ITEM);
                    itemList.add(item);
                }
                readMode = READ_MODE_KEY;
            } else if(c == '[') {

                String key = builder.toString();
                key = key.trim();
                builder.setLength(0);
                if(key.length() > 0) {
                    PathItem item = new PathItem(key, -1, PathItemType.OBJECT_ITEM);
                    itemList.add(item);
                }

                readMode = READ_MODE_INDEX;
            } else if(c == ']' && readMode == READ_MODE_INDEX) {
                String indexString = builder.toString().trim();
                builder.setLength(0);
                int index = Integer.parseInt(indexString);
                lastItem = new PathItem("", index, PathItemType.ARRAY_ITEM);
                itemList.add(lastItem);
                readMode = READ_MODE_UNDEFINED;
            } else {
                builder.append(c);
            }
        }
        String key = builder.toString();
        key = key.trim();
        if(!key.isEmpty()) {
            PathItem item = new PathItem(key, -1, PathItemType.OBJECT_ITEM);
            itemList.add(item);
        }

        return itemList;


    }

    protected static List<PathItem> parseMultiPath(String path) {
        ArrayList<PathItem> itemList = new ArrayList<>();
        String[] items = path.split("(?<!\\\\)\\.");
        int lastArrayItemIndex = -1;
        for(int i = 0, n = items.length; i < n; i++) {
            String item = items[i].trim();
            if(item.isEmpty()) continue;
            if(item.endsWith("]")) {
                int index = item.indexOf('[');
                if(index == -1) {
                    itemList.add(new PathItem(item, -1, PathItemType.OBJECT_ITEM));
                    continue;
                }
                String name = item.substring(0, index);
                String indexString = item.substring(index + 1, item.length() - 1);
                int indexValue = -1;
                try {
                    indexValue = Integer.parseInt(indexString);
                }  catch (NumberFormatException e) {
                    itemList.add(new PathItem(item.replace("\\.", "."), -1, PathItemType.OBJECT_ITEM));
                    continue;
                }
                if(lastArrayItemIndex > -1) {
                    itemList.add(new PathItem(name, indexValue, PathItemType.IN_ARRAY_ARRAY_ITEM));
                } else {
                    itemList.add(new PathItem(name, indexValue, PathItemType.ARRAY_ITEM));
                }
                lastArrayItemIndex = indexValue;
            } else {
                itemList.add(new PathItem(item, lastArrayItemIndex, lastArrayItemIndex > -1 ? PathItemType.IN_ARRAY_OBJECT_ITEM :  PathItemType.OBJECT_ITEM));
                lastArrayItemIndex = -1;
            }
        }
        int size  = itemList.size();
        if(size > 0) {
            PathItem pathItem = itemList.get(size - 1);
            if(pathItem.pathItemType == PathItemType.ARRAY_ITEM || pathItem.pathItemType == PathItemType.IN_ARRAY_ARRAY_ITEM) {
                itemList.add(new PathItem("", pathItem.index, PathItemType.END_POINT));

            }
            pathItem.pathItemType = PathItemType.END_POINT;
        }
        return itemList;

    }
}



