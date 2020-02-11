package com.nusinfineon.core;

import org.apache.poi.ss.usermodel.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class OutputCore {
    public static void main(String[] args) throws IOException {
        getOutputSummaryStatistics("C:\\Users\\Ahmad\\Documents\\Workspace\\playground\\src\\main\\resources\\output_min_size_22.xlsx",
                "C:\\Users\\Ahmad\\Documents\\Workspace\\playground\\src\\main\\resources\\product_key_cost.xlsx");
    }

    public static void getOutputSummaryStatistics(String outputExcelFilePath, String productKeyCostExcelFilePath) throws IOException {
        String UTIL_RES_REP = "Util Res Rep";
        String DAILY_THROUGHPUT_RES_REP = "Daily Throughput Res Rep";

        // Creates a temp excel file for referencing
        File originalInputFile = new File(outputExcelFilePath);
        File tempOutputFile = new File(outputExcelFilePath + "temp.xlsx");
        copyFileUsingStream(originalInputFile, tempOutputFile);

        if (!tempOutputFile.exists()) {
            throw new IOException("File not found in: " + outputExcelFilePath);
        }

        Workbook workbook = WorkbookFactory.create(tempOutputFile);

        // Get average utilization rates of IBIS Ovens =================================================================
        Sheet utilSheet = workbook.getSheet(UTIL_RES_REP);
        if (utilSheet == null) {
            throw new IOException("Excel file doesn't contain sheet: " + UTIL_RES_REP);
        }

        // Get column names and corresponding index. Assumes first 2 columns are "platform" and "name"
        HashMap<Integer, String> mapOfIndexToColumnName = new HashMap<Integer, String>();
        HashMap<String, Double> mapOfColumnNameToTotalUtilRate = new HashMap<String, Double>();
        Row headerRow = utilSheet.getRow(0);
        for (int cellIndex = 2; cellIndex < headerRow.getPhysicalNumberOfCells(); cellIndex++) {
            String columnName = headerRow.getCell(cellIndex).getStringCellValue();
            mapOfIndexToColumnName.put(cellIndex, columnName);
            mapOfColumnNameToTotalUtilRate.put(columnName, 0.0);
        }

        // Get all rows regarding IBIS.
        ArrayList<Row> ibisRows = new ArrayList<Row>();
        for (int rowIndex = 1; rowIndex < utilSheet.getPhysicalNumberOfRows(); rowIndex++) {
            Row currentRow = utilSheet.getRow(rowIndex);
            String platformType = currentRow.getCell(0).getStringCellValue();

            if (platformType.equals("IBIS")) {
                ibisRows.add(currentRow);
            }
        }

        // Get the average utilization rate for each column
        for (Row ibisRow : ibisRows) {
            // Increment the counts for each row.
            for (Integer index: mapOfIndexToColumnName.keySet()) {
                String columnName = mapOfIndexToColumnName.get(index);
                Double cellValue = ibisRow.getCell(index).getNumericCellValue();
                Double currentUtilRate = mapOfColumnNameToTotalUtilRate.get(columnName);
                mapOfColumnNameToTotalUtilRate.put(columnName, currentUtilRate + cellValue);
            }
        }
        HashMap<String, Double> mapOfColumnNameToAveragelUtilRate = new HashMap<String, Double>();
        for (String columnName: mapOfColumnNameToTotalUtilRate.keySet()) {
            Double rateSum = mapOfColumnNameToTotalUtilRate.get(columnName);
            if (rateSum > 0) { // Remove 0 entries
                Double averageRate = rateSum / ibisRows.size();
                mapOfColumnNameToAveragelUtilRate.put(columnName, averageRate);
            }
        }

        // Save the IBIS Utilization data in a new sheet
        String IBIS_UTIL_SHEET_NAME = "IBIS AVG UTIL SUMMARY";
        if (workbook.getSheet(IBIS_UTIL_SHEET_NAME) != null) {
            workbook.removeSheetAt(workbook.getSheetIndex(IBIS_UTIL_SHEET_NAME));
        }
        Sheet summaryStats = workbook.createSheet(IBIS_UTIL_SHEET_NAME);
        int rowIndex = 0;
        for (String columnName: mapOfColumnNameToAveragelUtilRate.keySet()) {
            Row row = summaryStats.createRow(rowIndex);

            // Write column name
            Cell cell = row.createCell(0, CellType.STRING);
            cell.setCellValue(columnName);

            // Write Averages util rate
            cell = row.createCell(1, CellType.NUMERIC);
            cell.setCellValue(mapOfColumnNameToAveragelUtilRate.get(columnName));

            rowIndex ++;
        }
        // End of section on IBIS Oven =================================================================================

        // Get Product throughput ======================================================================================
        Sheet dailyThroughputSheet = workbook.getSheet(DAILY_THROUGHPUT_RES_REP);

        // Get output data




        // Saves the current edited workbook by overwriting the original file
        FileOutputStream outputStream = new FileOutputStream(outputExcelFilePath);
        workbook.write(outputStream);
        outputStream.close();

        // Perform closing operations
        workbook.close();
        tempOutputFile.delete();

    }

    public static void copyFileUsingStream(File source, File destination) throws IOException {
        try (InputStream is = new FileInputStream(source); OutputStream os = new FileOutputStream(destination)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        }
    }

}
