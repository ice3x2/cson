package com.snoworca.cson;

public class KeyValueValueCommentObject extends ValueCommentObject {
    public String commentBeforeKey = null;
    public String commentAfterKey = null;



    @Override
    public boolean isCommented() {
        return commentBeforeKey != null || commentAfterKey != null || super.isCommented();
    }

    public String getKeyComment() {
        if(commentBeforeKey == null && commentAfterKey == null) {
            return null;
        } else if(commentBeforeKey == null) {
            return commentAfterKey;
        } else if(commentAfterKey == null) {
            return commentBeforeKey;
        } else {
            return commentBeforeKey + "\n" + commentAfterKey;
        }
    }

    public String getValueComment() {
        if(commentBeforeValue == null && commentAfterValue == null) {
            return null;
        } else if(commentBeforeValue == null) {
            return commentAfterValue;
        } else if(commentAfterValue == null) {
            return commentBeforeValue;
        } else {
            return commentBeforeValue + "\n" + commentAfterValue;
        }
    }



    public void setAfterKey(String comment) {
        commentAfterKey = comment;
    }

    public void setCommentAtKey(String comment) {
        setBeforeKey(comment);
    }

    public void setCommentAtValue(String comment) {
        setBeforeValue(comment);
    }

    public void addCommentAtKey(String comment) {
        if(commentBeforeKey == null) commentBeforeKey = "";
        commentBeforeKey = commentBeforeKey + comment;
    }

    public void addCommentAtValue(String comment) {
        if(commentBeforeValue == null) commentBeforeValue = "";
        commentBeforeValue = commentBeforeValue + comment;
    }

    public void setBeforeKey(String comment) {
        commentBeforeKey = comment;
    }

    public void setAfterValue(String comment) {
        commentAfterValue = comment;
    }

    public void setBeforeValue(String comment) {
        commentBeforeValue = comment;
    }


    public String getAfterKey() {
        return commentAfterKey;
    }

    public String getBeforeKey() {
        return commentBeforeKey;
    }


    public String getAfterValue() {
        return commentAfterValue;
    }

    public String getBeforeValue() {
        return commentBeforeValue;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if(commentBeforeKey != null) {
            sb.append(commentBeforeKey).append("\n");
        }
        if(commentAfterKey != null) {
            sb.append(commentAfterKey).append("\n");
        }
        if(commentBeforeValue != null) {
            sb.append(commentBeforeValue).append("\n");
        }
        if(commentAfterValue != null) {
            sb.append(commentAfterValue).append("\n");
        }

        return sb.toString();
    }
}
