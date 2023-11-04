package com.snoworca.cson.object;

import com.snoworca.cson.CSONArray;
import com.snoworca.cson.CSONObject;
import com.snoworca.cson.JSONOptions;

import java.util.ArrayList;
import java.util.Map;

public class SchemaArrayNode extends SchemaElementNode {

    private final ArrayList<SchemaNode> list = new ArrayList<>();



    public ArrayList<SchemaNode> getList() {
        return list;
    }

    public void add(SchemaNode node) {
        list.add(node);
    }

    public SchemaNode get(int index) {
        return list.get(index);
    }

    public int size() {
        return list.size();
    }

    public void set(int index, SchemaNode node) {
        while(list.size() <= index) list.add(null);
        list.set(index, node);
    }


    public void remove(int index) {
        list.remove(index);
    }

    public SchemaObjectNode getObjectNode(int index) {
        return (SchemaObjectNode) list.get(index);
    }

    public SchemaArrayNode getArrayNode(int index) {
        return (SchemaArrayNode) list.get(index);
    }


    @Override
    public SchemaNode copyNode() {
        SchemaArrayNode arrayNode = new SchemaArrayNode();
        for(int i = 0, n = list.size(); i < n; i++) {
            SchemaNode node = list.get(i);
            arrayNode.add(node.copyNode());
        }
        return arrayNode;
    }



}
