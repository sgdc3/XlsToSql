package com.github.sgdc3.xlstosql;

import com.github.sgdc3.xlstosql.utils.CellUtils;
import com.github.sgdc3.xlstosql.utils.SqlValueUtils;
import com.sun.deploy.util.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class XlsToSqlDocument {

    // Constants
    private final static String NEW_LINE = System.getProperty("line.separator");

    // SQL statements
    private final static String TABLE_INIT = "CREATE TABLE IF NOT EXISTS %table_name% (%columns%);" + NEW_LINE;
    private final static String INSERT_ROW = "INSERT INTO %table_name% (%columns%) VALUES (%values%);" + NEW_LINE;

    // Query builder
    private StringBuilder output;

    public XlsToSqlDocument() {
        output = new StringBuilder();
    }

    public void createTable(String tableName, Row headerRow, Row typesRow, List<Row> rows) throws XlsParseException {
        StringBuilder tableBuilder = new StringBuilder();

        // Obtain columns data (Name, Type)
        Map<String, String> columnsData = parseColumns(headerRow, typesRow);
        // Obtain the row data (List<List<Value>>)
        List<List<String>> rowsData = parseRows(columnsData, rows);

        // Generate the header and the DB entries
        tableBuilder.append(generateHeader(tableName, columnsData));
        for (List<String> row : rowsData) {
            tableBuilder.append(generateRow(tableName, columnsData.keySet(), row));
        }

        // Append the result
        output.append(tableBuilder);
    }

    private Map<String, String> parseColumns(Row headerRow, Row typesRow) throws XlsParseException {
        // We need to use a LinkedHashMap because we need to keep the right order of the elements
        Map<String, String> columns = new LinkedHashMap<>();

        Iterator<Cell> headerIterator = headerRow.cellIterator();
        Iterator<Cell> typesIterator = typesRow.cellIterator();

        // Parse all the columns
        while (headerIterator.hasNext()) {
            Cell nameCell = headerIterator.next();

            if (nameCell.getCellType() != Cell.CELL_TYPE_STRING) {
                throw new XlsParseException("The first row (which contains the column names) must have only string values! Error at (" + CellUtils.printCellCoordinates(nameCell) + ")");
            }
            String columnName = nameCell.getStringCellValue().trim().replace(" ", "");

            if (!typesIterator.hasNext()) {
                throw new XlsParseException("The second row must define the type of every column!");
            }
            Cell typeCell = typesIterator.next();

            if (typeCell.getCellType() != Cell.CELL_TYPE_STRING) {
                throw new XlsParseException("The second row (which contains the column types) must have only string values! Error at (" + CellUtils.printCellCoordinates(typeCell) + ")");
            }
            String columnType = typeCell.getStringCellValue().trim();

            if (columns.containsKey(columnName)) {
                throw new XlsParseException("Found multiple column with the same name! Error at column " + columnName);
            }

            columns.put(columnName, columnType);
        }

        return columns;
    }

    private List<List<String>> parseRows(Map<String, String> columnsData, List<Row> rows) {
        List<List<String>> rowValues = new ArrayList<>();

        // For every row
        for (Row row : rows) {
            List<String> cellValues = new ArrayList<>();

            int cellCount = 0;
            Iterator<Cell> cellIterator = row.cellIterator();

            // For every cell
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();

                // Ignore cells outside of the column range
                if (cellCount == columnsData.size()) {
                    break;
                }

                int cellType = cell.getCellType();
                String cellContent;

                // Parse the value according to the type
                switch (cellType) {
                    default:
                    case Cell.CELL_TYPE_ERROR:
                    case Cell.CELL_TYPE_BLANK:
                        cellContent = "NULL";
                        break;
                    case Cell.CELL_TYPE_BOOLEAN:
                        cellContent = SqlValueUtils.prepareBoolean(cell.getBooleanCellValue());
                        break;
                    case Cell.CELL_TYPE_FORMULA:
                        cellContent = SqlValueUtils.prepareString(cell.getCellFormula());
                        break;
                    case Cell.CELL_TYPE_STRING:
                        cellContent = SqlValueUtils.prepareString(cell.getStringCellValue());
                        break;
                    case Cell.CELL_TYPE_NUMERIC:
                        cellContent = SqlValueUtils.prepareNumber(cell.getNumericCellValue());
                        break;
                }

                cellValues.add(cellContent);
                cellCount++;
            }

            // Consider missing cells as null
            while (cellValues.size() < columnsData.size()) {
                cellValues.add("NULL");
            }

            rowValues.add(cellValues);
        }
        return rowValues;
    }

    private String generateHeader(String tableName, Map<String, String> columns) {
        // Join the column names with the types
        List<String> columnsSqlList = new ArrayList<>();
        for (String columnName : columns.keySet()) {
            String columnType = columns.get(columnName);
            columnsSqlList.add(columnName + " " + columnType);
        }

        // Join the columns
        String columnsSql = StringUtils.join(columnsSqlList, ",");

        // Replace variables in the SQL statement
        String result = TABLE_INIT;
        result = result.replace("%table_name%", tableName);
        result = result.replace("%columns%", columnsSql);
        return result;
    }

    private String generateRow(String tableName, Collection<String> columns, List<String> row) {
        // Join the values
        String rowValues = StringUtils.join(row, ",");
        String columnNames = StringUtils.join(columns, ",");

        // Replace variables in the SQL statement
        String result = INSERT_ROW;
        result = result.replace("%table_name%", tableName);
        result = result.replace("%columns%", columnNames);
        result = result.replace("%values%", rowValues);
        return result;
    }

    // Obtain the result
    public String toString() {
        return output.toString();
    }
}
