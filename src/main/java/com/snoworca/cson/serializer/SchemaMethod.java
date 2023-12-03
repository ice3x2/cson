package com.snoworca.cson.serializer;


import java.lang.reflect.Method;

class SchemaMethod implements SchemaNode {

    static enum Type {
        GETTER, SETTER
    }

    private final Type type;
    private final String key;

    private final String comment;
    private final String afterComment;

    private final Class<?> returnType;
    private final Class<?> parameterType;


    SchemaMethod(Method method) {
        CSONValueGetter getter = method.getAnnotation(CSONValueGetter.class);
        CSONValueSetter setter = method.getAnnotation(CSONValueSetter.class);
        if(getter != null) {
            this.type = Type.GETTER;
            this.key = getter.value();
            this.comment = getter.comment();
            this.afterComment = getter.commentAfterKey();
            this.returnType = method.getReturnType();
            this.parameterType = null;
             Types.of(this.returnType);


        } else if(setter != null) {
            this.type = Type.SETTER;
            this.key = setter.value();
            this.comment = null;
            this.afterComment = null;
            this.returnType = null;
            this.parameterType = method.getParameterTypes()[0];
        }


    }










    @Override
    public SchemaNode copyNode() {
        return null;
    }
}
