package com.github.sgdc3.xlstosql;

import java.util.Collection;

import org.apache.poi.ss.usermodel.Cell;

public class Utils {
    
    public static String replaceLast(String text, String regex, String replacement) {
        return text.replaceFirst("(?s)"+regex+"(?!.*?"+regex+")", replacement);
    }
    
    public static String stringCollectionToString(Collection<String> list) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for(String s : list) {
            if(!first) {
                builder.append(", ");
            }
            builder.append(s);
            first = false;
        }
        return builder.toString();
    }
    
    public static String printCellCoordinates(Cell cell) {
        return cell.getRowIndex() + "," + cell.getColumnIndex();
    }
}
