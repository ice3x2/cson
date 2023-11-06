package com.snoworca.cson.object;


import com.snoworca.cson.PathItem;

import java.util.List;

public class NodePath {

    private final SchemaElementNode node;

    public NodePath(SchemaElementNode Node) {
        this.node = Node;
    }



    private SchemaElementNode obtainOrCreateChild(SchemaElementNode Node, PathItem pathItem) {
        if(Node instanceof SchemaObjectNode && !pathItem.isInArray() && pathItem.isObject()) {
            SchemaObjectNode ObjectNode = (SchemaObjectNode)Node;
            String name = pathItem.getName();
            if(pathItem.isArrayValue()) {
                SchemaArrayNode childArrayNode = ObjectNode.getArrayNode(name);
                if(childArrayNode == null) {
                    childArrayNode = new SchemaArrayNode();
                    ObjectNode.put(name, childArrayNode);
                }

                return childArrayNode;
            } else {
                SchemaObjectNode childObjectNode = ObjectNode.getObjectNode(name);
                if(childObjectNode == null) {
                    childObjectNode = new SchemaObjectNode();
                    ObjectNode.put(name, childObjectNode);
                }
                return childObjectNode;
            }
        } else if(Node instanceof SchemaArrayNode && pathItem.isInArray()) {
            SchemaArrayNode ArrayNode = (SchemaArrayNode)Node;
            int index = pathItem.getIndex();
            if(pathItem.isObject()) {
                SchemaObjectNode childObjectNode = ArrayNode.getObjectNode(index);
                if(childObjectNode == null) {
                    childObjectNode = new SchemaObjectNode();
                    ArrayNode.set(index, childObjectNode);
                    if(pathItem.isArrayValue()) {
                        SchemaArrayNode childArrayNode = new SchemaArrayNode();
                        childObjectNode.put(pathItem.getName(), childArrayNode);
                        return childArrayNode;
                    }
                    SchemaObjectNode childAndChildObjectNode = new SchemaObjectNode();
                    childObjectNode.put(pathItem.getName(), childAndChildObjectNode);
                    return childAndChildObjectNode;
                } else  {
                    if(pathItem.isArrayValue()) {
                        SchemaArrayNode childChildArrayNode = childObjectNode.getArrayNode(pathItem.getName());
                        if (childChildArrayNode == null) {
                            childChildArrayNode = new SchemaArrayNode();
                            childObjectNode.put(pathItem.getName(), childChildArrayNode);
                        }
                        return childChildArrayNode;
                    } else {
                        SchemaObjectNode childAndChildObjectNode = childObjectNode.getObjectNode(pathItem.getName());
                        if (childAndChildObjectNode == null) {
                            childAndChildObjectNode = new SchemaObjectNode();
                            childObjectNode.put(pathItem.getName(), childAndChildObjectNode);
                        }
                        return childAndChildObjectNode;
                    }
                }
            }
            else if(pathItem.isArrayValue()) {
                SchemaArrayNode childArrayNode = ArrayNode.getArrayNode(index);
                if(childArrayNode == null) {
                    childArrayNode = new SchemaArrayNode();
                    ArrayNode.set(index, childArrayNode);
                }
                return childArrayNode;
            }

            // TODO 에러를 뿜어야함..
            //throw new RuntimeException("Invalid path");
            throw new IllegalArgumentException("Invalid path");
        } else {
            //TODO 에러를 뿜어야함..
            //throw new RuntimeException("Invalid path");
            throw new IllegalArgumentException("Invalid path");
        }
    }



    private void putNode(SchemaNode Node, PathItem pathItem, SchemaNode value) {
        if(pathItem.isInArray()) {
            if(pathItem.isObject()) {
                int index = pathItem.getIndex();
                SchemaObjectNode childObjectNode = ((SchemaArrayNode)Node).getObjectNode(index);
                if(childObjectNode == null) {
                    childObjectNode = new SchemaObjectNode();
                    ((SchemaArrayNode)Node).set(index, childObjectNode);
                }
                childObjectNode.put(pathItem.getName(), value);
            } else {
                ((SchemaArrayNode)Node).set(pathItem.getIndex(), value);
            }
        } else {
            ((SchemaObjectNode)Node).put(pathItem.getName(), value);
        }
    }



    public NodePath put(String path, SchemaNode value) {
        List<PathItem> list = PathItem.parseMultiPath2(path);

        SchemaElementNode rootNode = new SchemaObjectNode();
        SchemaElementNode schemaNode = rootNode;
        //noinspection ForLoopReplaceableByForEach
        for(int i = 0, n = list.size(); i < n; ++i) {
            PathItem pathItem = list.get(i);
            if(pathItem.isEndPoint()) {
                putNode(schemaNode, pathItem, value);
                break;
            }
            schemaNode = obtainOrCreateChild(schemaNode, pathItem);
        }
        this.node.merge(rootNode);
        return this;
    }


    public Object get(String path) {
        List<PathItem> pathItemList = PathItem.parseMultiPath2(path);
        Object parents = node;
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0, n = pathItemList.size(); i < n; ++i) {
            PathItem pathItem = pathItemList.get(i);
            if (pathItem.isEndPoint()) {
                if (pathItem.isInArray()) {
                    if(pathItem.isObject()) {
                        SchemaObjectNode endPointObject = ((SchemaArrayNode) parents).getObjectNode(pathItem.getIndex());
                        if(endPointObject == null) return null;
                        return endPointObject.get(pathItem.getName());
                    }
                    else {
                        return ((SchemaArrayNode)parents).get(pathItem.getIndex());
                    }
                } else {
                    return ((SchemaObjectNode) parents).get(pathItem.getName());
                }
            }
            else if((parents instanceof SchemaObjectNode && pathItem.isInArray()) || (parents instanceof SchemaArrayNode && !pathItem.isInArray())) {
                return null;
            }
            else {
                if (pathItem.isInArray()) {
                    assert parents instanceof SchemaArrayNode;
                    parents = ((SchemaArrayNode) parents).get(pathItem.getIndex());
                    if(pathItem.isObject() && parents instanceof SchemaObjectNode) {
                        parents = ((SchemaObjectNode) parents).get(pathItem.getName());
                    }
                } else {
                    assert parents instanceof SchemaObjectNode;
                    parents = ((SchemaObjectNode) parents).get(pathItem.getName());
                }
            }
        }
        return null;
    }

}
