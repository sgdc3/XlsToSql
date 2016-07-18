package com.github.sgdc3.xlstosql;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

public class XlsToSqlConverter {
    private final List<InputStream> sources;

    private String result;

    public XlsToSqlConverter(List<InputStream> sources) {
        this.sources = sources;
        this.result = null;
    }
    
    public String getResult() {
        return result;
    }
    
    public String convert() throws IOException {
        XlsToSqlDocument document = new XlsToSqlDocument();
        for (InputStream source : sources) {
            HSSFWorkbook workbook = new HSSFWorkbook(source);
            
            List<Sheet> sheets = new ArrayList<Sheet>();
            workbook.iterator().forEachRemaining(sheets::add);
            for(Sheet sheet : sheets) {
                String sheetName = sheet.getSheetName();
                
                List<Row> rows = new ArrayList<Row>();
                sheet.rowIterator().forEachRemaining(rows::add);
                Row headerRow = rows.remove(0);
                Row typesRow = rows.remove(0);
                
                document.createTable(sheetName, headerRow, typesRow, rows);
            }
            
            workbook.close();
            result = document.getText();
        }
        return result;
    }
}
