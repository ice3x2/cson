package com.snoworca.cson;

public class CSONIndexNotFoundException extends RuntimeException {
	CSONIndexNotFoundException() {
		super();
	}
	
	CSONIndexNotFoundException(Exception cause) {
		super(cause);
	}
}
