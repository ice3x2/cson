package com.snoworca.cson.serializer;

public @interface CSONValueGetter {
    String value() default "";
    String key() default "";
    String comment() default "";
    String commentAfterKey() default "";
}
