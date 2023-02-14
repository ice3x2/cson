package com.snoworca.cson;

public class ValueCommentObject {
    private String beforeComment;
    private String afterComment;

    public String getBeforeComment() {
        return beforeComment;
    }

    public void setBeforeComment(String beforeComment) {
        this.beforeComment = beforeComment;
    }

    public String getAfterComment() {
        return afterComment;
    }

    public void setAfterComment(String afterComment) {
        this.afterComment = afterComment;
    }

    public void addBeforeCommentAtValue(String comment) {
        if (beforeComment == null) {
            beforeComment = comment;
        } else {
            beforeComment += comment;
        }
    }

    public void addAfterCommentAtValue(String comment) {
        if (afterComment == null) {
            afterComment = comment;
        } else {
            afterComment += comment;
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


    public boolean isCommented() {
        return beforeComment != null || afterComment != null;
    }


}
