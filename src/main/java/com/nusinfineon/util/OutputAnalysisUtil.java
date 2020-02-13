package com.nusinfineon.util;

import org.apache.poi.ss.usermodel.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class OutputAnalysisUtil {

    /**
     * Creates a new sheet inside the given excel workbook. Will write values from the provided hash map to the sheet.
     * @param sheetName Name of the new sheet to save the data in.
     * @param hashMapOfValuesToWrite Hash map of String to Double values to write.
     * @param excelWorkbook Workbook to write data to.
     */
    public static void saveHashMapToNewSheet(String sheetName, HashMap<String, Double> hashMapOfValuesToWrite,
                                             Workbook excelWorkbook) {
        // Deletes sheet if it already exists
        if (excelWorkbook.getSheet(sheetName) != null) {
            excelWorkbook.removeSheetAt(excelWorkbook.getSheetIndex(sheetName));
        }

        Sheet sheetToWrite = excelWorkbook.createSheet(sheetName);

        int rowIndex = 0;

        for (String columnName: hashMapOfValuesToWrite.keySet()) {
            Row row = sheetToWrite.createRow(rowIndex);

            // Write column name
            Cell cell = row.createCell(0, CellType.STRING);
            cell.setCellValue(columnName);

            // Write corresponding value
            cell = row.createCell(1, CellType.NUMERIC);
            cell.setCellValue(hashMapOfValuesToWrite.get(columnName));

            rowIndex ++;
        }

    }

    /**
     * Converts a hashmap of string-long to string-double.
     * @param hashMapOfLongValues Hash map of string-long values.
     * @return Hash map of string-double values.
     */
    public static HashMap<String, Double> convertHashMapWithLongValueToDouble(HashMap<String, Long> hashMapOfLongValues) {
        HashMap<String, Double> hashMapOfDoubleValues = new HashMap<String, Double>();

        for (String s: hashMapOfLongValues.keySet()) {
            Double value = (double) hashMapOfLongValues.get(s);

            hashMapOfDoubleValues.put(s, value);
        }

        return hashMapOfDoubleValues;
    }

    /**
     * Copies an input file to a destination file via byte stream.
     * @param source The source file to be copied.
     * @param destination The destination file to contain the source file. Can be empty.
     * @throws IOException
     */
    public static void copyFileUsingStream(File source, File destination) throws IOException {
        try (InputStream is = new FileInputStream(source); OutputStream os = new FileOutputStream(destination)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        }
    }

    /**
     * Helper function to print all sheets and the corresponding headers in a single excel workbook.
     * @param excelFilePath String representing the filepath pointing to an excel workbook.
     * @throws IOException
     */
    public static void getSheetNameAndColumnsFromExcelFile(String excelFilePath) throws IOException {
        File excelFile = new File(excelFilePath);

        if (!excelFile.exists()) {
            throw new IOException("File not found in: " + excelFilePath);
        }

        Workbook workbook = WorkbookFactory.create(excelFile);

        HashMap<String, ArrayList<String>> mapOfSheetToColumns = new HashMap<>();

        for (Sheet sheet: workbook) {
            ArrayList<String> columnNames = new ArrayList<String>();
            Row firstRow = sheet.getRow(0);

            for (int cellIndex = 0; cellIndex < firstRow.getPhysicalNumberOfCells(); cellIndex ++) {
                Cell cell = firstRow.getCell(cellIndex);
                String columnName = cell.getStringCellValue();
                columnNames.add(columnName);
            }
            mapOfSheetToColumns.put(sheet.getSheetName(), columnNames);
        }

        for (String sheetName : mapOfSheetToColumns.keySet() ) {
            System.out.println("\nSheet name: " + sheetName);
            for (String columnName : mapOfSheetToColumns.get(sheetName) ) {
                System.out.println(columnName);
            }
        }
        workbook.close();
    }

    /**
     * Helper method to print all sheets available in an Excel workbook.
     * @param excelFilePath String representing the filepath pointing to an excel workbook.
     * @throws IOException
     */
    public static void accessOriginalInputFile(String excelFilePath) throws IOException {
        File excelFile = new File(excelFilePath);

        if (!excelFile.exists()) {
            throw new IOException("File not found in: " + excelFilePath);
        }

        Workbook workbook = WorkbookFactory.create(excelFile);

        ArrayList<String> sheetNames = new ArrayList<String>();

        for (Sheet sheet: workbook) {
            sheetNames.add(sheet.getSheetName());
        }

        sheetNames.sort(String::compareToIgnoreCase);

        for (String s: sheetNames) {
            System.out.println(s);
        }
        workbook.close();
    }



}


