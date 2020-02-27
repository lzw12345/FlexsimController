package com.nusinfineon.core;

import com.nusinfineon.exceptions.CustomException;
import com.nusinfineon.util.OutputAnalysisCalculation;
import org.apache.poi.ss.usermodel.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Logger;

import com.nusinfineon.util.OutputAnalysisUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class OutputAnalysisCore {

    private final static Logger LOGGER = Logger.getLogger(OutputAnalysisCore.class.getName());

    public static void main(String[] args) throws IOException {


        // Declare output file and cost file paths
        String productKeyCostExcelFilePath = "C:\\Users\\Ahmad\\Documents\\NUS\\IE 3100M\\Data Files\\product_key_cost.xlsx";
        String outputExcelFilePath1 = "C:\\Users\\Ahmad\\Documents\\NUS\\IE 3100M\\Data Files\\output_files_with_summary\\output_min_size_20.xlsx";
        String outputExcelFilePath2 = "C:\\Users\\Ahmad\\Documents\\NUS\\IE 3100M\\Data Files\\output_files_with_summary\\output_min_size_21.xlsx";
        String outputExcelFilePath3 = "C:\\Users\\Ahmad\\Documents\\NUS\\IE 3100M\\Data Files\\output_files_with_summary\\output_min_size_22.xlsx";
        String outputExcelFilePath4 = "C:\\Users\\Ahmad\\Documents\\NUS\\IE 3100M\\Data Files\\output_files_with_summary\\output_min_size_23.xlsx";
        String outputExcelFilePath5 = "C:\\Users\\Ahmad\\Documents\\NUS\\IE 3100M\\Data Files\\output_files_with_summary\\output_min_size_24.xlsx";
        String outputExcelFilePath6 = "C:\\Users\\Ahmad\\Documents\\NUS\\IE 3100M\\Data Files\\output_files_with_summary\\output_shortest_queue_resource_selection.xlsx";

        /*
        // Execute the summary statistics. Summary data will be appended to the output excel file.
        getOutputSummaryStatistics(outputExcelFilePath1, productKeyCostExcelFilePath);
        getOutputSummaryStatistics(outputExcelFilePath2, productKeyCostExcelFilePath);
        getOutputSummaryStatistics(outputExcelFilePath3, productKeyCostExcelFilePath);
        getOutputSummaryStatistics(outputExcelFilePath4, productKeyCostExcelFilePath);
        getOutputSummaryStatistics(outputExcelFilePath5, productKeyCostExcelFilePath);
        getOutputSummaryStatistics(outputExcelFilePath6, productKeyCostExcelFilePath);
        */

        // Generate a single excel output file (for Tableau) from multiple output files (already parsed).

        File outputFile1 = new File(outputExcelFilePath1);
        File outputFile2 = new File(outputExcelFilePath2);
        File outputFile3 = new File(outputExcelFilePath3);
        File outputFile4 = new File(outputExcelFilePath4);
        File outputFile5 = new File(outputExcelFilePath5);
        File outputFile6 = new File(outputExcelFilePath6);
        ArrayList<File> fileArrayList = new ArrayList<File>();

        fileArrayList.add(outputFile1);
        fileArrayList.add(outputFile2);
        fileArrayList.add(outputFile3);
        fileArrayList.add(outputFile4);
        fileArrayList.add(outputFile5);
        fileArrayList.add(outputFile6);

        File destinationFile = new File("C:\\Users\\Ahmad\\Documents\\NUS\\IE 3100M\\Data Files\\output_files_with_summary\\tableau_file.xlsx");

        generateExcelTableauFile(fileArrayList, destinationFile);

    }

    /**
     * Generates a single excel file to be used with Tableau. Summarizes the output file data for each excel file.
     * @param excelFiles
     */
    public static void generateExcelTableauFile(List<File> excelFiles, File destinationFile) throws IOException {
        LOGGER.info("Starting generateExcelTableauFile method");

        // Create the destination excel workbook and excel file
        if (!destinationFile.exists()) {
            destinationFile.createNewFile();
        }

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
        Workbook destinationWorkbook = new XSSFWorkbook();
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
    }

    /**
     * Generates the summary statistic for a single excel file.
     * @param outputExcelFilePath
     * @param productKeyCostExcelFilePath
     * @throws IOException
     */
    public static void getOutputSummaryStatistics(String outputExcelFilePath, String productKeyCostExcelFilePath) throws IOException {
        LOGGER.info("Starting output summary generation");

        // Creates a temp excel file for referencing
        File originalInputFile = new File(outputExcelFilePath);
        File tempOutputFile = new File(outputExcelFilePath + "temp.xlsx");
        OutputAnalysisUtil.copyFileUsingStream(originalInputFile, tempOutputFile);
        LOGGER.info("Successfully generated temporary copy of output file.");

        if (!tempOutputFile.exists()) {
            throw new IOException("File not found in: " + outputExcelFilePath);
        }

        TreeMap<String, Double> mapOfSummaryStatistics = new TreeMap<String, Double>();
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
            mapOfSummaryStatistics.putAll(treeMapOfAverageUtilizationRates);
            // =========================== End of section on IBIS Oven utilization rates ===================================

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
            File productCostFile = new File(productKeyCostExcelFilePath);
            Workbook productCostWorkbook = WorkbookFactory.create(productCostFile);
            Sheet productCostSheet = productCostWorkbook.getSheetAt(0);
            Sheet dailyProductThroughputSheet = workbook.getSheet(DAILY_THROUGHPUT_PRODUCT_REP);
            TreeMap<String, Double> treeMapOfTotalThroughputWorth = OutputAnalysisCalculation.calculateTotalProductWorth(dailyProductThroughputSheet, productCostSheet);
            productCostWorkbook.close();
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

            // =========================== Extract file name ie run specs ==============================================
            String runType = OutputAnalysisUtil.fileStringToFileName(originalInputFile.toString());

            // Saves all the extracted information to a new sheet.
            OutputAnalysisUtil.saveOverallOutputDataToNewSheet("OVERALL_SUMMARY", runType, mapOfSummaryStatistics, workbook);

            // Saves the current edited workbook by overwriting the original file
            FileOutputStream outputStream = new FileOutputStream(outputExcelFilePath);
            workbook.write(outputStream);
            outputStream.close();

            // Perform closing operations
            workbook.close();
            tempOutputFile.delete();
            LOGGER.info("Closed workbook and deleted temporary excel file.");

            LOGGER.info("Output statistics for " + originalInputFile.toString() + " generated successfully");

        } catch (CustomException e) {

            LOGGER.severe("EXCEPTION: " + e.getMessage() + ".");

            workbook.close();
            tempOutputFile.delete();
            LOGGER.info("Closed workbook and deleted temporary excel file.");

        }

    }

}

