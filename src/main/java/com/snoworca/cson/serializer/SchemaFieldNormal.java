package com.snoworca.cson.serializer;


import java.lang.reflect.Field;

public class SchemaFieldNormal extends SchemaField {

    private final boolean isPrimitive;




    protected SchemaFieldNormal(TypeElement typeElement, Field field, String path) {
        super(typeElement, field, path);
        this.isPrimitive =  this.getFieldType().isPrimitive();
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
