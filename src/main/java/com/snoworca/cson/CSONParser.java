package com.snoworca.cson;

public class CSONParser {
	
	public final static CSONElement parse(byte[] buffer) {
		CSONParseIterator csonParseIterator = new CSONParseIterator(); 
		CSONBufferReader.parse(buffer,csonParseIterator);
		return csonParseIterator.release();
	}

	public final static CSONElement parse(byte[] buffer, int offset, int len) {
		CSONParseIterator csonParseIterator = new CSONParseIterator();
		CSONBufferReader.parse(buffer,offset,len, csonParseIterator);
		return csonParseIterator.release();
	}

}
