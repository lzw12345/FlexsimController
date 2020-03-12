package com.nusinfineon.core;

import com.nusinfineon.exceptions.CustomException;
import com.nusinfineon.util.OutputAnalysisCalculation;
import jdk.javadoc.internal.doclets.toolkit.util.DocFinder;
import org.apache.poi.ss.usermodel.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.logging.Logger;

import com.nusinfineon.util.OutputAnalysisUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class OutputAnalysisCore {

    private final static Logger LOGGER = Logger.getLogger(OutputAnalysisCore.class.getName());

    public static void main(String[] args) throws IOException, CustomException {

        // =============== Tests on the whole folder ===================================================================
        File folderDirectory = new File("src/main/resources/sample-output-files/output-files-with-summary-data");

        // Generate output statistics for all excel files in a folder
        //appendSummaryStatisticsOfFolderOFExcelFiles(folderDirectory);

        // Generate the tableau excel file from the folder of excel files (with output data appended)
        generateExcelTableauFile(folderDirectory);


        // ======= Tests on a single excel file ========================================================================
        //File excelFile = new File("C:\\Users\\Ahmad\\Documents\\Workspace\\flexsim-controller\\src\\main\\resources\\sample-output-files\\output-files-with-summary-data\\output_min_size_20.xlsx");
        //appendSummaryStatisticsOfSingleOutputExcelFile(excelFile);
    }

    /**
     * Generates a single excel file to be used with Tableau. Summarizes the output file data for each excel file.
     * Saves the file into "src/main/resources/sample-output-files/tableau-excel-file/tableau-excel-file.xlsx"
     */
    public static void generateExcelTableauFile(File folderOfExcelFiles) throws IOException, CustomException {
        LOGGER.info("Starting generateExcelTableauFile method");

        // Generate a list of excel files from the folder
        if (!folderOfExcelFiles.isDirectory()) {
            throw new CustomException("Argument for generateExcelTableauFile() method is not a folder");
        }

        ArrayList<File> excelFiles = new ArrayList<File>();
        for (File file: folderOfExcelFiles.listFiles()) {
            if (file.exists() && (!file.isDirectory())) {
                excelFiles.add(file);
            }
        }
        LOGGER.info("No. of excel files to process: " + excelFiles.size());

        // Create the destination excel file
        final File destinationFile = new File("src/main/resources/sample-output-files/tableau-excel-file/tableau-excel-file.xlsx");
        if (!destinationFile.exists()) {
            // Delete if there is a file present
            destinationFile.createNewFile();
        }

        // Create the summary workbook
        Workbook destinationWorkbook = new XSSFWorkbook();

        // Create the summary utilization sheet ========================================================================

        // Create the sheet and write column names
        Sheet destinationUtilizationSheet = destinationWorkbook.createSheet("IBIS_UTILIZATION");

        // Write column headers
        final String[] UTILIZATION_COLUMN_HEADERS = {"Run Type", "Idle Rate", "Processing Rate", "Setup Rate",
                "Waiting for Operator Rate", "Waiting for Transporter Rate"};
        Row headerRow = destinationUtilizationSheet.createRow(0);
        for (int i = 0; i < UTILIZATION_COLUMN_HEADERS.length; i++) {
            Cell cell = headerRow.createCell(i, CellType.STRING);
            cell.setCellValue(UTILIZATION_COLUMN_HEADERS[i]);
        }

        // Store the headers of the excel file containing the summarized rates
        ArrayList<String> SOURCE_UTILIZATION_COLUMN_HEADERS = new ArrayList<>(Arrays.asList("RUN_TYPE", "UTILIZATION_RATE_IDLE",
                "UTILIZATION_RATE_PROCESSING", "UTILIZATION_RATE_SETUP", "UTILIZATION_RATE_WAITING FOR OPERATOR",
                "UTILIZATION_RATE_WAITING FOR TRANSPORTER"));


        // Iterate through each excel file and write the utilization data
        final String SOURCE_UTILIZATION_SHEET = "RUN_TYPE_AND_IBIS_UTILIZATION";
        int destinationRowCount = 1;
        for (File excelFile: excelFiles) {
            Workbook sourceWorkbook = WorkbookFactory.create(excelFile);
            Sheet sourceUtilizationSheet = sourceWorkbook.getSheet(SOURCE_UTILIZATION_SHEET);
            Row sourceRow = sourceUtilizationSheet.getRow(1);

            // Get index corresponding with column headers
            headerRow = sourceUtilizationSheet.getRow(0);
            HashMap<String, Integer> mapOfUtilColumnHeaders = OutputAnalysisUtil.getMappingOfHeadersToIndex(headerRow, SOURCE_UTILIZATION_COLUMN_HEADERS);

            // Write the data to a new row
            OutputAnalysisUtil.writeUtilizationRate(destinationUtilizationSheet, sourceRow, mapOfUtilColumnHeaders, destinationRowCount);

            sourceWorkbook.close();
            destinationRowCount = destinationRowCount + 1;
        }

        // End of writing utilization rates to summary workbook

        // Create the Stay Time Sheet ==================================================================================
        Sheet destinationStayTimeSheet = destinationWorkbook.createSheet("STAY_TIME");

        // Write column headers
        final String[] STAY_TIME_COLUMN_HEADERS = {"Run Type", "Stay Time", "Product ID"};
        headerRow = destinationStayTimeSheet.createRow(0);
        for (int i = 0; i < STAY_TIME_COLUMN_HEADERS.length; i++) {
            Cell cell = headerRow.createCell(i, CellType.STRING);
            cell.setCellValue(STAY_TIME_COLUMN_HEADERS[i]);
        }

        // Iterate through the excel files and write the stay time data
        final String SOURCE_STAYTIME_SHEET = "PRODUCT_STAY_TIME";
        final int STAY_TIME_PRODUCT_ID_COLUMN_INDEX = 0;
        final int STAY_TIME_PRODUCT_STAYIME_INDEX = 1;

        destinationRowCount = 1;
        for (File excelFile: excelFiles) {
            Workbook sourceWorkbook = WorkbookFactory.create(excelFile);
            Sheet sourceStaytimeSheet = sourceWorkbook.getSheet(SOURCE_STAYTIME_SHEET);
            String runType = OutputAnalysisUtil.fileStringToFileName(excelFile.toString());

            // Iterate through all rows and insert to the new sheet
            for (int i = 1; i < sourceStaytimeSheet.getPhysicalNumberOfRows(); i++) {
                Row sourceRow = sourceStaytimeSheet.getRow(i);

                Cell productCell = sourceRow.getCell(STAY_TIME_PRODUCT_ID_COLUMN_INDEX);
                Cell staytimeCell = sourceRow.getCell(STAY_TIME_PRODUCT_STAYIME_INDEX);

                if (productCell != null) {
                    // Extract values
                    String productId = productCell.getStringCellValue();
                    Double productStayTime = staytimeCell.getNumericCellValue();

                    // Write to the destination sheet
                    Row newStaytimeRow = destinationStayTimeSheet.createRow(destinationRowCount);

                    Cell destinationRuntypeCell = newStaytimeRow.createCell(0, CellType.STRING);
                    destinationRuntypeCell.setCellValue(runType);

                    Cell destinationStayTimeCell = newStaytimeRow.createCell(1, CellType.NUMERIC);
                    destinationStayTimeCell.setCellValue(productStayTime);

                    Cell destinationProductCell = newStaytimeRow.createCell(2, CellType.STRING);
                    destinationProductCell.setCellValue(productId);

                    destinationRowCount = destinationRowCount + 1;
                }

            }

            sourceWorkbook.close();
        }

        // End of writing stay time to sheet

        // Create the Time in System Sheet ==================================================================================
        Sheet destinationTimeInSystemSheet = destinationWorkbook.createSheet("TIME_IN_SYSTEM");

        // Write column headers
        final String[] TIME_IN_SYSTEM_COLUMN_HEADERS = {"Run Type", "Stay Time", "Product ID"};
        headerRow = destinationTimeInSystemSheet.createRow(0);
        for (int i = 0; i < TIME_IN_SYSTEM_COLUMN_HEADERS.length; i++) {
            Cell cell = headerRow.createCell(i, CellType.STRING);
            cell.setCellValue(TIME_IN_SYSTEM_COLUMN_HEADERS[i]);
        }

        // Iterate through the excel files and write the stay time data
        final String SOURCE_TIME_IN_SYSTEM_SHEET = "PRODUCT_TIME_IN_SYSTEM";
        final int TIME_IN_SYSTEM_PRODUCT_ID_COLUMN_INDEX = 0;
        final int TIME_IN_SYSTEM_PRODUCT_STAYIME_INDEX = 1;

        destinationRowCount = 1;
        for (File excelFile: excelFiles) {
            Workbook sourceWorkbook = WorkbookFactory.create(excelFile);
            Sheet sourceTimeInSystemSheet = sourceWorkbook.getSheet(SOURCE_TIME_IN_SYSTEM_SHEET);
            String runType = OutputAnalysisUtil.fileStringToFileName(excelFile.toString());

            // Iterate through all rows and insert to the new sheet
            for (int i = 1; i < sourceTimeInSystemSheet.getPhysicalNumberOfRows(); i++) {
                Row sourceRow = sourceTimeInSystemSheet.getRow(i);

                Cell productCell = sourceRow.getCell(TIME_IN_SYSTEM_PRODUCT_ID_COLUMN_INDEX);
                Cell staytimeCell = sourceRow.getCell(TIME_IN_SYSTEM_PRODUCT_STAYIME_INDEX);

                if (productCell != null) {
                    // Extract values
                    String productId = productCell.getStringCellValue();
                    Double productStayTime = staytimeCell.getNumericCellValue();

                    // Write to the destination sheet
                    Row newTimeInSystemRow = destinationTimeInSystemSheet.createRow(destinationRowCount);

                    Cell destinationRuntypeCell = newTimeInSystemRow.createCell(0, CellType.STRING);
                    destinationRuntypeCell.setCellValue(runType);

                    Cell destinationStayTimeCell = newTimeInSystemRow.createCell(1, CellType.NUMERIC);
                    destinationStayTimeCell.setCellValue(productStayTime);

                    Cell destinationProductCell = newTimeInSystemRow.createCell(2, CellType.STRING);
                    destinationProductCell.setCellValue(productId);

                    destinationRowCount = destinationRowCount + 1;
                }

            }

            sourceWorkbook.close();
        }
        // End of writing product stay times section



        // Saves the workbook ==========================================================================================

        FileOutputStream outputStream = new FileOutputStream(destinationFile);
        destinationWorkbook.write(outputStream);
        outputStream.close();
        destinationWorkbook.close();
        LOGGER.info("SUCCESSFULLY generated Tableau Excel file at: " + destinationFile.toString());

        /*
        // Start of old method
        // Get the column header definitions from the first file
        ArrayList<String> columnHeaders = new ArrayList<String>();
        File firstFile = excelFiles.get(0);
        Workbook firstWorkbook = WorkbookFactory.create(firstFile);
        Sheet firstSummarySheet = firstWorkbook.getSheet("OVERALL_SUMMARY");
        Row headerRow = firstSummarySheet.getRow(0);

        for (int i = 0; i < headerRow.getPhysicalNumberOfCells(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell != null) {
                String headerString = cell.getStringCellValue();
                columnHeaders.add(headerString);
            }
        }

        firstWorkbook.close();

        // Initialize arraylist and map to store column values
        TreeMap<String, ArrayList<Double>> mapOfRunTypesToValues = new TreeMap<String, ArrayList<Double>>();
        ArrayList<String> listOfRunTypes = new ArrayList<String>();

        // Get numbers from all excel files
        for (File file: excelFiles) {
            Workbook workbook = WorkbookFactory.create(file);
            Sheet summarySheet = workbook.getSheet("OVERALL_SUMMARY");
            Row summaryRow = summarySheet.getRow(1);

            // Populate the entries from each sheet
            String runName = summaryRow.getCell(0).getStringCellValue();
            listOfRunTypes.add(runName);
            ArrayList<Double> singleRowValues = new ArrayList<Double>();
            for (int i = 1; i <= 22; i++) { // Hardcode the number of values at 22
                Cell cell = summaryRow.getCell(i);
                Double value = cell.getNumericCellValue();
                singleRowValues.add(value);
            }
            mapOfRunTypesToValues.put(runName, singleRowValues);
            workbook.close();
        }

        // Append the data to a new workbook ===========================================================================
        Sheet destinationSheet = destinationWorkbook.createSheet("OUTPUT_SUMMARY");

        // Append column headers
        headerRow = destinationSheet.createRow(0);
        for (int i = 0; i < columnHeaders.size(); i++) {
            Cell cell = headerRow.createCell(i, CellType.STRING);
            cell.setCellValue(columnHeaders.get(i));
        }



        // Append each row data
        for (int i = 0; i < listOfRunTypes.size(); i++) {
            String runType = listOfRunTypes.get(i);
            ArrayList<Double> runTypeValues = mapOfRunTypesToValues.get(runType);
            Row row = destinationSheet.createRow(i + 1);
            Cell cell = row.createCell(0, CellType.STRING);
            cell.setCellValue(runType);

            for (int j = 0; j < runTypeValues.size(); j++) {
                Double value = runTypeValues.get(j);
                cell = row.createCell(j + 1, CellType.NUMERIC);
                cell.setCellValue(value);
            }
        }


        // Close the destination workbook.
        FileOutputStream outputStream = new FileOutputStream(destinationFile);
        destinationWorkbook.write(outputStream);
        outputStream.close();
        destinationWorkbook.close();
        LOGGER.info("SUCCESSFULLY generated Tableau Excel file at: " + destinationFile.toString());
        */
    }

    /**
     * Generates the summary statistic for a single excel file.
     * Sample usage:
     * "
     * File outputExcelFile01 = new File("src/main/resources/sample-output-files/output-files-with-summary-data/output_min_size_20.xlsx");
     * appendSummaryStatisticsOfSingleOutputExcelFile(outputExcelFile01);
     * "
     * @param outputExcelFile Output excel file of a single simulation run.
     * @throws IOException
     */
    public static void appendSummaryStatisticsOfSingleOutputExcelFile(File outputExcelFile) throws IOException {
        LOGGER.info("Starting output summary generation");

        // Creates a temp excel file for referencing
        File originalInputFile = outputExcelFile;
        File tempOutputFile = new File(originalInputFile.toString() + "temp.xlsx");
        OutputAnalysisUtil.copyFileUsingStream(originalInputFile, tempOutputFile);
        LOGGER.info("Successfully generated temporary copy of output file.");

        if (!tempOutputFile.exists()) {
            throw new IOException("File not found in: " + originalInputFile.toString());
        }

        TreeMap<String, Double> mapOfUtilizationRates = new TreeMap<String, Double>();
        Workbook workbook = WorkbookFactory.create(tempOutputFile);
        LOGGER.info("Successfully created Workbook from temporary copy of output file");

        //TODO: Implement check if all sheets are present in the workbook

        try {

            // ==================   Get average utilization rates of IBIS Ovens ============================================
            final String UTIL_RES_REP = "Util Res Rep";
            Sheet utilSheet = workbook.getSheet(UTIL_RES_REP);
            if (utilSheet == null) {
                throw new IOException("Excel file doesn't contain sheet: " + UTIL_RES_REP);
            }
            TreeMap<String, Double> treeMapOfAverageUtilizationRates = OutputAnalysisCalculation.calculateAverageIbisOvenUtilRate(utilSheet);
            mapOfUtilizationRates.putAll(treeMapOfAverageUtilizationRates);
            // =========================== End of section on IBIS Oven utilization rates ===================================

            // ====================== Get cycle time data  ============================================================
            final String THROUGHPUT_PRODUCT_REP = "Throughput Product Rep";
            Sheet cycleTimeSheet = workbook.getSheet(THROUGHPUT_PRODUCT_REP);
            if (cycleTimeSheet == null) {
                throw new IOException("Excel file doesn't contain sheet: " + THROUGHPUT_PRODUCT_REP);
            }
            TreeMap<String, Double> treeMapOfProductToAverageCycleTimesFromThroughputProduct = OutputAnalysisCalculation.calculateProductCycleTimeFromThroughputProduct(cycleTimeSheet);
            // ====================== End of section on cycle time summary statistics =====================================

            // ===============================  Get throughput data ====================================================
            final String DAILY_THROUGHPUT_PRODUCT_REP = "Daily Throughput Product Rep";
            Sheet throughputSheet = workbook.getSheet(DAILY_THROUGHPUT_PRODUCT_REP);
            if (throughputSheet == null) {
                throw new IOException("Excel file doesn't contain sheet: " + DAILY_THROUGHPUT_PRODUCT_REP);
            }
            TreeMap<String, Double> treeMapOfProductToAverageThroughput = OutputAnalysisCalculation.calculateProductThroughput(throughputSheet);
            // ====================== End of section on throughput data ================================================

            //=============================== Get daily throughput =====================================================
            // Day is stored as a numeric double
            TreeMap<Double, Double> treeMapOfDayToOutput = OutputAnalysisCalculation.calculateDailyThroughput(throughputSheet);
            // ======================== End of section on daily throughput =============================================

            // ======================= Get cycle time from "Daily Throughput Product Rep" =====================================
            TreeMap<String, Double> treeMapOfProductToAverageCycleTimesFromDailyThroughput = OutputAnalysisCalculation.calculateProductCycleTimeFromDailyThroughput(throughputSheet);
            // ======================== End of cycle time section ==============================================================

            // =========================== Extract file name ie run specs ==============================================
            String runType = OutputAnalysisUtil.fileStringToFileName(originalInputFile.toString());

            // Saves the simulation run type and utilization rate to a new sheet
            OutputAnalysisUtil.saveRunTypeAndUtilizationRatesTONewSheet("RUN_TYPE_AND_IBIS_UTILIZATION", runType, mapOfUtilizationRates, workbook);

            // Saves the product cycle time to a new sheet
            OutputAnalysisUtil.saveProductCycleTimeToNewSheet("PRODUCT_STAY_TIME", treeMapOfProductToAverageCycleTimesFromThroughputProduct, workbook);

            // Saves the product throughput to a new sheet
            OutputAnalysisUtil.saveProductThroughputToNewSheet("PRODUCT_THROUGHPUT", treeMapOfProductToAverageThroughput, workbook);

            // Saves the daily output to a new sheet
            OutputAnalysisUtil.saveDailyOutputSheet("DAILY_OUTPUT", treeMapOfDayToOutput, workbook);

            // Save the product cycle time (from daily product throughput) to a new sheet
            OutputAnalysisUtil.saveProductCycleTimeFromDailyThroughputToNewSheet("PRODUCT_TIME_IN_SYSTEM", treeMapOfProductToAverageCycleTimesFromDailyThroughput, workbook );

            // Saves the current edited workbook by overwriting the original file
            FileOutputStream outputStream = new FileOutputStream(originalInputFile.toString());
            workbook.write(outputStream);
            outputStream.close();

            // Perform closing operations
            workbook.close();
            tempOutputFile.delete();
            LOGGER.info("Closed workbook and deleted temporary excel file.");
            LOGGER.info("SUCCESSFULLY generated Output statistics for " + originalInputFile.getName() + "\n" +
                                "=========================================================================================");

        } catch (CustomException e) {

            LOGGER.severe("EXCEPTION: " + e.getMessage() + ".");

            workbook.close();
            tempOutputFile.delete();
            LOGGER.info("Closed workbook and deleted temporary excel file.");

        }

    }

    /**
     * Wrapper function to handle all files in a specified folder.
     * @param folderDirectory Directory of a folder with excel files to be processed.
     * @throws CustomException if argument is not a directory.
     */
    public static void appendSummaryStatisticsOfFolderOFExcelFiles(File folderDirectory) throws CustomException, IOException {
        if (!folderDirectory.isDirectory()) {
            throw new CustomException(folderDirectory.toString() + " is not a directory");
        }
        LOGGER.info("Accessing folder: " + folderDirectory.toString());

        // Process all files in the directory and append their respective summary statistics
        for (File file: folderDirectory.listFiles()) {
            if (file.exists() && (!file.isDirectory())) {
                appendSummaryStatisticsOfSingleOutputExcelFile(file);
            }
        }
    }

}

// Unused code
/*
// =============================== Get Product throughput from Daily throughput Resource =======================
final String DAILY_THROUGHPUT_RES_REP = "Daily Throughput Res Rep";
Sheet dailyThroughputSheet = workbook.getSheet(DAILY_THROUGHPUT_RES_REP);
if (dailyThroughputSheet == null) {
    throw new IOException("Excel file doesn't contain sheet: " + DAILY_THROUGHPUT_RES_REP);
}
TreeMap<String, Double> treeMapOfSummarizedDailyThroughputByResource = OutputAnalysisCalculation.calculateThroughputBasedOnDailyThroughputByResource(dailyThroughputSheet);
mapOfSummaryStatistics.putAll(treeMapOfSummarizedDailyThroughputByResource);
// =========================== End of section on Summarizing Daily Throughput ==================================

// =============================== Get average cycle time of products ==========================================
final String THROUGHPUT_PRODUCT_REP = "Throughput Product Rep";
Sheet productCycleTimeSheet = workbook.getSheet(THROUGHPUT_PRODUCT_REP);
TreeMap<String, Double> treeMapOfAverageProductCycleTime = OutputAnalysisCalculation.calculateAverageProductCycleTime(productCycleTimeSheet);
mapOfSummaryStatistics.putAll(treeMapOfAverageProductCycleTime);
// =============================== End of Cycle Time Calculation ===============================================

// =============================== Get value of throughput =====================================================

// Read the sheet from product-cost excel file
final String DAILY_THROUGHPUT_PRODUCT_REP = "Daily Throughput Product Rep";
File productCostFile = OutputAnalysisUtil.getProductKeyCostExcelFileFromRelativeDirectory();
Workbook productCostWorkbook = WorkbookFactory.create(productCostFile);
Sheet productCostSheet = productCostWorkbook.getSheetAt(0);
Sheet dailyProductThroughputSheet = workbook.getSheet(DAILY_THROUGHPUT_PRODUCT_REP);
TreeMap<String, Double> treeMapOfTotalThroughputWorth = OutputAnalysisCalculation.calculateTotalProductWorth(dailyProductThroughputSheet, productCostSheet);
productCostWorkbook.close();
productCostFile.delete();
mapOfSummaryStatistics.putAll(treeMapOfTotalThroughputWorth);
// =========================== End of throughput worth calculation =============================================

// =========================== Get Product Throughput from Daily Throughput Product ============================
//final String DAILY_THROUGHPUT_PRODUCT_REP = "Daily Throughput Product Rep";
TreeMap<String, Double> treeMapOfSummarizedDailyThroughputByProduct = OutputAnalysisCalculation.calculateThroughputBasedOnDailyThroughputByProduct(dailyProductThroughputSheet);
mapOfSummaryStatistics.putAll(treeMapOfSummarizedDailyThroughputByProduct);
// =========================== End of Product Throughput from Daily Throughput Product =========================

// ========================== Get Product Throughput from "Throughput Res Rep" =================================
final String THROUGHPUT_RES_REP = "Throughput Res Rep";
Sheet throughputResourceSheet = workbook.getSheet(THROUGHPUT_RES_REP);
TreeMap<String, Double> treeMapOfSummarizedThroughputByFlexsim = OutputAnalysisCalculation.calculateThroughputBasedOnThroughputByResource(throughputResourceSheet);
mapOfSummaryStatistics.putAll(treeMapOfSummarizedThroughputByFlexsim);
// =========================== End of Product Throughput from "Throughput Res Rep" =========================

*/
