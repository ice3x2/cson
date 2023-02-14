package com.snoworca.cson;

public class CommentObject {
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

    public void addBeforeComment(String comment) {
        if (beforeComment == null) {
            beforeComment = comment;
        } else {
            beforeComment += comment;
        }
    }

    public void addAfterComment(String comment) {
        if (afterComment == null) {
            afterComment = comment;
        } else {
            afterComment += comment;
        }
    }


    public String getComment() {
        if(beforeComment == null && afterComment == null) {
            return null;
        } else if(beforeComment == null) {
            return afterComment;
        } else if(afterComment == null) {
            return beforeComment;
        } else {
            return beforeComment + "\n" + afterComment;
        }
    }

    public String toString() {
        return getComment();
    }


    public boolean isCommented() {
        return beforeComment != null || afterComment != null;
    }


}
