package com.snoworca.cson;

import com.snoworca.cson.path.CSONPath;

import java.nio.ByteBuffer;
import java.util.regex.Pattern;

public abstract  class CSONElement {

	protected static final String HEAD_COMMENT_KEY  = "";
	protected static final String TAIL_COMMENT_KEY  = " ";

	private final static Pattern BASE64_PREFIX_REPLACE_PATTERN = Pattern.compile("(?i)^base64,");
	private final static Pattern BASE64_PREFIX_PATTERN = Pattern.compile("^((?i)base64,)([a-zA-Z0-9+/]*={0,2})$");
	private CommentObject tailCommentObject = null;
	private CommentObject headCommentObject = null;
	private CSONPath csonPath = null;

	public void setHeadComment(String comment) {
		if(comment== null) {
			return;
		}
		if(headCommentObject == null) {
			headCommentObject = new CommentObject();
		}
		headCommentObject.setBeforeComment(comment);
	}

	public void setTailComment(String comment) {
		if(comment== null) {
			return;
		}
		if(tailCommentObject == null) {
			tailCommentObject = new CommentObject();
		}
		tailCommentObject.setAfterComment(comment);

	}


	public CommentObject getHeadCommentObject() {
		return headCommentObject;
	}

	public CommentObject getTailCommentObject() {
		return tailCommentObject;
	}

	public CommentObject getOrCreateHeadCommentObject() {
		return headCommentObject == null ? headCommentObject = new CommentObject() : headCommentObject;
	}

	public CommentObject getOrCreateTailCommentObject() {
		return tailCommentObject == null ? tailCommentObject = new CommentObject() : tailCommentObject;
	}

	protected void setHeadCommentObject(CommentObject commentObject) {
		this.headCommentObject = commentObject;
	}

	protected void setTailCommentObject(CommentObject commentObject) {
		this.tailCommentObject = commentObject;
	}

	public String getTailComment() {
		return tailCommentObject == null ? null : tailCommentObject.getComment();
	}

	public String getHeadComment() {
		return headCommentObject == null ? null : headCommentObject.getComment();
	}


	public final CSONPath getCsonPath() {
		if(csonPath == null) {
			csonPath = new CSONPath(this);
		}
		return csonPath;
	}



	protected abstract void write(JSONWriter writer);




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
