package com.snoworca.cson.serialize;


public class StringTokener {
    private final char[] input;
    private final String inputString;
    private final int endIndex;
    private int position;

    private  boolean isLastPrevChar = false;

    public StringTokener(String input) {
        this.input = input.toCharArray();
        this.endIndex = input.length() - 1;
        this.inputString = input;
        this.position = -1;
    }

    public int getPosition(){
        return position;
    }


    public int setPosition(int position){
        if(position < 0 || position > endIndex)
            throw new IndexOutOfBoundsException("position out of bounds: " + position + " >= length(" + (this.endIndex + 1) + ")");

        this.position = position;
        return position;
    }

    public boolean isBegin(){
        return position < 0;
    }


    public boolean movePosition(char token){
        int lastPosition = position;
        if(position >= endIndex) {
            return false;
        }
        char ch = current();
        while(ch != token && position < endIndex) {
            if(position >= endIndex) {
                position = lastPosition;
                return false;
            }
            ch = next();
        }
        return true;
    }



    public char current() {
        if (position > this.endIndex || position < 0) {
            return '\0';
        }
        return input[position];
    }


    public String getInput(){
        return inputString;
    }

    public char next() {
        if (position >= this.endIndex) {
            return '\0';
        }
        return input[++position];
    }




    public char nextSkipSpace() {
        char ch = next();
        while (ch < 33 && ch != '\0') {
            ch = next();
        }
        return ch;
    }


    public boolean moveFirst() {
        if (input.length == 0 || position > 0) {
            return false;
        }
        position = 0;
        return true;
    }

    public char skipSpace() {
        /*if(position < 0) position = 0;
        if(position < endIndex) {
            position = 0;
        }*/
        char ch = input[position];
        boolean moveNext = false;
        while (ch < 33 && ch != '\0' && position < this.endIndex) {
            ch = input[++position];
            moveNext = true;
        }
        if(moveNext) {
            --position;

        }
        return ch;
    }

    public char prev() {
        if (position == 0) {
            --position;
            return '\0';
        }
        return input[--position];
    }

    public String readTo(char token) {
        int lastPosition = position;
        StringBuilder sb = new StringBuilder();
        char ch = next();
        while (ch != token && ch != '\0') {
            sb.append(ch);
            if (position >= this.endIndex) {
                position = lastPosition;
                return null;
            }
            ch = next();
        }
        return sb.toString();
    }


    public String readString() {
        return readString(current());
    }

    public String readString(char stringQuote) {
        StringBuilder sb = new StringBuilder();
        char ch = '\0';
        boolean isEscape = false;
        if(current() != stringQuote) {
            sb.append(current());
        }
        while ((ch = next()) != '\0') {
            if (isEscape) {
                isEscape = false;
            } else if (ch == stringQuote) {
                return sb.toString();
            } else if (ch == '\\') {
                isEscape = true;
                continue;
            }
            sb.append(ch);
        }
        // todo error handling
        // String 이 시작되었지만 끝나지 않움.
        return null;
    }


    public String readTo(char token1, char token2) {
        int lastPosition = position;
        StringBuilder sb = new StringBuilder();
        char ch = current();
        while (ch != token1 && ch != token2 && ch != '\0') {
            sb.append(ch);
            ch = next();
        }
        if (ch == '\0') {
            position = lastPosition;
            return null;
        }
        --position;
        return sb.toString();
    }

    public void rewindTo(char token) {
        char ch = current();
        while (ch != token && position >= 0) {
            ch = prev();
        }
    }

}
