package com.nusinfineon.core;

import com.nusinfineon.util.OutputAnalysisCalculation;
import org.apache.poi.ss.usermodel.*;

import java.io.*;
import java.util.HashMap;

import com.nusinfineon.util.OutputAnalysisUtil;

public class OutputAnalysisCore {


    public static void main(String[] args) throws IOException {
        getOutputSummaryStatistics("C:\\Users\\Ahmad\\Documents\\Workspace\\playground\\src\\main\\resources\\output_min_size_22.xlsx",
                "C:\\Users\\Ahmad\\Documents\\Workspace\\playground\\src\\main\\resources\\product_key_cost.xlsx");
    }

    public static void getOutputSummaryStatistics(String outputExcelFilePath, String productKeyCostExcelFilePath) throws IOException {

        // Creates a temp excel file for referencing
        File originalInputFile = new File(outputExcelFilePath);
        File tempOutputFile = new File(outputExcelFilePath + "temp.xlsx");
        OutputAnalysisUtil.copyFileUsingStream(originalInputFile, tempOutputFile);

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
        HashMap<String, Double> hashMapOfAverageUtilizationRates = OutputAnalysisCalculation.calculateAverageIbisOvenUtilRate(utilSheet);
        OutputAnalysisUtil.saveStringDoubleHashMapToNewSheet("IBIS AVG UTIL SUMMARY", hashMapOfAverageUtilizationRates, workbook);
        // =========================== End of section on IBIS Oven utilization rates ===================================

        // =============================== Get Product throughput from Daily throughput Resource =======================
        final String DAILY_THROUGHPUT_RES_REP = "Daily Throughput Res Rep";
        Sheet dailyThroughputSheet = workbook.getSheet(DAILY_THROUGHPUT_RES_REP);
        if (dailyThroughputSheet == null) {
            throw new IOException("Excel file doesn't contain sheet: " + DAILY_THROUGHPUT_RES_REP);
        }
        HashMap<String, Double> hashMapOfSummarizedDailyThroughputByResource = OutputAnalysisCalculation.calculateThroughputBasedOnDailyThroughputByResource(dailyThroughputSheet);
        OutputAnalysisUtil.saveStringDoubleHashMapToNewSheet("THROUGHPUT FROM DAILY", hashMapOfSummarizedDailyThroughputByResource, workbook);
        // =========================== End of section on Summarizing Daily Throughput ==================================

        // =============================== Get average cycle time of products ==========================================
        final String THROUGHPUT_PRODUCT_REP = "Throughput Product Rep";
        Sheet productCycleTimeSheet = workbook.getSheet(THROUGHPUT_PRODUCT_REP);
        HashMap<String, Double>hashMapOfAverageProductCycleTime = OutputAnalysisCalculation.calculateAverageProductCycleTime(productCycleTimeSheet);
        OutputAnalysisUtil.saveStringDoubleHashMapToNewSheet("AVERAGE CYCLE TIME", hashMapOfAverageProductCycleTime, workbook);
        // =============================== End of Cycle Time Calculation ===============================================

        // =============================== Get value of throughput =====================================================

        // Read the sheet from product-cost excel file
        final String DAILY_THROUGHPUT_PRODUCT_REP = "Daily Throughput Product Rep";
        File productCostFile = new File(productKeyCostExcelFilePath);
        Workbook productCostWorkbook = WorkbookFactory.create(productCostFile);
        Sheet productCostSheet = productCostWorkbook.getSheetAt(0);
        Sheet dailyProductThroughputSheet = workbook.getSheet(DAILY_THROUGHPUT_PRODUCT_REP);
        HashMap<String, Double> hashMapOfTotalThroughputWorth = OutputAnalysisCalculation.calculateTotalProductWorth(dailyProductThroughputSheet, productCostSheet);
        productCostWorkbook.close();
        OutputAnalysisUtil.saveStringDoubleHashMapToNewSheet("TOTAL WORTH", hashMapOfTotalThroughputWorth, workbook);
        // =========================== End of throughput worth calculation =============================================

        // =========================== Get Product Throughput from Daily Throughput Product ============================
        //final String DAILY_THROUGHPUT_PRODUCT_REP = "Daily Throughput Product Rep";
        HashMap<String, Long> hashMapOfSummarizedDailyThroughputByProduct = OutputAnalysisCalculation.calculateThroughputBasedOnDailyThroughputByProduct(dailyProductThroughputSheet);
        OutputAnalysisUtil.saveStringLongHashMapToNewSheet("THROUGHPUT FROM PRODUCT", hashMapOfSummarizedDailyThroughputByProduct, workbook);

        // Saves the current edited workbook by overwriting the original file
        FileOutputStream outputStream = new FileOutputStream(outputExcelFilePath);
        workbook.write(outputStream);
        outputStream.close();

        // Perform closing operations
        workbook.close();
        tempOutputFile.delete();

    }

}

