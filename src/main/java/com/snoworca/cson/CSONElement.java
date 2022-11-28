package com.snoworca.cson;

import java.nio.ByteBuffer;

public class CSONElement {
	
	public enum ElementType { Object, Array };
	
	private CSONElement mParents = null;
	private byte[] versionRaw = CSONDataType.VER_RAW;
	private ElementType mType = ElementType.Object;
	
	protected void setParents(CSONElement parents) {
		mParents = parents;
	}
	
	CSONElement(ElementType type) {
		this.mType = type;
	}
	
	public CSONElement getParents() {
		return mParents;
	}
	
	public ElementType getType() {
		return mType;
	}
	
	public String getVersion() {
		return Short.toString(ByteBuffer.wrap(versionRaw).getShort());
	}
	
	protected void setVersion(byte[] versionRaw) {
		this.versionRaw = versionRaw;
	}
	
	
	

}
