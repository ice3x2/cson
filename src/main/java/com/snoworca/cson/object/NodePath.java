package com.snoworca.cson.object;


import com.snoworca.cson.PathItem;

import java.util.List;

public class NodePath {

    private final Node Node;

    public NodePath(Node Node) {
        this.Node = Node;
    }

    public Boolean optBoolean(String path) {
        return optBoolean(path, null);
    }

    public Boolean optBoolean(String path, Boolean defaultValue) {
        Object obj = get(path);
        if (obj instanceof Boolean) {
            return (Boolean) obj;
        } else if(obj instanceof Number) {
            return ((Number)obj).intValue() == 1;
        } else if(obj instanceof String) {
            return Boolean.parseBoolean((String) obj);
        }
        return defaultValue;
    }

    public Double optDouble(String path) {
        return optDouble(path, null);
    }

    public Double optDouble(String path, Double defaultValue) {
        Object obj = get(path);
        if (obj instanceof Number) {
            return ((Number)obj).doubleValue();
        }
        else if(obj instanceof Boolean) {
            return (Boolean)obj ? 1.0 : 0.0;
        }
        else if(obj instanceof String) {
            try {
                return Double.parseDouble((String) obj);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public Float optFloat(String path) {
        return optFloat(path, null);
    }

    public Float optFloat(String path, Float defaultValue) {
        Object obj = get(path);
        if (obj instanceof Number) {
            return ((Number)obj).floatValue();
        }
        else if(obj instanceof Boolean) {
            return (Boolean)obj ? 1.0f : 0.0f;
        }
        else if(obj instanceof String) {
            try {
                return Float.parseFloat((String) obj);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }


        return defaultValue;
    }

    public Long optLong(String path) {
        return optLong(path, null);
    }

    public Long optLong(String path, Long defaultValue) {
        Object obj = get(path);
        if (obj instanceof Number) {
            return ((Number)obj).longValue();
        }
        else if(obj instanceof Boolean) {
            return (Boolean)obj ? 1L : 0L;
        }
        else if(obj instanceof String) {
            try {
                return Long.parseLong((String) obj);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public Short optShort(String path) {
        return optShort(path, null);
    }

    public Short optShort(String path, Short defaultValue) {
        Object obj = get(path);
        if (obj instanceof Number) {
            return ((Number)obj).shortValue();
        }
        else if(obj instanceof Boolean) {
            return (Boolean)obj ? (short)1 : (short)0;
        }
        else if(obj instanceof String) {
            try {
                return Short.parseShort((String) obj);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public Byte optByte(String path) {
        return optByte(path, null);
    }

    public Byte optByte(String path, Byte defaultValue) {
        Object obj = get(path);
        if (obj instanceof Number) {
            return ((Number)obj).byteValue();
        }
        else if(obj instanceof Boolean) {
            return (Boolean)obj ? (byte)1 : (byte)0;
        }
        else if(obj instanceof String) {
            try {
                return Byte.parseByte((String) obj);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }


    public Integer optInteger(String path) {
        return optInteger(path, null);
    }

    public Integer optInteger(String path, Integer defaultValue) {
        Object obj = get(path);
        if (obj instanceof Number) {
            return ((Number)obj).intValue();
        }
        else if(obj instanceof Boolean) {
            return (Boolean)obj ? 1 : 0;
        }
        else if(obj instanceof String) {
            try {
                return Integer.parseInt((String) obj);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public String optString(String path, String defaultValue) {
        Object obj = get(path);
        if (obj instanceof String) {
            return (String) obj;
        } else if(obj instanceof Number) {
            return String.valueOf(obj);
        }
        return defaultValue;
    }

    public String optString(String path) {
        return optString(path, null);
    }

    public ObjectNode getObjectNodeNode(String path) {
        Object obj = get(path);
        if (obj instanceof ObjectNode) {
            return (ObjectNode) obj;
        }
        return null;
    }


    public ArrayNode getArrayNodeNode(String path) {
        Object obj = get(path);
        if (obj instanceof ArrayNode) {
            return (ArrayNode) obj;
        }
        return null;
    }


    private Node obtainOrCreateChild(Node Node, PathItem pathItem) {
        if(Node instanceof ObjectNode && !pathItem.isInArray() && pathItem.isObject()) {
            ObjectNode ObjectNode = (ObjectNode)Node;
            String name = pathItem.getName();
            if(pathItem.isArrayValue()) {
                ArrayNode childArrayNode = ObjectNode.getArrayNode(name);
                if(childArrayNode == null) {
                    childArrayNode = new ArrayNode();
                    ObjectNode.put(name, childArrayNode);
                }

                return childArrayNode;
            } else {
                ObjectNode childObjectNode = ObjectNode.getObjectNode(name);
                if(childObjectNode == null) {
                    childObjectNode = new ObjectNode();
                    ObjectNode.put(name, childObjectNode);
                }
                return childObjectNode;
            }
        } else if(Node instanceof ArrayNode && pathItem.isInArray()) {
            ArrayNode ArrayNode = (ArrayNode)Node;
            int index = pathItem.getIndex();
            if(pathItem.isObject()) {
                ObjectNode childObjectNode = ArrayNode.getObjectNode(index);
                if(childObjectNode == null) {
                    childObjectNode = new ObjectNode();
                    ArrayNode.set(index, childObjectNode);
                    if(pathItem.isArrayValue()) {
                        ArrayNode childArrayNode = new ArrayNode();
                        childObjectNode.put(pathItem.getName(), childArrayNode);
                        return childArrayNode;
                    }
                    ObjectNode childAndChildObjectNode = new ObjectNode();
                    childObjectNode.put(pathItem.getName(), childAndChildObjectNode);
                    return childAndChildObjectNode;
                } else  {
                    if(pathItem.isArrayValue()) {
                        ArrayNode childChildArrayNode = childObjectNode.getArrayNode(pathItem.getName());
                        if (childChildArrayNode == null) {
                            childChildArrayNode = new ArrayNode();
                            childObjectNode.put(pathItem.getName(), childChildArrayNode);
                        }
                        return childChildArrayNode;
                    } else {
                        ObjectNode childAndChildObjectNode = childObjectNode.getObjectNode(pathItem.getName());
                        if (childAndChildObjectNode == null) {
                            childAndChildObjectNode = new ObjectNode();
                            childObjectNode.put(pathItem.getName(), childAndChildObjectNode);
                        }
                        return childAndChildObjectNode;
                    }
                }
            }
            else if(pathItem.isArrayValue()) {
                ArrayNode childArrayNode = ArrayNode.getArrayNode(index);
                if(childArrayNode == null) {
                    childArrayNode = new ArrayNode();
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



    private void putNode(Node Node, PathItem pathItem, Node value) {
        if(pathItem.isInArray()) {
            if(pathItem.isObject()) {
                int index = pathItem.getIndex();
                ObjectNode childObjectNode = ((ArrayNode)Node).getObjectNode(index);
                if(childObjectNode == null) {
                    childObjectNode = new ObjectNode();
                    ((ArrayNode)Node).set(index, childObjectNode);
                }
                childObjectNode.put(pathItem.getName(), value);
            } else {
                ((ArrayNode)Node).set(pathItem.getIndex(), value);
            }
        } else {
            ((ObjectNode)Node).put(pathItem.getName(), value);
        }
    }



    public NodePath put(String path, Node value) {
        List<PathItem> list = PathItem.parseMultiPath2(path);
        Node lastNode = this.Node;
        //noinspection ForLoopReplaceableByForEach
        for(int i = 0, n = list.size(); i < n; ++i) {
            PathItem pathItem = list.get(i);
            if(pathItem.isEndPoint()) {
                putNode(lastNode, pathItem, value);
                break;
            }
            lastNode = obtainOrCreateChild(lastNode, pathItem);
        }
        return this;
    }


    public Object get(String path) {
        List<PathItem> pathItemList = PathItem.parseMultiPath2(path);
        Object parents = Node;
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0, n = pathItemList.size(); i < n; ++i) {
            PathItem pathItem = pathItemList.get(i);

            if (pathItem.isEndPoint()) {
                if (pathItem.isInArray()) {
                    if(pathItem.isObject()) {
                        ObjectNode endPointObject = ((ArrayNode) parents).getObjectNode(pathItem.getIndex());
                        if(endPointObject == null) return null;
                        return endPointObject.get(pathItem.getName());
                    }
                    else {
                        return ((ArrayNode)parents).get(pathItem.getIndex());
                    }
                } else {
                    return ((ObjectNode) parents).get(pathItem.getName());
                }
            }
            else if((parents instanceof ObjectNode && pathItem.isInArray()) || (parents instanceof ArrayNode && !pathItem.isInArray())) {
                return null;
            }
            else {
                if (pathItem.isInArray()) {
                    assert parents instanceof ArrayNode;
                    parents = ((ArrayNode) parents).get(pathItem.getIndex());
                    if(pathItem.isObject() && parents instanceof ObjectNode) {
                        parents = ((ObjectNode) parents).get(pathItem.getName());
                    }
                } else {
                    assert parents instanceof ObjectNode;
                    parents = ((ObjectNode) parents).get(pathItem.getName());
                }
            }
        }
        return null;
    }

}
