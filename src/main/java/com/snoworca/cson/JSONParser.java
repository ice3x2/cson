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

        KeyValueValueCommentObject lastKeyValueCommentObject = new KeyValueValueCommentObject();
        StringBuilder commentBuilder = new StringBuilder();

        char nextChar = readComment(x, commentBuilder);
        if(commentBuilder.length() > 0) {
            csonArray.getHeadCommentObject().setBeforeKey(commentBuilder.toString().trim());
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
                    lastKeyValueCommentObject.setBeforeValue(commentBuilder.toString().trim());
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
                    if(value instanceof  CSONObject) {
                        CSONObject valueObject = (CSONObject) value;
                        valueObject.setHeadComment(lastKeyValueCommentObject.getBeforeValue());
                        lastKeyValueCommentObject.setAfterValue(valueObject.getTailCommentObject().getAfterKey());
                        nextChar = x.nextClean();
                    }
                    else {
                        nextChar = readComment(x, commentBuilder);
                        if(commentBuilder.length() > 0) {
                            lastKeyValueCommentObject.setAfterValue(commentBuilder.toString().trim());
                        }
                    }
                    csonArray.addAtJSONParsing(value);

                }
                if(!lastKeyValueCommentObject.isEmpty()) {
                    csonArray.addCommentObjects(lastKeyValueCommentObject);
                    lastKeyValueCommentObject = new KeyValueValueCommentObject();
                } else {
                    csonArray.addCommentObjects(null);
                }

                switch (nextChar) {
                    case 0:
                        // array is unclosed. No ']' found, instead EOF
                        throw x.syntaxError("Expected a ',' or ']'");
                    case ',':
                        nextChar = x.nextClean();
                        if (nextChar == 0) {
                            // array is unclosed. No ']' found, instead EOF
                            throw x.syntaxError("Expected a ',' or ']'");
                        }
                        if (nextChar == ']') {
                            return;
                        }
                        x.back();
                        break;
                    case ']':
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

        KeyValueValueCommentObject lastKeyValueCommentObject = new KeyValueValueCommentObject();
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
                lastKeyValueCommentObject.setBeforeKey(commentBuilder.toString().trim());
            }

            switch (c) {
                case 0:
                    throw x.syntaxError("A JSONObject text must end with '}'");
                case '}':
                    if(lastKeyValueCommentObject.getBeforeKey() != null) {
                        csonObject.getTailCommentObject().setBeforeKey(lastKeyValueCommentObject.getBeforeKey());
                        lastKeyValueCommentObject.setBeforeKey(null);
                    }
                    readComment(x, commentBuilder);
                    if(commentBuilder.length() > 0) {
                        csonObject.getTailCommentObject().setAfterKey(commentBuilder.toString().trim());
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
                        lastKeyValueCommentObject.setBeforeKey(commentBuilder.toString().trim());
                    }
                default:
                    x.back();
                    key = x.nextValue().toString();
            }

            c = readComment(x, commentBuilder);
            if(commentBuilder.length() > 0) {
                lastKeyValueCommentObject.setAfterKey(commentBuilder.toString().trim());
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
                    lastKeyValueCommentObject.setBeforeValue(commentBuilder.toString().trim());
                }
                x.back();
                Object value = x.nextValue();
                putAtJSONParsing(csonObject, key, value);
                if(value instanceof CSONObject) {
                    CSONObject objectValue = (CSONObject)value;
                    String commentAfterKey = objectValue.getTailCommentObject().getAfterKey();
                    if(commentAfterKey != null) {
                        lastKeyValueCommentObject.setAfterValue(commentAfterKey);
                    }
                    String commentHead = lastKeyValueCommentObject.getBeforeValue();
                    if(commentHead != null) {
                        objectValue.getHeadCommentObject().setBeforeKey(commentHead);
                    }
                    if(lastKeyValueCommentObject.isCommented()) {
                        csonObject.putCommentObject(key, lastKeyValueCommentObject);
                        lastKeyValueCommentObject = new KeyValueValueCommentObject();
                    }

                    //continue;
                }
                else if(value instanceof CSONArray) {
                    CSONArray objectValue = (CSONArray)value;
                    //x.back();
                }


                next = readComment(x, commentBuilder);
                if(commentBuilder.length() > 0) {
                    lastKeyValueCommentObject.setAfterValue(commentBuilder.toString().trim());
                }


                if(lastKeyValueCommentObject.isCommented()) {
                    csonObject.putCommentObject(key, lastKeyValueCommentObject);
                    lastKeyValueCommentObject = new KeyValueValueCommentObject();
                }
            } else {
                next = readComment(x, commentBuilder);
                if(commentBuilder.length() > 0) {
                    lastKeyValueCommentObject.setAfterValue(commentBuilder.toString().trim());
                }
            }


            switch (next) {
                case ';':
                case ',':
                    readComment(x, commentBuilder);
                    if(commentBuilder.length() > 0) {
                        lastKeyValueCommentObject.setBeforeKey(commentBuilder.toString().trim());
                    }
                    x.back();
                    break;
                case '}':
                    readComment(x, commentBuilder);
                    if(commentBuilder.length() > 0) {
                        csonObject.getTailCommentObject().setAfterKey(commentBuilder.toString().trim());
                    }
                    x.back();
                    return;
                default:
                    throw x.syntaxError("Expected a ',' or '}'");
            }
        }
    }

}
