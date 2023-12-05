package com.snoworca.cson.serializer;


import java.lang.reflect.Method;


class SchemaMethod extends SchemaValue {



    private static Class<?> getValueType(Method method) {
        CSONValueGetter csonValueGetter = method.getAnnotation(CSONValueGetter.class);
        CSONValueSetter csonValueSetter = method.getAnnotation(CSONValueSetter.class);
        if(csonValueSetter != null) {
            Class<?>[] types =  method.getParameterTypes();
            if(types.length != 1) {
                throw new CSONSerializerException("Setter method " + method.getDeclaringClass().getName() + "." + method.getName() + " must have only one parameter");
            }
            return types[0];
        }
        else if(csonValueGetter != null) {
            Class<?> returnType = method.getReturnType();
            if(returnType == void.class || returnType == Void.class || returnType == null) {
                throw new CSONSerializerException("Getter method " + method.getDeclaringClass().getName() + "." + method.getName() + " must have return type");
            }
            return returnType;
        }
        else {
            throw new CSONSerializerException("Method " + method.getDeclaringClass().getName() + "." + method.getName() + " must be annotated with @CSONValueGetter or @CSONValueSetter");
        }
    }


    private static String getPath(Method method) {
        CSONValueGetter csonValueGetter = method.getAnnotation(CSONValueGetter.class);
        CSONValueSetter csonValueSetter = method.getAnnotation(CSONValueSetter.class);
        if(csonValueSetter != null) {
            String path = csonValueSetter.value();
            if(path.isEmpty()) {
                path = csonValueSetter.key();
            }
            if(path.isEmpty()) {
                path = setterNameFilter(method.getName());
            }
            return path;
        }
        else if(csonValueGetter != null) {
            String path = csonValueGetter.value();
            if(path.isEmpty()) {
                path = csonValueGetter.key();
            }
            if(path.isEmpty()) {
                path = getterNameFilter(method.getName());
            }
            return path;
        }
        else {
            throw new CSONSerializerException("Method " + method.getDeclaringClass().getName() + "." + method.getName() + " must be annotated with @CSONValueGetter or @CSONValueSetter");
        }
    }

    private static String setterNameFilter(String methodName) {
        if(methodName.length() > 3 && (methodName.startsWith("set") || methodName.startsWith("Set") || methodName.startsWith("SET") ||
                methodName.startsWith("put") || methodName.startsWith("Put") || methodName.startsWith("PUT") ||
                methodName.startsWith("add") || methodName.startsWith("Add") || methodName.startsWith("ADD"))) {
            String name = methodName.substring(3);
            name = name.substring(0,1).toLowerCase() + name.substring(1);
            return name;
        }
        else {
            return methodName;
        }
    }

    private static String getterNameFilter(String methodName) {
        if(methodName.length() > 3 && (methodName.startsWith("get") || methodName.startsWith("Get") || methodName.startsWith("GET"))) {
            String name =  methodName.substring(3);
            name = name.substring(0,1).toLowerCase() + name.substring(1);
            return name;
        }
        else if(methodName.length() > 3 && (methodName.startsWith("is") || methodName.startsWith("Is") || methodName.startsWith("IS"))) {
            String name = methodName.substring(2);
            name = name.substring(0,1).toLowerCase() + name.substring(1);
            return name;
        }
        else {
            return methodName;
        }
    }

    static enum MethodType {
        Getter,
        Setter,
        Both


    }

    private static MethodType getMethodType(Method method) {
        CSONValueGetter csonValueGetter = method.getAnnotation(CSONValueGetter.class);
        CSONValueSetter csonValueSetter = method.getAnnotation(CSONValueSetter.class);
        if(csonValueSetter != null) {
            return MethodType.Setter;
        }
        else if(csonValueGetter != null) {
            return MethodType.Getter;
        }
        else {
            throw new CSONSerializerException("Method " + method.getDeclaringClass().getName() + "." + method.getName() + " must be annotated with @CSONValueGetter or @CSONValueSetter");
        }
    }


    private MethodType methodType = null;
    private Method methodSetter;
    private Method methodGetter;

    private String comment = null;
    private String afterComment = null;

    SchemaMethod(TypeElement parentsTypeElement, Method method) {
        super(parentsTypeElement,getPath(method),  getValueType(method));
        method.setAccessible(true);
        MethodType methodType = getMethodType(method);
        assertValueType(getValueTypeClass(), method.getDeclaringClass().getName() + "." + method.getName());
        if(methodType == MethodType.Getter) {
            setGetter(method);
        }
        else if(methodType == MethodType.Setter) {
            setSetter(method);
        }
    }

    private void setGetter(Method method) {
        CSONValueGetter csonValueGetter = method.getAnnotation(CSONValueGetter.class);
        String comment = csonValueGetter.comment();
        String afterComment = csonValueGetter.commentAfterKey();
        this.comment = comment.isEmpty() ? null : comment;
        this.afterComment = afterComment.isEmpty() ? null : afterComment;
        methodGetter = method;
        if(methodType == MethodType.Setter) {
            this.methodType = MethodType.Both;
        } else {
            this.methodType = MethodType.Getter;
        }
    }

    private void setSetter(Method method) {
        methodSetter = method;
        if(methodType == MethodType.Getter) {
            this.methodType = MethodType.Both;
        } else {
            this.methodType = MethodType.Setter;
        }

    }


    @Override
    boolean appendDuplicatedSchemaValue(SchemaValue node) {
        if(this.methodType != MethodType.Both &&
                node instanceof SchemaMethod && this.parentsTypeElement == node.parentsTypeElement && this.valueTypeClass == node.valueTypeClass) {
            SchemaMethod schemaMethod = (SchemaMethod) node;
            if(schemaMethod.methodType == MethodType.Getter && this.methodType == MethodType.Setter) {
                setGetter(schemaMethod.methodGetter);
                return true;
            } else if(schemaMethod.methodType == MethodType.Setter && this.methodType == MethodType.Getter) {
                setSetter(schemaMethod.methodSetter);
                return true;
            }
        }

        return super.appendDuplicatedSchemaValue(node);
    }



    public MethodType getMethodType() {
        return methodType;
    }

    @Override
    public SchemaNode copyNode() {
        return new SchemaMethod(parentsTypeElement, methodSetter);
    }

    @Override
    String getComment() {
        return comment;
    }

    @Override
    String getAfterComment() {
        return afterComment;
    }

    @Override
    Object onGetValue(Object parent) {
        if(methodGetter == null) return null;
        try {
            return methodGetter.invoke(parent);
        } catch (Exception e) {
            throw new CSONSerializerException("Failed to invoke method " + methodSetter.getDeclaringClass().getName() + "." + methodSetter.getName(), e);
        }
    }

    @Override
    void onSetValue(Object parent, Object value) {
        if(methodSetter == null) return;
        try {
            methodSetter.invoke(parent, value);
        } catch (Exception e) {
            throw new CSONSerializerException("Failed to invoke method " + methodSetter.getDeclaringClass().getName() + "." + methodSetter.getName(), e);
        }
    }



    static boolean isSchemaMethodGetter(SchemaNode schemaValue) {
        return schemaValue instanceof SchemaMethod && (((SchemaMethod)schemaValue).getMethodType() == SchemaMethod.MethodType.Getter  || ((SchemaMethod)schemaValue).getMethodType() == SchemaMethod.MethodType.Both);
    }



}
