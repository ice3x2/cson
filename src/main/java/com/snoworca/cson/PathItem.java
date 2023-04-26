package com.snoworca.cson;

import java.util.ArrayList;
import java.util.List;

public class PathItem {
    private String name;
    private int index;

    private boolean isEndPoint;
    private boolean isArrayIndex;


    private PathItem(String name, int index, boolean isArrayIndex) {
        this.name = name;
        this.index = index;
        this.isArrayIndex = isArrayIndex;
    }

    public int getIndex() {
        return index;
    }

    public boolean isEndPoint() {
        return isEndPoint;
    }

    public String getName() {
        return name;
    }

    public boolean isArrayItem() {
        return isArrayIndex;
    }

    protected static List<PathItem> parseMultiPath(String path) {
        ArrayList<PathItem> itemList = new ArrayList<>();
        String[] items = path.split("(?<!\\\\)\\.");
        for(int i = 0, n = items.length; i < n; i++) {
            String item = items[i].trim();
            if(item.isEmpty()) continue;
            if(item.endsWith("]")) {
                int index = item.indexOf('[');
                if(index == -1) {
                    itemList.add(new PathItem(item, -1, false));
                    continue;
                }
                String name = item.substring(0, index);
                String indexString = item.substring(index + 1, item.length() - 1);
                int indexValue = -1;
                try {
                    indexValue = Integer.parseInt(indexString);
                }  catch (NumberFormatException e) {
                    itemList.add(new PathItem(item.replace("\\.", "."), -1, false));
                    continue;
                }
                itemList.add(new PathItem(name, indexValue, true));
            } else {
                itemList.add(new PathItem(item, -1, false));
            }
        }
        return itemList;

    }
}



