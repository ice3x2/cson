package com.snoworca.cson.object;

import java.util.ArrayList;

public class ArrayNode implements Node {
    private final ArrayList<Node> list = new ArrayList<>();

    public ArrayList<Node> getList() {
        return list;
    }

    public void add(Node node) {
        list.add(node);
    }

    public Node get(int index) {
        return list.get(index);
    }

    public int size() {
        return list.size();
    }

    public void set(int index, Node node) {
        while(list.size() <= index) list.add(null);
        list.set(index, node);
    }

    public void remove(int index) {
        list.remove(index);
    }

    public ObjectNode getObjectNode(int index) {
        return (ObjectNode) list.get(index);
    }

    public ArrayNode getArrayNode(int index) {
        return (ArrayNode) list.get(index);
    }




}
