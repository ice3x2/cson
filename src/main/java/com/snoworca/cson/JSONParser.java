package com.snoworca.cson;

import java.io.Reader;

class JSONParser {

    public static void parse(Reader json, CSONObject csonObject) {
        JSONTokener jsonTokener = new JSONTokener(json);
    }



    private static char nextComment(JSONTokener x, StringBuilder commentBuilder) throws CSONException {
        x.back();
        char next = x.next();
        boolean isMultiLine = false;
        while(next == '/') {
            char nextC = x.next();
            if(nextC == '/') {
                String strComment = x.nextTo('\n');
                if(isMultiLine) commentBuilder.append("\n");
                commentBuilder.append(strComment);
                isMultiLine = true;
                next = x.nextClean();
            } else if(nextC == '*') {
                String strComment = x.nextToFromString("*/");
                if(isMultiLine) commentBuilder.append("\n");
                commentBuilder.append(strComment);
                isMultiLine = true;
                next = x.nextClean();
            } else  {
                next = nextC;
            }
        }
        return next;
    }



    private static char readComment(JSONTokener x, StringBuilder commentBuilder) {
        commentBuilder.setLength(0);
        char nextClean = x.nextClean();
        if(nextClean  == '/') {
            nextClean = nextComment(x, commentBuilder);
            return nextClean;
        }
        return nextClean;
    }

    private static void putAtJSONParsing(CSONObject csonObject, String key, Object value) {
        if(value instanceof String && CSONElement.isBase64String((String)value)) {
            value = CSONElement.base64StringToByteArray((String)value);
        }
        csonObject.put(key, value);
    }

    static void parseArray(JSONTokener x, CSONArray csonArray) {


        StringBuilder commentBuilder = new StringBuilder();
        CommentObject lastCommentObject = new CommentObject();

        char nextChar = readComment(x, commentBuilder);
        if(commentBuilder.length() > 0) {
            csonArray.setHeadComment(commentBuilder.toString().trim());
        }

        if (nextChar != '[') {
            throw x.syntaxError("A JSONArray text must start with '['");
        }

        if (nextChar != ']') {

            if (nextChar == 0) {
                throw x.syntaxError("Expected a ',' or ']'");
            }

            for (;;) {
                nextChar = readComment(x, commentBuilder);
                if(commentBuilder.length() > 0) {
                    lastCommentObject.setBeforeComment(commentBuilder.toString().trim());
                }
                if (nextChar == 0) {
                    throw x.syntaxError("Expected a ',' or ']'");
                }

                if(nextChar == ']') {
                    return;
                }


                x.back();
                if (nextChar == ',') {
                    csonArray.addAtJSONParsing(null);
                    nextChar = x.nextClean();
                } else {
                    Object value = x.nextValue();
                    if(value instanceof CSONElement) {
                        CSONElement valueObject = (CSONElement) value;
                        valueObject.setHeadComment(lastCommentObject.getBeforeComment());
                        CommentObject commentObject = valueObject.getTailCommentObject();
                        if(commentObject != null && commentObject.isCommented()) {
                            lastCommentObject.setAfterComment(valueObject.getTailCommentObject().getAfterComment());
                        }
                        nextChar = x.nextClean();
                    }
                    else {
                        nextChar = readComment(x, commentBuilder);
                        if(commentBuilder.length() > 0) {
                            lastCommentObject.setAfterComment(commentBuilder.toString().trim());
                        }
                    }
                    csonArray.addAtJSONParsing(value);
                }
                if(lastCommentObject.isCommented()) {
                    csonArray.addCommentObjects(lastCommentObject);
                    lastCommentObject = new CommentObject();
                } else {
                    csonArray.addCommentObjects(null);
                }

                switch (nextChar) {
                    case 0:
                        // array is unclosed. No ']' found, instead EOF
                        throw x.syntaxError("Expected a ',' or ']'");
                    case ',':
                        nextChar = readComment(x, commentBuilder);
                        if(commentBuilder.length() > 0) {
                            csonArray.getOrCreateHeadCommentObject().setBeforeComment(commentBuilder.toString().trim());
                        }
                        if (nextChar == 0) {
                            // array is unclosed. No ']' found, instead EOF
                            throw x.syntaxError("Expected a ',' or ']'");
                        }
                        if (nextChar == ']') {
                            readComment(x, commentBuilder);
                            if(commentBuilder.length() > 0) {
                                csonArray.getOrCreateHeadCommentObject().setAfterComment(commentBuilder.toString().trim());
                            }
                            return;
                        }
                        x.back();
                        break;
                    case ']':
                        readComment(x, commentBuilder);
                        if(commentBuilder.length() > 0) {
                            csonArray.getOrCreateHeadCommentObject().setAfterComment(commentBuilder.toString().trim());
                        }
                        return;
                    default:
                        throw x.syntaxError("Expected a ',' or ']'");
                }
            }
        }
    }

    static void parseObject(JSONTokener x, CSONObject csonObject) {
        char c;
        String key = null;

        CommentObject valueCommentObject = new CommentObject();
        CommentObject lastKeyObject = new CommentObject();
        StringBuilder commentBuilder = new StringBuilder();

        char nextClean = readComment(x, commentBuilder);
        if(commentBuilder.length() > 0) {
            csonObject.setHeadComment(commentBuilder.toString().trim());
        }
        if (nextClean != '{') {
            throw x.syntaxError("A JSONObject text must begin with '{'");
        }
        for (;;) {
            char prev = x.getPrevious();
            c = readComment(x, commentBuilder);
            if(commentBuilder.length() > 0) {
                lastKeyObject.setBeforeComment(commentBuilder.toString().trim());
            }

            switch (c) {
                case 0:
                    throw x.syntaxError("A JSONObject text must end with '}'");
                case '}':
                    if(lastKeyObject.getBeforeComment() != null) {
                        csonObject.getOrCreateTailCommentObject().setBeforeComment(lastKeyObject.getBeforeComment());
                    }
                    readComment(x, commentBuilder);
                    if(commentBuilder.length() > 0) {
                        csonObject.getOrCreateTailCommentObject().setAfterComment(commentBuilder.toString().trim());
                    }
                    x.back();
                    return;
                case '{':
                case '[':
                    if(prev=='{') {
                        throw x.syntaxError("A JSON Object can not directly nest another JSON Object or JSON Array.");
                    }
                    // fall through
                    readComment(x, commentBuilder);
                    if(commentBuilder.length() > 0) {
                        lastKeyObject.setBeforeComment(commentBuilder.toString().trim());
                    }
                default:
                    x.back();
                    key = x.nextValue().toString();
            }

            c = readComment(x, commentBuilder);
            if(commentBuilder.length() > 0) {
                lastKeyObject.setAfterComment(commentBuilder.toString().trim());
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
                if (csonObject.opt(key) != null) {
                    // key already exists
                    throw x.syntaxError("Duplicate key \"" + key + "\"");
                }


                readComment(x, commentBuilder);
                if(commentBuilder.length() > 0) {
                    valueCommentObject.setBeforeComment(commentBuilder.toString().trim());
                }
                x.back();
                Object value = x.nextValue();
                putAtJSONParsing(csonObject, key, value);
                if(value instanceof CSONElement) {
                    CSONElement objectValue = (CSONElement)value;
                    CommentObject tailCommentObject = objectValue.getTailCommentObject();
                    if(tailCommentObject != null) {
                        String valueAfterComment = tailCommentObject.getAfterComment();
                        if(valueAfterComment != null) {
                            valueCommentObject.setAfterComment(valueAfterComment);
                        }
                    }
                    String commentHead = valueCommentObject.getBeforeComment();
                    if(commentHead != null) {
                        objectValue.setHeadComment(commentHead);
                    }
                }


                next = readComment(x, commentBuilder);
                if(commentBuilder.length() > 0) {
                    valueCommentObject.setAfterComment(commentBuilder.toString().trim());
                }

                if(lastKeyObject.isCommented() || valueCommentObject.isCommented()) {
                    csonObject.setCommentObjects(key, !lastKeyObject.isCommented() ? null :
                            lastKeyObject, !valueCommentObject.isCommented() ? null : valueCommentObject);
                    lastKeyObject = new CommentObject();
                    valueCommentObject = new CommentObject();
                }

            } else {
                next = readComment(x, commentBuilder);
                if(commentBuilder.length() > 0) {
                    lastKeyObject.setAfterComment(commentBuilder.toString().trim());
                }
            }


            switch (next) {
                case ';':
                case ',':
                    readComment(x, commentBuilder);
                    if(commentBuilder.length() > 0) {
                        lastKeyObject.setBeforeComment(commentBuilder.toString().trim());
                    }
                    x.back();
                    break;
                case '}':
                    readComment(x, commentBuilder);
                    if(commentBuilder.length() > 0) {
                        csonObject.getOrCreateTailCommentObject().setAfterComment(commentBuilder.toString().trim());
                    }
                    x.back();
                    return;
                default:
                    throw x.syntaxError("Expected a ',' or '}'");
            }
        }
    }

}
