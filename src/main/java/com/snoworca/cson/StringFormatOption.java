package com.snoworca.cson;

public interface StringFormatOption {

    StringFormatType getFormatType();

    public static StringFormatOption json() {
        return JSONOptions.json();
    }

    public static StringFormatOption json5() {
        return JSONOptions.json5();
    }


}
