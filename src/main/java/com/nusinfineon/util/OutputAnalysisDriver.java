package com.nusinfineon.util;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.nusinfineon.core.OutputAnalysisCore;
import com.nusinfineon.exceptions.CustomException;

/**
 * This class is a driver to execute the OutputAnalysis code only.
 */
public class OutputAnalysisDriver {


    public static void main(String[] args) throws IOException, CustomException {
        System.out.println("Starting output analysis...");
        OutputAnalysisCore outputCore = new OutputAnalysisCore();

        // =============== Tests on the whole folder ===================================================================
        File folderDirectory = new File("sample-output-files/output-files-clean");
        File destinationDirectory = new File("sample-output-files");

        System.out.println("Accessing folder: " + folderDirectory.toString());
        // Generate output statistics for all excel files in a folder
        outputCore.appendSummaryStatisticsOfFolderOFExcelFiles(folderDirectory);

        // Generate the tableau excel file from the folder of excel files (with output data appended)
        outputCore.generateExcelTableauFile(folderDirectory, destinationDirectory);

        // Copy Tableau files from resources to output folder
        File tableauSourceDirectory = new File("build/resources/main/output/tableau_workbooks");
        try {
            FileUtils.copyDirectory(tableauSourceDirectory, destinationDirectory);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
