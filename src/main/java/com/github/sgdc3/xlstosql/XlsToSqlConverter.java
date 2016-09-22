package com.github.sgdc3.xlstosql;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// TODO: The code should be more OOP oriented. -sgdc3
public class XlsToSqlConverter {

    public static String convert(InputStream source) throws IOException {
        XlsToSqlDocument document = new XlsToSqlDocument();

        // Read the xls document
        HSSFWorkbook workbook = new HSSFWorkbook(source);

        // For every sheet
        Iterator<Sheet> sheetIterator = workbook.sheetIterator();
        while (sheetIterator.hasNext()) {
            Sheet sheet = sheetIterator.next();

            // Get header rows
            Row headerRow = sheet.getRow(0);
            Row typesRow = sheet.getRow(1);

            // Collect data rows
            List<Row> rows = new ArrayList<>();
            for (int i = 2; i < sheet.getLastRowNum(); i++) {
                rows.add(sheet.getRow(i));
            }

            // Create table
            document.createTable(sheet.getSheetName(), headerRow, typesRow, rows);
        }

        // Close the opened xls document
        workbook.close();

        return document.toString();
    }
}
