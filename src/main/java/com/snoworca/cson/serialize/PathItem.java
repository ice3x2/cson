package com.snoworca.cson.serialize;

import java.util.ArrayList;
import java.util.List;

public class PathItem {

    private final static int READ_MODE_KEY = 0;
    private final static int READ_MODE_INDEX = 1;
    private final static int READ_MODE_UNDEFINED = -1;

    private final String name;
    private final int index;


    private boolean isArrayItem;
    private boolean isEndPoint;


    private PathItem(String name, int index) {
        this.name = name;
        this.index = index;
        if(index > -1) {
            isArrayItem = true;
        }
    }

    private PathItem(int index) {
        this.name = "";
        this.index = index;
        if(index > -1) {
            isArrayItem = true;
        }
    }

    private PathItem(String key) {
        this.name = key;
        this.index = -1;
    }

    public boolean isEndPoint() {
        return isEndPoint;
    }


    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public boolean isArrayItem() {
        return isArrayItem;
    }



    public static List<PathItem> parseMultiPath2(String path) {
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
                    PathItem item = new PathItem(key, -1);
                    itemList.add(item);
                }
                readMode = READ_MODE_KEY;
            } else if(c == '[') {

                String key = builder.toString();
                key = key.trim();
                builder.setLength(0);
                if(key.length() > 0) {
                    PathItem item = new PathItem(key);
                    itemList.add(item);
                }

                readMode = READ_MODE_INDEX;
            } else if(c == ']' && readMode == READ_MODE_INDEX) {
                String indexString = builder.toString().trim();
                builder.setLength(0);
                int index = Integer.parseInt(indexString);
                lastItem = new PathItem(index);
                itemList.add(lastItem);
                readMode = READ_MODE_UNDEFINED;
            } else {
                builder.append(c);
            }
        }
        String key = builder.toString();
        key = key.trim();
        if(!key.isEmpty()) {
            PathItem item = new PathItem(key);
            itemList.add(item);
        }
        if(itemList.size() > 0) {
            lastItem = itemList.get(itemList.size() - 1);
            lastItem.isEndPoint = true;
        }

        return itemList;


    }

}



