package com.github.sgdc3.xlstosql.utils;

public class SqlValueUtils {

    public static String prepareString(String string) {
        return "'" + string.replace("'", "''").replace("\r\n", "").replace("\n", "").replace("\r", "") + "'";
    }

    public static String prepareNumber(Double value) {
        return value.toString().replaceAll(",", "");
    }

    public static String prepareBoolean(boolean value) {
        if (value) {
            return "1";
        }
        return  "0";
    }
}
