package com.github.sgdc3.xlstosql;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

public class XlsToSqlDocument {
    
    private static final String TABLE_INIT = "CREATE TABLE IF NOT EXISTS %table_name% (%columns%);\r\n";
    private static final String INSERT_ROW = "INSERT INTO %table_name% (%columns%) VALUES (%values%);\r\n";

    // TODO: find a better way to share this value between methods
    // Contains the columns data (Name, Type)
    private Map<String, String> columns;
    
    private StringBuilder output;
    
    public XlsToSqlDocument() {
        output = new StringBuilder();
        // We need to use a LinkedHashMap because we need to keep the right order of the elements
        columns = new LinkedHashMap<>();
    }
    
    public void createTable(String tableName, Row headerRow, Row typesRow, List<Row> rows) throws XlsParseException {
        StringBuilder tableBuilder = new StringBuilder();
        tableBuilder.append(generateTableHeader(tableName, headerRow, typesRow));
        for(Row row : rows) {
            tableBuilder.append(generateRowInsert(tableName, row));
        }
        output.append(tableBuilder);
    }
    
    private String generateRowInsert(String tableName, Row row) {
        List<String> cellContents = new ArrayList<>();
        
        List<Cell> cells = new ArrayList<>();
        Iterator<Cell> cellIterator = row.cellIterator();
        cellIterator.forEachRemaining(cells::add);
        
        int cellCount = 0;
        
        for(Cell cell : cells) {
            
            // Ignore cells outside of the column range
            if(cellCount == columns.size()) {
                break;
            }
            
            int cellType = cell.getCellType();
            String cellContent;
            switch(cellType) {
                default:
                case Cell.CELL_TYPE_ERROR:
                case Cell.CELL_TYPE_BLANK:
                    cellContent = "NULL";
                    break;
                case Cell.CELL_TYPE_BOOLEAN:
                    if(cell.getBooleanCellValue()) {
                        cellContent = "1";
                        break;
                    }
                    cellContent = "0";
                    break;
                case Cell.CELL_TYPE_FORMULA:
                    cellContent = "'" + cell.getCellFormula().replace("'", "''").replace("\r\n", "").replace("\n", "").replace("\r", "") + "'";
                    break;
                case Cell.CELL_TYPE_STRING:
                    cellContent = "'" + cell.getStringCellValue().replace("'", "''").replace("\r\n", "").replace("\n", "").replace("\r", "") + "'";
                    break;
                case Cell.CELL_TYPE_NUMERIC:
                    cellContent = Double.toString(cell.getNumericCellValue()).replaceAll(",", "");
                    break;
            }
            
            cellContents.add(cellContent);
            cellCount++;
        }
        
        // Consider missing cells as null
        while(cellContents.size() < columns.size()) {
            cellContents.add("NULL");
        }
        
        String rowValues = Utils.stringCollectionToString(cellContents);
        String columnNames = Utils.stringCollectionToString(columns.keySet());
        
        String result = INSERT_ROW;
        result = result.replace("%table_name%", tableName);
        result = result.replace("%columns%", columnNames);
        result = result.replace("%values%", rowValues);
        
        return result;
    }
    
    private String generateTableHeader(String tableName, Row headerRow, Row typesRow) throws XlsParseException {
        
        Iterator<Cell> headerIterator = headerRow.cellIterator();
        Iterator<Cell> typesIterator = typesRow.cellIterator();
        
        columns.clear();
        
        while(headerIterator.hasNext()) {
            Cell nameCell = headerIterator.next();
            
            if(nameCell.getCellType() != Cell.CELL_TYPE_STRING) {
                throw new XlsParseException("The first row (which contains the column names) must have only string values! Error at (" + Utils.printCellCoordinates(nameCell) + ")");
            }
            String columnName = nameCell.getStringCellValue().trim().replace(" ", "");
            
            if(!typesIterator.hasNext()) {
                throw new XlsParseException("The second row must define the type of every column!");
            }
            Cell typeCell = typesIterator.next();
            
            if(typeCell.getCellType() != Cell.CELL_TYPE_STRING) {
                throw new XlsParseException("The second row (which contains the column types) must have only string values! Error at (" + Utils.printCellCoordinates(typeCell) + ")");
            }
            String columnType = typeCell.getStringCellValue().trim();
            
            if(columns.containsKey(columnName)) {
                throw new XlsParseException("Found multiple column with the same name! Error at column " + columnName);
            }
            
            columns.put(columnName, columnType);
        }
        
        List<String> columnsSqlList = new ArrayList<>();
        
        for(String columnName : columns.keySet()) {
            String columnType = columns.get(columnName);
            columnsSqlList.add(columnName + " " + columnType);
        }
        
        String columnsSql = Utils.stringCollectionToString(columnsSqlList);

        String result = TABLE_INIT;
        result = result.replace("%table_name%", tableName);
        result = result.replace("%columns%", columnsSql);
        
        return result;
    }
    
    public String getText() {
        return output.toString();
    }
}
