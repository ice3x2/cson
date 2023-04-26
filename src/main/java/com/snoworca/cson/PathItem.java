package com.snoworca.cson;

import java.util.ArrayList;
import java.util.List;

class PathItem {


    private String name;
    private int index = -1;
    private int childIndex = -1;

    private boolean inArray = false;
    private boolean arrayItem = false;
    private boolean endPoint = false;

    private PathItem(String name) {
        this.name = name;
    }


    public int getIndex() {
        return index;
    }

    public int getChildIndex() {
        return childIndex;
    }

    public String getName() {
        return name;
    }

    public boolean isArrayItem() {
        return arrayItem;
    }

    public boolean isEndPoint() {
        return endPoint;
    }

    public boolean isInArray() {
        return inArray;
    }

    private static PathItem createValueItem(String name) {
        PathItem item = new PathItem(name);
        return item;
    }

    private static PathItem createEndpointItemInArray() {
        PathItem item = new PathItem("");
        item.endPoint = true;
        item.inArray = true;
        return item;
    }

    private static PathItem createValueItem(String name, int index) {
        PathItem item = new PathItem(name);
        item.index = index;
        if(index > -1) {
            item.inArray = true;
        }
        return item;
    }


    private static PathItem createArrayItemInArray(String name, int index, int childIndex) {
        PathItem item = new PathItem(name);
        item.index = index;
        item.childIndex = childIndex;
        item.inArray = true;
        item.arrayItem = true;
        return item;
    }

    private static PathItem createArrayItem(String name, int index) {
        PathItem item = new PathItem(name);
        item.index = index;
        item.arrayItem = true;
        return item;
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
                    itemList.add(createValueItem(item));
                    continue;
                }
                String name = item.substring(0, index);
                String indexString = item.substring(index + 1, item.length() - 1);
                int indexValue = -1;
                try {
                    indexValue = Integer.parseInt(indexString);
                }  catch (NumberFormatException e) {
                    itemList.add(createValueItem(item.replace("\\.", ".")));
                    continue;
                }
                if(lastArrayItemIndex > -1) {
                    itemList.add(createArrayItemInArray(name, lastArrayItemIndex, indexValue));
                } else {
                    itemList.add(createArrayItem(name, indexValue));
                }
                lastArrayItemIndex = indexValue;
            } else {
                itemList.add(createValueItem(item, lastArrayItemIndex));
                lastArrayItemIndex = -1;
            }
        }
        int size  = itemList.size();
        if(size > 0) {
            PathItem pathItem = itemList.get(size - 1);
            if(pathItem.arrayItem) {
                itemList.add(createEndpointItemInArray());
            } else {
                pathItem.endPoint = true;
            }
        }
        return itemList;
    }
}



