package com.snoworca.cson.serializer;

public interface SchemaValue extends SchemaNode {

    Object getValue(Object parent);

    void setValue(Object parent, Object value);

    String getComment();
    String getAfterComment();

}
