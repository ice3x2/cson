package com.snoworca.cson.object;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CSONValue {


    String value() default "";

    String key() default "";
    boolean byteArrayToCSONArray() default false;

}
