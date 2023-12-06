package com.snoworca.cson;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayDeque;

class PureJSONParser {


    private final static String NULL = "null";


    private PureJSONParser() {
    }
    enum Mode {
        String,
        Number,
        WaitValue,
        OpenArray,
        CloseObject,
        CloseArray,
        WaitValueSeparator,
        NextStoreSeparator, // , 가 나오기를 기다림
        InKey,
        WaitKey, // 키가 나오기를 기다림
    }




    static CSONElement parsePureJSON(StringReader reader) {

        //ArrayDeque<Mode> modeStack = new ArrayDeque<>();
        ArrayDeque<CSONElement> csonElements = new ArrayDeque<>();
        CSONElement currentElement = null;

        Mode currentMode = null;
        StringBuilder dataStringBuilder = new StringBuilder();
        String key = null;

        int index = 0;
        try {
            int c;
            boolean isArray = false;
            boolean isFloat = false;
            while ((c = reader.read()) != -1) {
                String sc = (char)c + "";
                System.out.print(sc);
                ++index;
                //Mode currentMode = modeStack.peekLast();
                if(c != '"' && (currentMode == Mode.String || currentMode == Mode.InKey)) {
                    dataStringBuilder.append((char)c);
                } else if(currentMode == Mode.Number && c != ',') {
                    if(c == '.' || c == 'E' || c == 'e') {
                        isFloat = true;
                    }
                    dataStringBuilder.append((char)c);
                }
                else if(c == '{') {
                    if(currentMode != Mode.WaitValue && currentMode != null) {
                        throw new CSONParseException("Unexpected character '{' at " + index);
                    }
                    currentMode = Mode.WaitKey;
                    //currentMode  =Mode.WaitKey);
                    currentElement = new CSONObject();
                    csonElements.offerLast(currentElement);
                    isArray = false;
                } else if(c == '[') {
                    if(currentMode != null && currentMode != Mode.WaitValue) {
                        throw new CSONParseException("Unexpected character '[' at " + index);
                    }
                    currentMode  = Mode.WaitValue;
                    currentElement = new CSONArray();
                    csonElements.offerLast(currentElement);
                    isArray = true;
                } else if(c == ']'  || c == '}') {
                    if(currentMode != Mode.NextStoreSeparator) {
                        throw new CSONParseException("Unexpected character '" + c + "' at " + index);
                    }

                    currentMode  =Mode.NextStoreSeparator;
                    csonElements.removeLast();
                    if(csonElements.isEmpty()) {
                        return currentElement;
                    }
                    currentElement = csonElements.getLast();
                    isArray = currentElement instanceof CSONArray;
                } else if(c == ',') {
                    if(currentMode != Mode.NextStoreSeparator && currentMode != Mode.Number) {
                        throw new CSONParseException("Unexpected character ',' at " + index);
                    }
                    if(currentMode == Mode.Number) {
                        String numberString = dataStringBuilder.toString().trim();
                        dataStringBuilder.setLength(0);
                        if(numberString.isEmpty()) {
                            throw new CSONParseException("Unexpected character ',' at " + index);
                        }

                        if(NULL.equalsIgnoreCase(numberString)) {
                            putStringData(currentElement, null, key);
                        } else {
                            Number numberValue = null;
                            Boolean booleanValue = null;
                            boolean isHex = false;
                            char firstChar = numberString.charAt(0);
                            if (firstChar == '+') {
                                numberString = numberString.substring(1);
                            } else if (firstChar == '.') {
                                numberString = "0" + numberString;
                            }
                            if (firstChar == '0' && numberString.length() > 1) {
                                char secondChar = numberString.charAt(1);
                                if (secondChar == 'x' || secondChar == 'X') {
                                    isHex = true;
                                }
                            }

                            try {
                                if (firstChar == 't' || firstChar == 'T' || firstChar == 'F' ||   firstChar == 'f' ) {
                                    booleanValue = Boolean.parseBoolean(numberString.toLowerCase());
                                }
                                else if (isHex) {
                                        numberValue = Long.parseLong(numberString.substring(2), 16);
                                } else if (isFloat) {
                                    numberValue = Double.parseDouble(numberString);
                                } else {
                                    numberValue = Long.parseLong(numberString);
                                }
                            } catch (NumberFormatException e) {
                                throw new CSONParseException("Number format error value '" +  numberString + "' at " + index, e);
                            }
                            if(booleanValue != null) {
                                putBooleanData(currentElement, booleanValue, key);
                            } else {
                                putNumberData(currentElement, numberValue, key);
                            }
                            key = null;
                            isFloat = false;
                        }
                    }

                    if(currentElement instanceof CSONArray) {
                        currentMode  = Mode.WaitValue;
                    } else {
                        currentMode  =Mode.WaitKey;
                    }
                }
                else if(c == '"') {
                    if(currentMode != Mode.String && currentMode != Mode.WaitKey && currentMode != Mode.WaitValue && currentMode != Mode.InKey) {
                        throw new CSONParseException("Unexpected character '\"' at " + index);
                    }
                    else if(currentMode == Mode.InKey) {
                        key = dataStringBuilder.toString();
                        dataStringBuilder.setLength(0);
                        currentMode  = Mode.WaitValueSeparator;

                    }
                    else if(currentMode == Mode.String) {
                        String value = dataStringBuilder.toString();
                        putStringData(currentElement, value, key);
                        key = null;
                        dataStringBuilder.setLength(0);
                        currentMode  =Mode.NextStoreSeparator;
                    }
                    else if(currentMode == Mode.WaitValue) {
                        
                        currentMode  =Mode.String;
                    }
                    else if(currentMode == Mode.WaitKey) {
                        
                        currentMode  =Mode.InKey;
                    }
                } else if(c == ':') {
                    if(currentMode != Mode.WaitValueSeparator) {
                        throw new CSONParseException("Unexpected character ':' at " + index);
                    } else {
                        
                        currentMode  =Mode.WaitValue;
                    }
                } else if(currentMode == Mode.WaitValue && !Character.isSpaceChar(c)  && c != '\n' && c != '\r' && c != '\t' && c != '\b' && c != '\f' && c != '\0' && c != 0xFEFF) {
                    dataStringBuilder.append((char)c);
                    currentMode  =Mode.Number;
                }
            }
        } catch (CSONParseException e) {
            throw e;
        } catch (IOException e) {
            throw new CSONParseException(e.getMessage());
        }
        throw new CSONParseException("Unexpected end of stream");
    }

    private static void putStringData(CSONElement currentElement, String value, String key) {
        if("Night mode".equalsIgnoreCase(value)) {
            System.out.println("Night mode");
        }
        if(key != null) {
            ((CSONObject)currentElement).put(key, value);
        } else {
            ((CSONArray)currentElement).add(value);
        }
    }


    private static void putBooleanData(CSONElement currentElement, boolean value, String key) {
        if(key != null) {
            ((CSONObject)currentElement).put(key, value);
        } else {
            ((CSONArray)currentElement).add(value);
        }
    }
    private static void putNumberData(CSONElement currentElement, Number value, String key) {
        if(currentElement instanceof  CSONObject) {
            ((CSONObject)currentElement).put(key, value);
        } else {
            ((CSONArray)currentElement).add(value);
        }
    }

}
