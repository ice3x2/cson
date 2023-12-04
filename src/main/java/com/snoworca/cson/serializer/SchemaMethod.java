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


    private final MethodType methodType;
    private final Method method;

    private final String comment;
    private final String afterComment;

    SchemaMethod(TypeElement parentsTypeElement, Method method) {
        super(parentsTypeElement,getPath(method),  getValueType(method));
        this.method = method;
        this.method.setAccessible(true);
        this.methodType = getMethodType(method);
        assertValueType(getValueType(), method.getDeclaringClass().getName() + "." + method.getName());
        if(this.methodType == MethodType.Getter) {
            CSONValueGetter csonValueGetter = method.getAnnotation(CSONValueGetter.class);
            String comment = csonValueGetter.comment();
            String afterComment = csonValueGetter.commentAfterKey();
            this.comment = comment.isEmpty() ? null : comment;
            this.afterComment = afterComment.isEmpty() ? null : afterComment;
        } else {
            this.comment = null;
            this.afterComment = null;
        }
    }

    public MethodType getMethodType() {
        return methodType;
    }

    @Override
    public SchemaNode copyNode() {
        return new SchemaMethod(parentsTypeElement, method);
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
    Object getValue(Object parent) {
        try {
            return method.invoke(parent);
        } catch (Exception e) {
            throw new CSONSerializerException("Failed to invoke method " + method.getDeclaringClass().getName() + "." + method.getName(), e);
        }
    }

    @Override
    Object setValue(Object parent, Object value) {
        try {
            return method.invoke(parent, value);
        } catch (Exception e) {
            throw new CSONSerializerException("Failed to invoke method " + method.getDeclaringClass().getName() + "." + method.getName(), e);
        }
    }


}
