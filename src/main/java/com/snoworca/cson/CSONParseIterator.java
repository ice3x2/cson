package com.snoworca.cson;


class CSONParseIterator implements CSONBufferReader.ParseCallback {
	String mSelectKey = null;
	CSONElement mCurrentElement;
	CSONElement mRoot;
	byte[] mVersion;
	
	public CSONElement release() {
		CSONElement result = mRoot;
		mSelectKey = null;
		mCurrentElement = null;
		mRoot = null;
		return result;
	}
	
	
	
	@Override
	public void onVersion(byte[] version) {
		this.mVersion = version;
	}
	
	@Override
	public void onValue(Object value) {	
		if(value == null) value = new NullValue();
		if(mCurrentElement.getType() == CSONElement.ElementType.Object) {
			((CSONObject)mCurrentElement).put(mSelectKey, value);
			mSelectKey = null;
		} else {
			((CSONArray)mCurrentElement).push(value);
		}
	}
	
	@Override
	public void onOpenObject() {
		CSONObject obj = new CSONObject();
		obj.setVersion(this.mVersion);
		if(mCurrentElement == null) 
		{
			mCurrentElement = obj;
			return;
		}				
		else if(mCurrentElement.getType() == CSONElement.ElementType.Object) {
			((CSONObject)mCurrentElement).put(mSelectKey, obj);
			mSelectKey = null;
		} else {
			((CSONArray)mCurrentElement).add(obj);
		}
		obj.setParents(mCurrentElement);
		mCurrentElement = obj;
	}
	
	@Override
	public void onOpenArray() {
		CSONArray obj = new CSONArray();
		obj.setVersion(this.mVersion);
		if(mCurrentElement == null) 
		{
			mCurrentElement = obj;
			return;
		}		
		else if(mCurrentElement.getType() == CSONElement.ElementType.Object) {
			((CSONObject)mCurrentElement).put(mSelectKey, obj);
			mSelectKey = null;
		} else {
			((CSONArray)mCurrentElement).add(obj);
		}
		obj.setParents(mCurrentElement);
		mCurrentElement = obj;
		
		
	}
	
	@Override
	public void onKey(String key) {
		mSelectKey = key;
	}
	
	@Override
	public void onCloseObject() {
		onCloseCSONElement();
	}
	
	@Override
	public void onCloseArray() {
		onCloseCSONElement();
	}
	
	private void onCloseCSONElement() {
		CSONElement parents =  mCurrentElement.getParents();
		if(parents ==null) {
			mRoot = mCurrentElement;
			return;
		}
		mCurrentElement = parents;
	}

	
}
