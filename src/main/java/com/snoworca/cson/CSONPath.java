package com.snoworca.cson;

public class CSONPath {

    private CSONElement csonElement;

    CSONPath(CSONElement csonElement) {
        this.csonElement = csonElement;
    }

    public Boolean optBoolean(String path) {
        return optBoolean(path, null);
    }

    public Boolean optBoolean(String path, Boolean defaultValue) {
        Object obj = get(path);
        if (obj instanceof Boolean) {
            return (Boolean) obj;
        } else if(obj instanceof Number) {
            return ((Number)obj).intValue() == 1;
        } else if(obj instanceof String) {
            return Boolean.parseBoolean((String) obj);
        }
        return defaultValue;
    }

    public Double optDouble(String path) {
        return optDouble(path, null);
    }

    public Double optDouble(String path, Double defaultValue) {
        Object obj = get(path);
        if (obj instanceof Number) {
            return ((Number)obj).doubleValue();
        }
        else if(obj instanceof Boolean) {
            return (Boolean)obj ? 1.0 : 0.0;
        }
        else if(obj instanceof String) {
            try {
                return Double.parseDouble((String) obj);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public Float optFloat(String path) {
        return optFloat(path, null);
    }

    public Float optFloat(String path, Float defaultValue) {
        Object obj = get(path);
        if (obj instanceof Number) {
            return ((Number)obj).floatValue();
        }
        else if(obj instanceof Boolean) {
            return (Boolean)obj ? 1.0f : 0.0f;
        }
        else if(obj instanceof String) {
            try {
                return Float.parseFloat((String) obj);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }


        return defaultValue;
    }

    public Long optLong(String path) {
        return optLong(path, null);
    }

    public Long optLong(String path, Long defaultValue) {
        Object obj = get(path);
        if (obj instanceof Number) {
            return ((Number)obj).longValue();
        }
        else if(obj instanceof Boolean) {
            return (Boolean)obj ? 1L : 0L;
        }
        else if(obj instanceof String) {
            try {
                return Long.parseLong((String) obj);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public Short optShort(String path) {
        return optShort(path, null);
    }

    public Short optShort(String path, Short defaultValue) {
        Object obj = get(path);
        if (obj instanceof Number) {
            return ((Number)obj).shortValue();
        }
        else if(obj instanceof Boolean) {
            return (Boolean)obj ? (short)1 : (short)0;
        }
        else if(obj instanceof String) {
            try {
                return Short.parseShort((String) obj);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public Byte optByte(String path) {
        return optByte(path, null);
    }

    public Byte optByte(String path, Byte defaultValue) {
        Object obj = get(path);
        if (obj instanceof Number) {
            return ((Number)obj).byteValue();
        }
        else if(obj instanceof Boolean) {
            return (Boolean)obj ? (byte)1 : (byte)0;
        }
        else if(obj instanceof String) {
            try {
                return Byte.parseByte((String) obj);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }


    public Integer optInteger(String path) {
        return optInteger(path, null);
    }

    public Integer optInteger(String path, Integer defaultValue) {
        Object obj = get(path);
        if (obj instanceof Number) {
            return ((Number)obj).intValue();
        }
        else if(obj instanceof Boolean) {
            return (Boolean)obj ? 1 : 0;
        }
        else if(obj instanceof String) {
            try {
                return Integer.parseInt((String) obj);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public String optString(String path, String defaultValue) {
        Object obj = get(path);
        if (obj instanceof String) {
            return (String) obj;
        } else if(obj instanceof Number) {
            return String.valueOf(obj);
        }
        return defaultValue;
    }

    public String optString(String path) {
        return optString(path, null);
    }

    public CSONObject optCSONObject(String path) {
        Object obj = get(path);
        if (obj instanceof CSONObject) {
            return (CSONObject) obj;
        }
        return null;
    }


    public CSONArray optCSONArray(String path) {
        Object obj = get(path);
        if (obj instanceof CSONArray) {
            return (CSONArray) obj;
        }
        return null;
    }







    public Object get(String path) {
        String[] pathArray = path.split("\\.");
        Object value = csonElement;
        for (String key : pathArray) {
            if (key.matches("^.*\\[\\d\\]$") && value instanceof CSONElement) {
                int start = key.indexOf("[");
                int index = Integer.parseInt(key.substring(start + 1, key.length() - 1));
                String arrayKey = key.substring(0, start);
                if(value instanceof CSONObject) {
                    value = ((CSONObject) value).get(arrayKey);
                }
                if(!(value instanceof CSONArray)) {
                    return null;
                }
                value = ((CSONArray) value).opt(index);
            } else if (value instanceof CSONObject) {
                value = ((CSONObject) value).opt(key);
            } else {
                return null;
            }
        }
        return value;
    }
}
