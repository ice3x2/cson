package com.snoworca.cson.serializer;


import java.io.PrintWriter;
import java.lang.reflect.Field;

public class SchemaFieldNormal extends SchemaField {


    protected SchemaFieldNormal(TypeElement typeElement, Field field, String path) {
        super(typeElement, field, path);

        if(this.type == Types.Object && getField().getType().getAnnotation(CSON.class) == null)  {
            throw new CSONObjectException("Object type " + this.field.getType().getName() + " is not annotated with @CSON");
        }
    }


    public SchemaFieldNormal copy() {
        SchemaFieldNormal fieldRack = new SchemaFieldNormal(parentsTypeElement, field, path);
        fieldRack.setParentFiled(getParentField());
        return fieldRack;
    }



    @Override
    public SchemaNode copyNode() {
        SchemaFieldNormal fieldRack = copy();
        return fieldRack;
    }



}
