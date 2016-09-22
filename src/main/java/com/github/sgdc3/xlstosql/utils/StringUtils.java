package com.github.sgdc3.xlstosql.utils;

import java.util.Collection;

public class StringUtils {

    public static String join(Collection<?> collection, String delimiter) {
        StringBuilder result = new StringBuilder();

        for (Object object : collection) {
            if (result.length() != 0) {
                result.append(delimiter);
            }
            result.append(object);
        }

        return result.toString();
    }
}
