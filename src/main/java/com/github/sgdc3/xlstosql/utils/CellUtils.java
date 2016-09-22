package com.github.sgdc3.xlstosql.utils;

import org.apache.poi.ss.usermodel.Cell;

public class CellUtils {

    public static String printCellCoordinates(Cell cell) {
        return cell.getRowIndex() + "," + cell.getColumnIndex();
    }
}
