package com.snoworca.cson;


import java.io.BufferedReader;
import java.io.IOException;

public class YAMLParser {

    private static int getIntent(String line) {
        int i = 0;
        int len = line.length();
        while(i < len) {
            char c = line.charAt(i);
            switch(c) {
                case ' ':
                case '\t':
                    i++;
                    break;
                default:
                    return i;
            }
        }
        return i;
    }

    public static CSONObject parse(String yaml)  {
        try {
            return parse(new BufferedReader(new java.io.StringReader(yaml)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static CSONObject parse(BufferedReader yaml) throws IOException {
        CSONObject csonObject = new CSONObject();
        String line = null;
        int valueIntent = 0;
        while((line = yaml.readLine()) != null) {
            line += "\n";
            String key = null;
            String value = null;
            int intent = getIntent(line);
            char[] array = line.toCharArray();
            for (int i = intent, n = array.length; i < n; i++) {
                char c = array[i];
                switch (c) {
                    case ' ':
                    case '\t':
                        break;
                    case ':':
                        key = line.substring(intent, i);
                        valueIntent = i + 1;
                        break;
                    case '-':
                        break;
                    case '\n':
                        value = line.substring(valueIntent, i);
                        value = value.trim();
                        csonObject.put(key, value);
                        break;
                    case '#':
                        break;
                    default:
                        break;
                }
            }
        }

        return csonObject;

        }



}
