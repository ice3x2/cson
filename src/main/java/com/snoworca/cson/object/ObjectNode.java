package com.snoworca.cson.object;


import java.util.LinkedHashMap;
import java.util.Map;

public class ObjectNode implements Node {

    private ObjectNode parent;
    private final Map<String, Node> map = new LinkedHashMap<>();
    private FieldRack fieldRack;
    private FieldRack parentFieldRack;

    public ObjectNode() {}

    public FieldRack getFieldRack() {
        return fieldRack;
    }

    public ObjectNode setFieldRack(FieldRack fieldRack) {
        this.fieldRack = fieldRack;
        return this;
    }

    public FieldRack getParentFieldRack() {
        return parentFieldRack;
    }

    public ObjectNode setParentFieldRack(FieldRack parentFieldRack) {
        this.parentFieldRack = parentFieldRack;
        return this;
    }


    public Node getParent() {
        return parent;
    }

    public ObjectNode setParent(ObjectNode parent) {
        this.parent = parent;
        return this;
    }

    public Node get(String key) {
        return map.get(key);
    }


    public void put(String key, Node value) {
        map.put(key, value);
    }


    public Map<String, Node> getMap() {
        return map;
    }

    public ArrayNode getArrayNode(String key) {
        return (ArrayNode) map.get(key);
    }

    public ObjectNode getObjectNode(String key) {
        return (ObjectNode) map.get(key);
    }

}
