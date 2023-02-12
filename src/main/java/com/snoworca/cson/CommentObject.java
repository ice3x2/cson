package com.snoworca.cson;

public class CommentObject {
    public String commentBeforeKey = null;
    public String commentAfterKey = null;

    public String commentBeforeValue = null;
    public String commentAfterValue = null;

    public boolean isEmpty() {
        return commentBeforeKey == null && commentAfterKey == null && commentBeforeValue == null && commentAfterValue == null;
    }

    public boolean hasComment() {
        return commentBeforeKey != null || commentAfterKey != null || commentBeforeValue != null || commentAfterValue != null;
    }

    public String getKeyComment() {
        if(commentBeforeKey == null && commentAfterKey == null) {
            return null;
        } else if(commentBeforeKey == null) {
            return commentAfterKey;
        } else if(commentAfterKey == null) {
            return commentBeforeKey;
        } else {
            return commentBeforeKey + "\n\n" + commentAfterKey;
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
            return commentBeforeValue + "\n\n" + commentAfterValue;
        }
    }

    public void setAfterKey(String comment) {
        commentAfterKey = comment;
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
        return "Comment{" +
                "commentBeforeKey='" + commentBeforeKey + '\'' +
                ", commentAfterKey='" + commentAfterKey + '\'' +
                ", commentBeforeValue='" + commentBeforeValue + '\'' +
                ", commentAfterValue='" + commentAfterValue + '\'' +
                '}';
    }
}
