package com.snoworca.cson;

import java.io.Reader;
import java.io.StringReader;

class JSONParser {

    public static void parse(Reader json, CSONObject csonObject) {
        JSONTokener jsonTokener = new JSONTokener(json);


    }

    static void parseObject(JSONTokener x, CSONObject csonObject) {
        char c;
        String key = null;

        //StringBuilder valueCommentBuilder = new StringBuilder();
        CommentObject lastCommentObject = new CommentObject();
        StringBuilder commentBuilder = new StringBuilder();

        char nextClean = readComment(x, commentBuilder);
        if(commentBuilder.length() > 0) {
            csonObject.getHeadCommentObject().setBeforeKey(commentBuilder.toString().trim());
        }
        if (nextClean != '{') {
            throw x.syntaxError("A JSONObject text must begin with '{'");
        }
        for (;;) {
            char prev = x.getPrevious();
            c = readComment(x, commentBuilder);
            if(commentBuilder.length() > 0) {
                lastCommentObject.setBeforeKey(commentBuilder.toString().trim());
            }

            switch (c) {
                case 0:
                    throw x.syntaxError("A JSONObject text must end with '}'");
                case '}':
                    if(lastCommentObject.getBeforeKey() != null) {
                        csonObject.getTailCommentObject().setBeforeKey(lastCommentObject.getBeforeKey());
                        lastCommentObject.setBeforeKey(null);
                    }
                    readComment(x, commentBuilder);
                    if(commentBuilder.length() > 0) {
                        this.tailCommentObject.setAfterKey(commentBuilder.toString().trim());
                    }
                    return;
                case '{':
                case '[':
                    if(prev=='{') {
                        throw x.syntaxError("A JSON Object can not directly nest another JSON Object or JSON Array.");
                    }
                    // fall through
                    readComment(x, commentBuilder);
                    if(commentBuilder.length() > 0) {
                        lastCommentObject.setBeforeKey(commentBuilder.toString().trim());
                    }
                default:
                    x.back();
                    key = x.nextValue().toString();
            }

            c = readComment(x, commentBuilder);
            if(commentBuilder.length() > 0) {
                lastCommentObject.setAfterKey(commentBuilder.toString().trim());
            }

            // The key is followed by ':'.
            //c = x.nextClean();
            if (c != ':') {
                throw x.syntaxError("Expected a ':' after a key");
            }

            char next;
            // Use syntaxError(..) to include error location
            if (key != null) {
                // Check if key exists
                if (this.opt(key) != null) {
                    // key already exists
                    throw x.syntaxError("Duplicate key \"" + key + "\"");
                }

                readComment(x, commentBuilder);
                if(commentBuilder.length() > 0) {
                    lastCommentObject.setBeforeValue(commentBuilder.toString().trim());
                }
                x.back();
                Object value = x.nextValue();
                this.putAtJSONParsing(key, value);
                if(value instanceof CSONObject) {
                    CSONObject objectValue = (CSONObject)value;
                    String commentAfterKey = objectValue.getTailCommentObject()git .getAfterKey();
                    if(commentAfterKey != null) {
                        lastCommentObject.setAfterValue(commentAfterKey);
                    }
                    String commentHead = lastCommentObject.getBeforeValue();
                    if(commentHead != null) {
                        objectValue.getHeadCommentObject().setBeforeKey(commentHead);
                    }
                    if(lastCommentObject.hasComment()) {
                        putCommentObject(key, lastCommentObject);
                        lastCommentObject = new CommentObject();
                    }

                    continue;
                }


                next = readComment(x, commentBuilder);
                if(commentBuilder.length() > 0) {
                    lastCommentObject.setAfterValue(commentBuilder.toString().trim());
                }


                if(lastCommentObject.hasComment()) {
                    putCommentObject(key, lastCommentObject);
                    lastCommentObject = new CommentObject();
                }
            } else {
                next = readComment(x, commentBuilder);
                if(commentBuilder.length() > 0) {
                    lastCommentObject.setAfterValue(commentBuilder.toString().trim());
                }
            }


            switch (next) {
                case ';':
                case ',':
                    readComment(x, commentBuilder);
                    if(commentBuilder.length() > 0) {
                        lastCommentObject.setBeforeKey(commentBuilder.toString().trim());
                    }
                    x.back();
                    break;
                case '}':
                    readComment(x, commentBuilder);
                    if(commentBuilder.length() > 0) {
                        tailCommentObject.setAfterKey(commentBuilder.toString().trim());
                    }
                    x.back();
                    return;
                default:
                    throw x.syntaxError("Expected a ',' or '}'");
            }
        }
    }

}
