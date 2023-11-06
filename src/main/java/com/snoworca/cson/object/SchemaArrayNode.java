package com.snoworca.cson.object;

import java.util.ArrayList;

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


    @Override
    public void merge(SchemaElementNode schemaElementNode) {
        if(schemaElementNode instanceof SchemaArrayNode) {
            SchemaArrayNode arrayNode = (SchemaArrayNode) schemaElementNode;
            for(int i = 0, n = arrayNode.size(); i < n; i++) {
                SchemaNode node = arrayNode.get(i);
                if(i < list.size()) {
                    SchemaNode oldNode = list.get(i);
                    if(oldNode instanceof SchemaObjectNode && node instanceof SchemaObjectNode) {
                        ((SchemaObjectNode) oldNode).merge((SchemaObjectNode) node);
                    } else if(oldNode instanceof SchemaArrayNode && node instanceof SchemaArrayNode) {
                        ((SchemaArrayNode) oldNode).merge((SchemaArrayNode) node);
                    } else {
                        list.set(i, node);
                    }
                } else {
                    list.add(node);
                }
            }
        }

    }
}
