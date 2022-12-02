package com.snoworca.cson;

import java.nio.ByteBuffer;
import java.util.regex.Pattern;

public class CSONElement {

	private final static Pattern BASE64_PREFIX_REPLACE_PATTERN = Pattern.compile("(?i)^base64,");
	private final static Pattern BASE64_PREFIX_PATTERN = Pattern.compile("^((?i)base64,)([a-zA-Z0-9+/]*={0,2})$");


	public enum ElementType { Object, Array;};

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



	public static CSONElement clone(CSONElement element) {
		if(element == null) return null;
		if(element instanceof CSONObject) {
			return ((CSONObject)element).clone();
		}
		return ((CSONArray)element).clone();
	}


	public static boolean isBase64String(String value) {
		return BASE64_PREFIX_PATTERN.matcher(value).matches();
	}

	public static byte[] base64StringToByteArray(String value) {
		value = BASE64_PREFIX_REPLACE_PATTERN.matcher(value).replaceAll("");
		return Base64.decode(value);
	}
	

}
