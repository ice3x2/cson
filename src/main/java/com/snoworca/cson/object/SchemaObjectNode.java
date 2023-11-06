package com.snoworca.cson.object;




import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class SchemaObjectNode extends SchemaElementNode {

    private final Map<String, SchemaNode> map = new LinkedHashMap<>();
    private FieldRack fieldRack;


    public SchemaObjectNode() {}


    public FieldRack getFieldRack() {
        return fieldRack;
    }

    public SchemaObjectNode setFieldRack(FieldRack fieldRack) {
        this.fieldRack = fieldRack;
        return this;
    }



    public SchemaNode get(String key) {
        return map.get(key);
    }


    public void put(String key, SchemaNode value) {
        if(value instanceof SchemaObjectNode) {

            ((SchemaObjectNode) value).setParent(this);
        } else if(value instanceof SchemaArrayNode)
            ((SchemaArrayNode) value).setParent(this);
        map.put(key, value);
    }


    public Map<String, SchemaNode> getMap() {
        return map;
    }

    public SchemaArrayNode getArrayNode(String key) {
        return (SchemaArrayNode) map.get(key);
    }

    public SchemaObjectNode getObjectNode(String key) {
        return (SchemaObjectNode) map.get(key);
    }

    public SchemaObjectNode copyNode() {
        SchemaObjectNode objectNode = new SchemaObjectNode();
        for(Map.Entry<String, SchemaNode> entry : map.entrySet()) {
            SchemaNode node = entry.getValue().copyNode();
            if(node instanceof SchemaElementNode) {
                ((SchemaElementNode) node).setParentFieldRackList(getParentFieldRackList());
            }

            objectNode.put(entry.getKey(), node);
        }
        return objectNode;
    }



    public Set<String> keySet() {
        return map.keySet();
    }


    @Override
    public void merge(SchemaElementNode schemaElementNode) {
        if(schemaElementNode instanceof SchemaObjectNode) {
            SchemaObjectNode objectNode = (SchemaObjectNode) schemaElementNode;
            Set<Map.Entry<String, SchemaNode>> entrySet = objectNode.map.entrySet();
            for(Map.Entry<String, SchemaNode> entry : entrySet) {
                String key = entry.getKey();
                SchemaNode node = entry.getValue();
                SchemaNode thisNode = map.get(key);
                if(thisNode instanceof  SchemaObjectNode && node instanceof SchemaObjectNode) {
                    ((SchemaObjectNode) thisNode).merge((SchemaObjectNode) node);
                } else if(thisNode instanceof SchemaArrayNode && node instanceof SchemaArrayNode) {
                    ((SchemaArrayNode) thisNode).merge((SchemaArrayNode) node);
                } else {
                    map.put(key, node);
                }
            }
        }
        addParentFieldRackAll(schemaElementNode.getParentFieldRackList());

    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{");
        Set<Map.Entry<String, SchemaNode>> entrySet = map.entrySet();
        for(Map.Entry<String, SchemaNode> entry : entrySet) {
            stringBuilder.append(entry.getKey()).append(":").append(entry.getValue().toString()).append(",");
        }
        if(stringBuilder.length() > 1) {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

}
