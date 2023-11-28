package com.snoworca.cson.serializer;

import java.lang.reflect.Field;

public class SchemaFieldMap extends SchemaField {
    SchemaFieldMap(TypeElement parentsTypeElement, Field field, String path) {
        super(parentsTypeElement, field, path);
    }

    @Override
    public SchemaNode copyNode() {
        return null;
    }
}
