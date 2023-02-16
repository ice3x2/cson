package com.snoworca.cson;

class JSONParser {

    private JSONTokener tokener;
    private JSONOptions options;


    protected JSONParser(JSONTokener tokener) {
        this.options = tokener.getJsonOption();
        this.tokener = tokener;

    }


    private char nextComment(StringBuilder commentBuilder) throws CSONException {
        tokener.back();
        char next = tokener.next();
        boolean isMultiLine = false;
        while(next == '/') {
            char nextC = tokener.next();
            if(nextC == '/') {
                String strComment = tokener.nextTo('\n');
                if(isMultiLine) commentBuilder.append("\n");
                commentBuilder.append(strComment);
                isMultiLine = true;
                next = tokener.nextClean();
            } else if(nextC == '*') {
                String strComment = null;
                try {
                    strComment = tokener.nextToFromString("*/", true);
                } catch (CSONException e) {
                    throw tokener.syntaxError("Unterminated comment", e);
                }
                if(isMultiLine) commentBuilder.append("\n");
                commentBuilder.append(strComment);
                isMultiLine = true;
                next = tokener.nextClean();
            } else  {
                next = nextC;
            }
        }
        return next;
    }


    private char skipComment() throws CSONException {
        tokener.back();
        char next = tokener.next();
        boolean isMultiLine = false;
        while(next == '/') {
            char nextC = tokener.next();
            if(nextC == '/') {
                tokener.skipTo('\n');
                next = tokener.nextClean();
            } else if(nextC == '*') {
                String strComment = null;
                try {
                    tokener.skipTo("*/");
                } catch (CSONException e) {
                    throw tokener.syntaxError("Unterminated comment", e);
                }
                next = tokener.nextClean();
            } else  {
                next = nextC;
            }
        }
        return next;
    }


    private char readOrSkipComment(StringBuilder commentBuilder) {
        char nextClean = tokener.nextClean();
        commentBuilder.setLength(0);
        if(nextClean  == '/') {
            if(!options.isAllowComments()) {
                char nextChar = tokener.next();
                if(nextChar == '/' || nextChar == '*') {
                    throw tokener.syntaxError("Comments are not allowed");
                } else {
                    throw tokener.syntaxError("Expected a ',' or ']'");
                }
            }
            if(!options.isSkipComments()) {
                nextClean = nextComment(commentBuilder);
            } else {
                nextClean = skipComment();
            }
            return nextClean;
        }
        return nextClean;
    }

    private void putAtJSONParsing(CSONObject csonObject, String key, Object value) {
        if(value instanceof String && CSONElement.isBase64String((String)value)) {
            value = CSONElement.base64StringToByteArray((String)value);
        }
        csonObject.put(key, value);
    }

    public void parseArray(CSONArray csonArray) {
        //if(isPureJson) {
        //    parseArrayFromPureJson(csonArray);
        //} else {
            parseArrayFromJson5(csonArray);
        //}
    }


    /*
    private  void parseArrayFromPureJson(CSONArray csonArray) {
        if (tokener.nextClean() != '[') {
            throw tokener.syntaxError("A JSONArray text must start with '['");
        }
        char nextChar = tokener.nextClean();
        if (nextChar == 0) {
            // array is unclosed. No ']' found, instead EOF
            throw tokener.syntaxError("Expected a ',' or ']'");
        }
        if (nextChar != ']') {
            tokener.back();
            for (;;) {
                if (tokener.nextClean() == ',') {
                    tokener.syntaxError("Missing value");
                } else {
                    tokener.back();
                    csonArray.addAtJSONParsing(tokener.nextPureValue(options));
                }
                switch (tokener.nextClean()) {
                    case 0:
                        // array is unclosed. No ']' found, instead EOF
                        throw tokener.syntaxError("Expected a ',' or ']'");
                    case ',':
                        nextChar = tokener.nextClean();
                        if (nextChar == 0) {
                            // array is unclosed. No ']' found, instead EOF
                            throw tokener.syntaxError("Expected a ',' or ']'");
                        }
                        if (nextChar == ']') {
                            return;
                        }
                        tokener.back();
                        break;
                    case ']':
                        return;
                    default:
                        throw tokener.syntaxError("Expected a ',' or ']'");
                }
            }
        }
    }*/

    private void parseArrayFromJson5(CSONArray csonArray) throws CSONException {

        StringBuilder commentBuilder = new StringBuilder();
        CommentObject lastCommentObject = new CommentObject();

        char nextChar = readOrSkipComment( commentBuilder);
        if(commentBuilder.length() > 0) {
            csonArray.setHeadComment(commentBuilder.toString().trim());
        }

        if (nextChar != '[') {
            throw tokener.syntaxError("A JSONArray text must start with '['");
        }

        if (nextChar != ']') {

            if (nextChar == 0) {
                throw tokener.syntaxError("Expected a ',' or ']'");
            }

            for (;;) {
                String beforeComment = lastCommentObject.getAfterComment();
                if(beforeComment == null) {
                    nextChar = readOrSkipComment( commentBuilder);
                    if(commentBuilder.length() > 0) {
                        lastCommentObject.setBeforeComment(commentBuilder.toString().trim());
                    }
                } else {
                    nextChar = tokener.nextClean();
                }

                if (nextChar == 0) {
                    throw tokener.syntaxError("Expected a ',' or ']'");
                }
                if(nextChar == ']') {
                    readOrSkipComment( commentBuilder);
                    if(commentBuilder.length() > 0) {
                        csonArray.getOrCreateTailCommentObject().setAfterComment(commentBuilder.toString().trim());
                    }
                    tokener.back();
                    return;
                }
                tokener.back();
                if (nextChar == ',') {
                    csonArray.addAtJSONParsing(null);
                    nextChar = tokener.nextClean();
                } else {
                    Object value = tokener.nextValue();
                    if(value instanceof CSONElement) {
                        CSONElement valueObject = (CSONElement) value;
                        valueObject.setHeadComment(lastCommentObject.getBeforeComment());
                        CommentObject commentObject = valueObject.getTailCommentObject();
                        if(commentObject != null && commentObject.isCommented()) {
                            lastCommentObject.setAfterComment(valueObject.getTailCommentObject().getAfterComment());
                        }
                        nextChar = tokener.nextClean();
                        csonArray.addAtJSONParsing(value);
                        //if(nextChar != ']') {
                           // continue;
                        //}
                    }
                    else {
                        nextChar = readOrSkipComment( commentBuilder);
                        if(commentBuilder.length() > 0) {
                            lastCommentObject.setAfterComment(commentBuilder.toString().trim());
                        }
                        csonArray.addAtJSONParsing(value);
                    }
                }
                if(lastCommentObject.isCommented()) {
                    csonArray.addCommentObjects(lastCommentObject);
                } else {
                    csonArray.addCommentObjects(null);
                }
                lastCommentObject = new CommentObject();

                switch (nextChar) {
                    case 0:
                        // array is unclosed. No ']' found, instead EOF
                        throw tokener.syntaxError("Expected a ',' or ']'");
                    case ',':
                        nextChar = tokener.nextClean();

                        if(commentBuilder.length() > 0) {
                            lastCommentObject.setBeforeComment(commentBuilder.toString().trim());
                        }
                        if (nextChar == 0) {
                            // array is unclosed. No ']' found, instead EOF
                            throw tokener.syntaxError("Expected a ',' or ']'");
                        }
                        if (nextChar == ']') {
                            csonArray.getOrCreateTailCommentObject().setBeforeComment(lastCommentObject.getBeforeComment());
                            readOrSkipComment( commentBuilder);
                            if(commentBuilder.length() > 0) {
                                csonArray.getOrCreateTailCommentObject().setAfterComment(commentBuilder.toString().trim());
                            }
                            tokener.back();
                            return;
                        }
                        tokener.back();
                        break;
                    case ']':
                        readOrSkipComment( commentBuilder);
                        if(commentBuilder.length() > 0) {
                            csonArray.getOrCreateTailCommentObject().setAfterComment(commentBuilder.toString().trim());
                        }
                        tokener.back();
                        return;
                    case '[':
                    case '{':
                        tokener.back();
                        continue;
                    default:
                        throw tokener.syntaxError("Expected a ',' or ']'");
                }
            }
        }
    }

    void parseObject(CSONObject csonObject) {
        //if(isPureJson) {
        //    parseObjectFromPureJson(csonObject);
        //} else {
            parseObjectFromJson5( csonObject);
        //}
    }

    /*
    void parseObjectFromPureJson(CSONObject csonObject) {
        char c;
        String key = null;
        char nextClean = tokener.nextClean();
        if (nextClean != '{') {
            throw tokener.syntaxError("A JSONObject text must begin with '{'");
        }
        for (;;) {
            c = tokener.nextClean();
            switch (c) {
                case 0:
                    throw tokener.syntaxError("A JSONObject text must end with '}'");
                case '}':
                    return;
                case ',':
                    tokener.next();

                    //throw tokener.syntaxError("Missing \"key\":\"value\" after comma.");
                default:
                    tokener.back();
                    key = tokener.nextPureKey(options).toString();
            }

            c = tokener.nextClean();
            if (c == '=') {
                if (tokener.next() != '>') {
                    tokener.back();
                }
            } else if (c != ':') {
                throw tokener.syntaxError("Expected a ':' after a key");
            }
            Object value = tokener.nextPureValue(options);
            if (key != null) {
                putAtJSONParsing(csonObject, key, value);
            }

            switch (tokener.nextClean()) {
                case ';':
                case ',':
                    tokener.back();
                    break;
                case '}':
                    return;
                default:
                    throw tokener.syntaxError("Expected a ',' or '}'");
            }
        }
    }*/

    void parseObjectFromJson5(CSONObject csonObject) {
        char c;
        String key = null;

        CommentObject valueCommentObject = new CommentObject();
        CommentObject lastKeyObject = new CommentObject();
        StringBuilder commentBuilder = new StringBuilder();

        char lastPrev = 0;
        char nextClean = readOrSkipComment( commentBuilder);
        if(commentBuilder.length() > 0) {
            csonObject.setHeadComment(commentBuilder.toString().trim());
        }
        if (nextClean != '{') {
            throw tokener.syntaxError("A JSONObject text must begin with '{'");
        }
        for (;;) {
            char prev = tokener.getPrevious();
            c = readOrSkipComment(commentBuilder);
            if(commentBuilder.length() > 0) {
                lastKeyObject.setBeforeComment(commentBuilder.toString().trim());
            }
            switch (c) {
                case 0:
                    throw tokener.syntaxError("A JSONObject text must end with '}'");
                case '}':
                    if(lastPrev == ',' && !options.isAllowTrailingComma()) {
                        throw tokener.syntaxError("Expected a key:value pair after ','");
                    }
                    if(lastKeyObject.getBeforeComment() != null) {
                        csonObject.getOrCreateTailCommentObject().setBeforeComment(lastKeyObject.getBeforeComment());
                    }
                    readOrSkipComment( commentBuilder);
                    if(commentBuilder.length() > 0) {
                        csonObject.getOrCreateTailCommentObject().setAfterComment(commentBuilder.toString().trim());
                    }
                    tokener.back();
                    return;
                case '{':
                case '[':
                    if(prev=='{') {
                        throw tokener.syntaxError("A JSON Object can not directly nest another JSON Object or JSON Array.");
                    }
                    // fall through
                    readOrSkipComment( commentBuilder);
                    if(commentBuilder.length() > 0) {
                        lastKeyObject.setBeforeComment(commentBuilder.toString().trim());
                    }
                default:
                    tokener.back();
                    key = tokener.nextValue().toString();
            }

            c = readOrSkipComment( commentBuilder);
            if(commentBuilder.length() > 0) {
                lastKeyObject.setAfterComment(commentBuilder.toString().trim());
            }

            // The key is followed by ':'.
            //c = x.nextClean();
            if (c != ':') {
                throw tokener.syntaxError("Expected a ':' after a key");
            }

            char next;
            // Use syntaxError(..) to include error location
            if (key != null) {
                // Check if key exists
                if (csonObject.opt(key) != null) {
                    // key already exists
                    throw tokener.syntaxError("Duplicate key \"" + key + "\"");
                }


                readOrSkipComment( commentBuilder);
                if(commentBuilder.length() > 0) {
                    valueCommentObject.setBeforeComment(commentBuilder.toString().trim());
                }
                tokener.back();
                Object value = tokener.nextValue();
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

                next = readOrSkipComment(commentBuilder);
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
                next = readOrSkipComment( commentBuilder);
                if(commentBuilder.length() > 0) {
                    lastKeyObject.setAfterComment(commentBuilder.toString().trim());
                }
            }

            lastPrev = next;
            switch (next) {
                case ';':
                case ',':
                    readOrSkipComment(commentBuilder);
                    if(commentBuilder.length() > 0) {
                        lastKeyObject.setBeforeComment(commentBuilder.toString().trim());
                    }
                    tokener.back();
                    break;
                case '}':
                    if(lastPrev == ',' && !options.isAllowTrailingComma()) {
                        throw tokener.syntaxError("Expected a key:value pair after ','");
                    }
                    readOrSkipComment( commentBuilder);
                    if(commentBuilder.length() > 0) {
                        csonObject.getOrCreateTailCommentObject().setAfterComment(commentBuilder.toString().trim());
                    }
                    tokener.back();
                    return;
                default:
                    throw tokener.syntaxError("Expected a ',' or '}'");
            }
        }
    }

}
