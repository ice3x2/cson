package com.snoworca.cson;

public class CSONElement {
	
	public enum ElementType { Object, Array };
	
	private CSONElement mParents = null;
	private byte[] mVersion = CSONDataType.VER;
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
		return new StringBuilder().append(mVersion[0]).append(".").append(mVersion[1]).append(".").append(mVersion[2]).toString();
	}
	
	protected void setVersion(byte[] version) {
		mVersion = version;
	}
	
	
	

}
