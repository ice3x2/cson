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



