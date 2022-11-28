package com.snoworca.cson;

public class CSONParseException  extends RuntimeException {
    public CSONParseException(String message) {
        super(message);
    }

    CSONParseException() {
        super();
    }

}
