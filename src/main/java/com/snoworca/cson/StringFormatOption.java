package com.snoworca.cson;

public interface StringFormatOption {


    final static StringFormatOption PURE_JSON = () -> StringFormatType.PureJSON;
    StringFormatType getFormatType();


    public static StringFormatOption json() {
        return JSONOptions.json();
    }

    public static StringFormatOption json5() {
        return JSONOptions.json5();
    }

    public static StringFormatOption jsonPure() {
        return PURE_JSON;
    }




}
