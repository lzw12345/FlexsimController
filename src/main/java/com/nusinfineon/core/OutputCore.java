package com.nusinfineon.core;

import org.apache.poi.ss.usermodel.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class OutputCore {
    public static void main(String[] args) throws  IOException {
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
        /*
        Logic: Extract Column Headers first. Then, filter by IBIS rows and sum up the cells corresponding to each
        header. Lastly, average out the numbers and only extract "averaged values" > 0.
         */
        final String UTIL_RES_REP = "Util Res Rep";

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
        Sheet summaryUtilization = workbook.createSheet(IBIS_UTIL_SHEET_NAME);
        int rowIndex = 0;
        for (String columnName: mapOfColumnNameToAveragelUtilRate.keySet()) {
            Row row = summaryUtilization.createRow(rowIndex);

            // Write column name
            Cell cell = row.createCell(0, CellType.STRING);
            cell.setCellValue(columnName);

            // Write Averaged util rate
            cell = row.createCell(1, CellType.NUMERIC);
            cell.setCellValue(mapOfColumnNameToAveragelUtilRate.get(columnName));

            rowIndex ++;
        }
        // =========================== End of section on IBIS Oven utilization rates ===================================

        // =============================== Get Product throughput ======================================================
        /*
        Logic: Looks at the input and output for the whole factory.
        Input = "Load/Burn In/Transfer Normal Qty" + "Load/Burn In/Transfer YRTP Qty"
        Output = "Unload Normal Qty" + "Unload YRTP Qty"
         */
        final String DAILY_THROUGHPUT_RES_REP = "Daily Throughput Res Rep";
        final String LOAD_NORMAL_COLUMN = "Load/Burn In/Transfer Normal Qty";
        final String LOAD_YRTP_COLUMN = "Load/Burn In/Transfer YRTP Qty";
        final String UNLOAD_NORMAL_COLUMN = "Unload Normal Qty";
        final String UNLOAD_YRTP_COLUMN = "Unload YRTP Qty";

        Sheet dailyThroughputSheet = workbook.getSheet(DAILY_THROUGHPUT_RES_REP);

        // Get index of column names and initialize counts
        HashMap<String, Integer> mapOfThroughputToIndex = new HashMap<String, Integer>();
        HashMap<String, Long> mapOfThroughputToCounts = new HashMap<String, Long>();
        headerRow = dailyThroughputSheet.getRow(0);
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
        for (rowIndex = 1; rowIndex < dailyThroughputSheet.getPhysicalNumberOfRows(); rowIndex ++) {
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
        HashMap<String, Long> mapOfInputAndOutput = new HashMap<String, Long>();
        mapOfInputAndOutput.put("TOTAL INPUT", mapOfThroughputToCounts.get(LOAD_NORMAL_COLUMN)
                + mapOfThroughputToCounts.get(LOAD_YRTP_COLUMN));
        mapOfInputAndOutput.put("TOTAL OUTPUT", mapOfThroughputToCounts.get(UNLOAD_NORMAL_COLUMN)
                + mapOfThroughputToCounts.get(UNLOAD_YRTP_COLUMN));

        // Save the Throughput data in a new sheet
        String THROUGHPUT_SHEET_NAME = "TOTAL THROUGHPUT";
        if (workbook.getSheet(THROUGHPUT_SHEET_NAME) != null) {
            workbook.removeSheetAt(workbook.getSheetIndex(THROUGHPUT_SHEET_NAME));
        }
        Sheet summaryThroughput = workbook.createSheet(THROUGHPUT_SHEET_NAME);
        rowIndex = 0;
        for (String columnName: mapOfInputAndOutput.keySet()) {
            Row row = summaryThroughput.createRow(rowIndex);

            // Write column name
            Cell cell = row.createCell(0, CellType.STRING);
            cell.setCellValue(columnName);

            // Write Averaged util rate
            cell = row.createCell(1, CellType.NUMERIC);
            cell.setCellValue(mapOfInputAndOutput.get(columnName));

            rowIndex ++;
        }

        // =========================== End of section on Factory Throughput ============================================

        // =============================== Get Statistics on Cycle Time of Products ====================================
        final String THROUGHPUT_PRODUCT_REP = "Throughput Product Rep";
        final String STAYTIME_AVERAGE_COLUMN = "StayTime Average (hr)";
        final String STAYTIME_MIN_COLUMN = "StayTime Min (hr)";
        final String STAYTIME_MAX_COLUMN = "StayTime Max (hr)";

        Sheet cycleTimeSheet = workbook.getSheet(THROUGHPUT_PRODUCT_REP);

        // Get index of column names and initialize counts
        HashMap<String, Integer> mapOfStayTimeToIndex = new HashMap<String, Integer>();
        HashMap<String, Long> mapOfStayTimeToCounts = new HashMap<String, Long>();
        headerRow = cycleTimeSheet.getRow(0);
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
        for (rowIndex = 1; rowIndex < cycleTimeSheet.getPhysicalNumberOfRows(); rowIndex ++) {
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
        rowIndex = 0;
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

    public static Double getAverageCostOfProductHashMap(HashMap<String, Double> mapOfProductToCost) {
        Double sum = 0.0;
        for (String product: mapOfProductToCost.keySet()) {
            sum += mapOfProductToCost.get(product);
        }
        return (sum / mapOfProductToCost.size());
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
