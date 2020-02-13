package com.nusinfineon.core;

import org.apache.poi.ss.usermodel.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class OutputAnalysisCore {


    public static void main(String[] args) throws IOException {
        // accessOriginalInputFile("C:\\Users\\Ahmad\\Documents\\Workspace\\playground\\src\\main\\resources\\one_level_input_original.xlsx");
        //getSheetNameAndColumnsFromExcelFile("C:\\Users\\Ahmad\\Documents\\Workspace\\playground\\src\\main\\resources\\output_min_size_22.xlsx");

        getOutputSummaryStatistics("C:\\Users\\Ahmad\\Documents\\Workspace\\playground\\src\\main\\resources\\output_min_size_22.xlsx",
                "C:\\Users\\Ahmad\\Documents\\Workspace\\playground\\src\\main\\resources\\product_key_cost.xlsx");
    }

    public static void getOutputSummaryStatistics(String outputExcelFilePath, String productKeyCostExcelFilePath) throws IOException {

        // Creates a temp excel file for referencing
        File originalInputFile = new File(outputExcelFilePath);
        File tempOutputFile = new File(outputExcelFilePath + "temp.xlsx");
        copyFileUsingStream(originalInputFile, tempOutputFile);

        if (!tempOutputFile.exists()) {
            throw new IOException("File not found in: " + outputExcelFilePath);
        }

        Workbook workbook = WorkbookFactory.create(tempOutputFile);

        // ==================   Get average utilization rates of IBIS Ovens ============================================
        final String UTIL_RES_REP = "Util Res Rep";
        Sheet utilSheet = workbook.getSheet(UTIL_RES_REP);
        if (utilSheet == null) {
            throw new IOException("Excel file doesn't contain sheet: " + UTIL_RES_REP);
        }
        HashMap<String, Double> hashMapOfAverageUtilizationRates = calculateAverageUtilRateHashMap(utilSheet);
        saveHashMapToNewSheet("IBIS AVG UTIL SUMMARY", hashMapOfAverageUtilizationRates, workbook);

        // =========================== End of section on IBIS Oven utilization rates ===================================

        // =============================== Get Summary of Daily Throughput =============================================
        final String DAILY_THROUGHPUT_RES_REP = "Daily Throughput Res Rep";
        Sheet dailyThroughputSheet = workbook.getSheet(DAILY_THROUGHPUT_RES_REP);
        if (dailyThroughputSheet == null) {
            throw new IOException("Excel file doesn't contain sheet: " + DAILY_THROUGHPUT_RES_REP);
        }
        HashMap<String, Double> hashMapOfSummarizedDailyThroughput = calculateDailyThroughputHashMap(dailyThroughputSheet);
        saveHashMapToNewSheet("TOTAL THROUGHPUT FROM DAILY", hashMapOfSummarizedDailyThroughput, workbook);

        // =========================== End of section on Summarizing Daily Throughput ==================================

        // =============================== Get Statistics on Cycle Time of Products ====================================
        final String THROUGHPUT_PRODUCT_REP = "Throughput Product Rep";
        final String STAYTIME_AVERAGE_COLUMN = "StayTime Average (hr)";
        final String STAYTIME_MIN_COLUMN = "StayTime Min (hr)";
        final String STAYTIME_MAX_COLUMN = "StayTime Max (hr)";

        Sheet cycleTimeSheet = workbook.getSheet(THROUGHPUT_PRODUCT_REP);

        // Get index of column names and initialize counts
        HashMap<String, Integer> mapOfStayTimeToIndex = new HashMap<String, Integer>();
        HashMap<String, Long> mapOfStayTimeToCounts = new HashMap<String, Long>();
        Row headerRow = cycleTimeSheet.getRow(0);
        for (int cellIndex = 0; cellIndex < headerRow.getPhysicalNumberOfCells(); cellIndex ++) {
            String cellValue = headerRow.getCell(cellIndex).getStringCellValue();
            switch (cellValue) {
                case STAYTIME_AVERAGE_COLUMN:
                    mapOfStayTimeToIndex.put(STAYTIME_AVERAGE_COLUMN, cellIndex);
                    mapOfStayTimeToCounts.put(STAYTIME_AVERAGE_COLUMN, Long.valueOf(0));
                    break;
                case STAYTIME_MIN_COLUMN:
                    mapOfStayTimeToIndex.put(STAYTIME_MIN_COLUMN, cellIndex);
                    mapOfStayTimeToCounts.put(STAYTIME_MIN_COLUMN, Long.valueOf(0));
                    break;
                case STAYTIME_MAX_COLUMN:
                    mapOfStayTimeToIndex.put(STAYTIME_MAX_COLUMN, cellIndex);
                    mapOfStayTimeToCounts.put(STAYTIME_MAX_COLUMN, Long.valueOf(0));
                    break;
                default:
                    break;
            } // End of switch case block
        } // End of for loop block

        // Iterate through all rows and get the counts
        for (int rowIndex = 1; rowIndex < cycleTimeSheet.getPhysicalNumberOfRows(); rowIndex ++) {
            Row currentRow = cycleTimeSheet.getRow(rowIndex);

            // Populate counts
            for (String columnName: mapOfStayTimeToIndex.keySet()) {
                int columnIndex = mapOfStayTimeToIndex.get(columnName);
                long cellValue = (long) (currentRow.getCell(columnIndex).getNumericCellValue());
                long currentCount = mapOfStayTimeToCounts.get(columnName);
                mapOfStayTimeToCounts.put(columnName, currentCount + cellValue);
            }
        }

        // Average out the Stay-Cycle Time
        HashMap<String, Double> mapOfStayTimeToAverageStayTime = new HashMap<String, Double>();
        for (String cycleTime: mapOfStayTimeToCounts.keySet()) {
            long currentValue = mapOfStayTimeToCounts.get(cycleTime);
            Double averageCycleTime = (double) currentValue / (double) (cycleTimeSheet.getPhysicalNumberOfRows() - 1);
            mapOfStayTimeToAverageStayTime.put(cycleTime, averageCycleTime);
        }

        // Save the Throughput data in a new sheet
        String CYCLE_TIME_SHEET_NAME = "AVERAGE CYCLE TIME";
        if (workbook.getSheet(CYCLE_TIME_SHEET_NAME) != null) {
            workbook.removeSheetAt(workbook.getSheetIndex(CYCLE_TIME_SHEET_NAME));
        }
        Sheet summaryCycleTime = workbook.createSheet(CYCLE_TIME_SHEET_NAME);
        int rowIndex = 0;
        for (String columnName: mapOfStayTimeToAverageStayTime.keySet()) {
            Row row = summaryCycleTime.createRow(rowIndex);

            // Write column name
            Cell cell = row.createCell(0, CellType.STRING);
            cell.setCellValue(columnName);

            // Write Averaged util rate
            cell = row.createCell(1, CellType.NUMERIC);
            cell.setCellValue(mapOfStayTimeToAverageStayTime.get(columnName));

            rowIndex ++;
        }

        // =============================== End of Cycle Time Calculation ===============================================

        // =============================== Get value of throughput =====================================================

        // Read from product-key-cost table and store data
        File productCostFile = new File(productKeyCostExcelFilePath);
        Workbook productCostWorkbook = WorkbookFactory.create(productCostFile);
        Sheet mainCostSheet = productCostWorkbook.getSheetAt(0);
        HashMap<String, Double> mapOfProductToCost = new HashMap<String, Double>();
        for (rowIndex = 1; rowIndex < mainCostSheet.getPhysicalNumberOfRows(); rowIndex ++) {
            Row row = mainCostSheet.getRow(rowIndex);
            String productKey = row.getCell(1).getStringCellValue();
            Double productCost = row.getCell(2).getNumericCellValue();
            mapOfProductToCost.put(productKey, productCost);
        }
        productCostWorkbook.close();

        // Get Counts of Product that has been processed / outputted
        final String DAILY_THROUGHPUT_PRODUCT_REP = "Daily Throughput Product Rep";
        final String QTY_OUT_COLUMN = "Qty Out";
        final String PRODUCT_COLUMN = "Product";

        Sheet dailyProductThroughputSheet = workbook.getSheet(DAILY_THROUGHPUT_PRODUCT_REP);

        // Get column index for QTY_OUT_COLUMN index
        int qtyOutColumnIndex = -1; int productColumnIndex = -1;
        headerRow = dailyProductThroughputSheet.getRow(0);
        for (int cellIndex = 0; cellIndex < headerRow.getPhysicalNumberOfCells(); cellIndex ++) {
            String cellValue = headerRow.getCell(cellIndex).getStringCellValue();
            switch (cellValue) {
                case QTY_OUT_COLUMN:
                    qtyOutColumnIndex = cellIndex;
                    break;
                case PRODUCT_COLUMN:
                    productColumnIndex = cellIndex;
                    break;
                default:
                    break;
            } // End of switch case block
        } // End of for loop block

        // Get unique counts for each product
        HashMap<String, Double> mapOfProductOutCounts = new HashMap<String, Double>();
        for (rowIndex = 1; rowIndex < dailyProductThroughputSheet.getPhysicalNumberOfRows(); rowIndex++) {
            Row row = dailyProductThroughputSheet.getRow(rowIndex);
            if (row.getCell(productColumnIndex) != null) {
                String productKey = row.getCell(productColumnIndex).getStringCellValue().trim();
                Double productQty = row.getCell(qtyOutColumnIndex).getNumericCellValue();

                if (mapOfProductOutCounts.containsKey(productKey)) {
                    // Update entry
                    Double currentQty = mapOfProductOutCounts.get(productKey);
                    mapOfProductOutCounts.put(productKey, currentQty + productQty);
                } else {
                    // Create a new entry
                    mapOfProductOutCounts.put(productKey, productQty);
                }
            }
        } // End of fot loop

        // Get the total worth of products outputted.
        Double totalWorth = 0.0;
        for (String productKey : mapOfProductOutCounts.keySet()) {
            Double productCost = 0.0;
            // If an associated cost exists
            if (mapOfProductToCost.containsKey(productKey)) {
                productCost = mapOfProductToCost.get(productKey);
            } else {
                productCost = getAverageCostOfProductHashMap(mapOfProductToCost);
            }

            Double productWorth = productCost * mapOfProductOutCounts.get(productKey);
            totalWorth += productWorth;
        }

        // Save the product worth data in a new sheet
        String TOTAL_WORTH_SHEET_NAME = "TOTAL WORTH";
        if (workbook.getSheet(TOTAL_WORTH_SHEET_NAME) != null) {
            workbook.removeSheetAt(workbook.getSheetIndex(TOTAL_WORTH_SHEET_NAME));
        }
        Sheet summaryTotalWorth = workbook.createSheet(TOTAL_WORTH_SHEET_NAME);
        Row row = summaryTotalWorth.createRow(0);
        Cell cell = row.createCell(0, CellType.STRING);
        cell.setCellValue("TOTAL WORTH");
        cell = row.createCell(1, CellType.NUMERIC);
        cell.setCellValue(totalWorth);

        // Saves the current edited workbook by overwriting the original file
        FileOutputStream outputStream = new FileOutputStream(outputExcelFilePath);
        workbook.write(outputStream);
        outputStream.close();

        // Perform closing operations
        workbook.close();
        tempOutputFile.delete();

    }

    /**
     *
     *  Logic: Looks at the input and output for the whole factory.
     *  Input = "Load/Burn In/Transfer Normal Qty" + "Load/Burn In/Transfer YRTP Qty"
     *  Output = "Unload Normal Qty" + "Unload YRTP Qty"
     *
     * @param dailyThroughputSheet
     * @return
     */
    public static HashMap<String, Double> calculateDailyThroughputHashMap(Sheet dailyThroughputSheet) {

        final String LOAD_NORMAL_COLUMN = "Load/Burn In/Transfer Normal Qty";
        final String LOAD_YRTP_COLUMN = "Load/Burn In/Transfer YRTP Qty";
        final String UNLOAD_NORMAL_COLUMN = "Unload Normal Qty";
        final String UNLOAD_YRTP_COLUMN = "Unload YRTP Qty";

        // Get index of column names and initialize counts
        HashMap<String, Integer> mapOfThroughputToIndex = new HashMap<String, Integer>();
        HashMap<String, Long> mapOfThroughputToCounts = new HashMap<String, Long>();
        Row headerRow = dailyThroughputSheet.getRow(0);
        for (int cellIndex = 0; cellIndex < headerRow.getPhysicalNumberOfCells(); cellIndex ++) {
            String cellValue = headerRow.getCell(cellIndex).getStringCellValue();
            switch (cellValue) {
                case LOAD_NORMAL_COLUMN:
                    mapOfThroughputToIndex.put(LOAD_NORMAL_COLUMN, cellIndex);
                    mapOfThroughputToCounts.put(LOAD_NORMAL_COLUMN, Long.valueOf(0));
                    break;
                case LOAD_YRTP_COLUMN:
                    mapOfThroughputToIndex.put(LOAD_YRTP_COLUMN, cellIndex);
                    mapOfThroughputToCounts.put(LOAD_YRTP_COLUMN, Long.valueOf(0));
                    break;
                case UNLOAD_NORMAL_COLUMN:
                    mapOfThroughputToIndex.put(UNLOAD_NORMAL_COLUMN, cellIndex);
                    mapOfThroughputToCounts.put(UNLOAD_NORMAL_COLUMN, Long.valueOf(0));
                    break;
                case UNLOAD_YRTP_COLUMN:
                    mapOfThroughputToIndex.put(UNLOAD_YRTP_COLUMN, cellIndex);
                    mapOfThroughputToCounts.put(UNLOAD_YRTP_COLUMN, Long.valueOf(0));
                    break;
                default:
                    break;
            } // End of switch case block
        } // End of for loop block

        // Iterate through all rows and get the counts
        for (int rowIndex = 1; rowIndex < dailyThroughputSheet.getPhysicalNumberOfRows(); rowIndex ++) {
            Row currentRow = dailyThroughputSheet.getRow(rowIndex);

            // Populate counts
            for (String columnName: mapOfThroughputToIndex.keySet()) {
                int columnIndex = mapOfThroughputToIndex.get(columnName);
                long cellValue = (long) (currentRow.getCell(columnIndex).getNumericCellValue());
                long currentCount = mapOfThroughputToCounts.get(columnName);
                mapOfThroughputToCounts.put(columnName, currentCount + cellValue);
            }
        }

        // Sum up to input and output
        HashMap<String, Double> mapOfInputAndOutput = new HashMap<String, Double>();
        mapOfInputAndOutput.put("TOTAL INPUT", (double) mapOfThroughputToCounts.get(LOAD_NORMAL_COLUMN)
                + mapOfThroughputToCounts.get(LOAD_YRTP_COLUMN));
        mapOfInputAndOutput.put("TOTAL OUTPUT", (double) mapOfThroughputToCounts.get(UNLOAD_NORMAL_COLUMN)
                + mapOfThroughputToCounts.get(UNLOAD_YRTP_COLUMN));

        return mapOfInputAndOutput;
    }

    /**
     * Obtains a hash map of Average Utilization Rates of IBIS Oven.
     *
     *  Logic: Extract Column Headers first. Then, filter by IBIS rows and sum up the cells corresponding to each
     *  header. Lastly, average out the numbers and only extract "averaged values" > 0.
     *
     * @param utilizationSheet Excel sheet that has the IBIS Utilization rates.
     * @return A hash map of utilization categories to their respective rates.
     * @throws IOException
     */
    public static HashMap<String, Double> calculateAverageUtilRateHashMap(Sheet utilizationSheet) {

        // Get column names and corresponding index. Assumes first 2 columns are "platform" and "name"
        HashMap<Integer, String> mapOfIndexToColumnName = new HashMap<Integer, String>();
        HashMap<String, Double> mapOfColumnNameToTotalUtilRate = new HashMap<String, Double>();
        Row headerRow = utilizationSheet.getRow(0);
        for (int cellIndex = 2; cellIndex < headerRow.getPhysicalNumberOfCells(); cellIndex++) {
            String columnName = headerRow.getCell(cellIndex).getStringCellValue();
            mapOfIndexToColumnName.put(cellIndex, columnName);
            mapOfColumnNameToTotalUtilRate.put(columnName, 0.0);
        }

        // Get all rows regarding IBIS.
        ArrayList<Row> ibisRows = new ArrayList<Row>();
        for (int rowIndex = 1; rowIndex < utilizationSheet.getPhysicalNumberOfRows(); rowIndex++) {
            Row currentRow = utilizationSheet.getRow(rowIndex);
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

        return mapOfColumnNameToAveragelUtilRate;
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
     * Calculates the average cost (value) of a String-Double hash map.
     * @param mapOfProductToCost Hash map of product to cost.
     * @return Double.
     */
    public static Double getAverageCostOfProductHashMap(HashMap<String, Double> mapOfProductToCost) {
        Double sum = 0.0;
        for (String product: mapOfProductToCost.keySet()) {
            sum += mapOfProductToCost.get(product);
        }
        return (sum / mapOfProductToCost.size());
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

