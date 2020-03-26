package com.nusinfineon.core;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import com.nusinfineon.Main;
import com.nusinfineon.exceptions.CustomException;
import com.nusinfineon.util.LotSequencingRule;

public class Core {

    private String flexsimLocation;
    private String modelLocation;
    private String inputLocation;
    private String outputLocation;
    private String runSpeed;
    private String stopTime;
    private HashMap<LotSequencingRule, Boolean> lotSequencingRules;
    private String batchSizeMinString;
    private String batchSizeMaxString;
    private String batchSizeStepString;
    private String resourceSelectCriteria;
    private String lotSelectionCriteria;
    private String trolleyLocationSelectCriteria;
    private String bibLoadOnLotCriteria;
    private boolean isModelShown;
    private ArrayList<File> excelOutputFiles;

    private static final Logger LOGGER = Logger.getLogger(Core.class.getName());
    private static final Boolean INIT_FCFS = true;
    private static final Boolean INIT_SPT = false;
    private static final Boolean INIT_MJ = false;
    private static final Boolean INIT_RAND = false;
    private static final String INIT_RUN_SPEED = "4";
    private static final String INIT_STOP_TIME = "1140";
    private static final String INIT_MAX_BATCH_SIZE = "24";
    private static final String INIT_MIN_BATCH_SIZE = "1";
    private static final String INIT_STEP_SIZE = "1";
    private static final String INIT_RESOURCE_SELECT_CRITERIA = "4";
    private static final String INIT_LOT_SELECTION_CRITERIA = "3";
    private static final String INIT_TROLLEY_LOCATION_SELECT_CRITERIA = "2";
    private static final String INIT_BIB_LOAD_ON_LOT_CRITERIA = "2";

    private static final String OUTPUT_FOLDER_NAME = "Output";
    private static final String RAW_OUTPUT_FOLDER_NAME = "Raw Output Excel Files";
    private static final String TABLEAU_FILES_DIR = "/output/tableau_workbooks";
    private static final ArrayList<String> TABLEAU_FILE_NAMES = new ArrayList<>(Arrays.asList(
            "Daily Throughput.twb", "IBIS Utilization Rates.twb", "Stay Time.twb",
            "Throughput.twb", "Time in System.twb", "Worth.twb"));

    /**
     * Main execute function to generate input files. run model and generate output file
     * @throws IOException, CustomException, InterruptedException, DDEException
     */
    public void execute() throws IOException, CustomException {
        // Code block handling creation of excel file for min batch size iterating
        ExcelInputCore excelInputCore = new ExcelInputCore(inputLocation, lotSequencingRules, batchSizeMinString,
                batchSizeMaxString, batchSizeStepString, resourceSelectCriteria, lotSelectionCriteria,
                trolleyLocationSelectCriteria, bibLoadOnLotCriteria);

        // Initialise listener for running of simulation
        RunCore runCore = new RunCore(flexsimLocation, modelLocation, outputLocation, runSpeed, stopTime, isModelShown);

        try {
            excelInputCore.execute();
        } catch (IOException e) {
            LOGGER.severe("Unable to create files");
            throw new CustomException("Error in creating temp files");
        } catch (CustomException e) {
            LOGGER.severe(e.getMessage());
            throw e;
        }

        // Extract the array of files and sizes from ExcelInputCore
        ArrayList<File> excelInputFiles = excelInputCore.getExcelFiles();
        excelOutputFiles = new ArrayList<>();

        runCore.executeRuns(excelInputFiles, excelOutputFiles);

        handleOutput();

        Runtime.getRuntime().exec("cmd /c taskkill /f /im excel.exe");
    }

    /**
     * Used to handle processing and analysis of output
     * @throws IOException
     */
    private void handleOutput() throws IOException, CustomException {
        File outputFile = new File(outputLocation);
        String outputPathName = outputFile.getParent();
        Path outputDir = Paths.get(outputPathName, OUTPUT_FOLDER_NAME);
        Path rawOutputDir = Paths.get(outputPathName, OUTPUT_FOLDER_NAME, RAW_OUTPUT_FOLDER_NAME);

        // Create folder directories
        if (!Files.exists(outputDir)) {
            Files.createDirectory(outputDir);
            LOGGER.info("Output directory created");
        } else {
            LOGGER.info("Output directory already exists");
        }

        // Delete and recreate raw output folder if exists
        if (!Files.exists(rawOutputDir)) {
            Files.createDirectory(rawOutputDir);
            LOGGER.info("Raw output directory created");
        } else {
            LOGGER.info("Raw output directory already exists");
            FileUtils.deleteDirectory(new File(rawOutputDir.toString()));
            Files.createDirectory(rawOutputDir);
        }

        // Move raw output files into Raw Output folder
        for (File file : excelOutputFiles) {
            String fileName = file.getName();
            Files.move(Paths.get(String.valueOf(file)), Paths.get(String.valueOf(rawOutputDir), fileName),
                    StandardCopyOption.REPLACE_EXISTING);
            LOGGER.info(fileName + " moved into Raw Output folder");
        }

        // Output Analysis
        File folderDirectory = new File(String.valueOf(rawOutputDir));
        File destinationDirectory = new File(String.valueOf(outputDir));

        if (folderDirectory.list().length > 0) {
            // Generate output statistics for all excel files in a folder
            OutputAnalysisCore.appendSummaryStatisticsOfFolderOFExcelFiles(folderDirectory);

            // Generate the tableau excel file from the folder of excel files (with output data appended)
            OutputAnalysisCore.generateExcelTableauFile(folderDirectory, destinationDirectory);

            // Copy Tableau files from resources to output folder
            for (String fileName : TABLEAU_FILE_NAMES) {
                try {
                    URL file = Main.class.getResource(TABLEAU_FILES_DIR + "/" + fileName);
                    File newFile = new File(destinationDirectory + "/" + fileName);
                    FileUtils.copyURLToFile(file, newFile);
                    LOGGER.info(fileName + " moved into Output folder");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            throw new CustomException("Raw Output Excel Files folder is empty!");
        }
    }

    /**
     * Used to store data into core before execute and save (the json parser serializes it)
     *
     * @param flexsimLocation
     * @param modelLocation
     * @param inputLocation
     * @param outputLocation
     * @param runSpeed
     * @param stopTime
     * @param batchSizeMinString
     * @param batchSizeMaxString
     * @param batchSizeStepString
     */
    public void inputData(String flexsimLocation, String modelLocation, String inputLocation, String outputLocation,
                          String runSpeed, String stopTime, boolean isModelShown,
                          HashMap<LotSequencingRule, Boolean> lotSequencingRules,
                          String batchSizeMinString, String batchSizeMaxString, String batchSizeStepString,
                          String resourceSelectCriteria, String lotSelectionCriteria,
                          String trolleyLocationSelectCriteria, String bibLoadOnLotCriteria) {
        this.flexsimLocation = flexsimLocation;
        this.modelLocation = modelLocation;
        this.inputLocation = inputLocation;
        this.outputLocation = outputLocation;
        this.runSpeed = runSpeed;
        this.stopTime = stopTime;
        this.isModelShown = isModelShown;

        this.lotSequencingRules = lotSequencingRules;

        this.batchSizeMinString = batchSizeMinString;
        this.batchSizeMaxString = batchSizeMaxString;
        this.batchSizeStepString = batchSizeStepString;

        this.resourceSelectCriteria = resourceSelectCriteria;
        this.lotSelectionCriteria = lotSelectionCriteria;
        this.trolleyLocationSelectCriteria = trolleyLocationSelectCriteria;
        this.bibLoadOnLotCriteria = bibLoadOnLotCriteria;
    }

    public String getFlexsimLocation() {
        return flexsimLocation;
    }

    public String getModelLocation() {
        return modelLocation;
    }

    public String getInputLocation() {
        return inputLocation;
    }

    public String getOutputLocation() {
        return outputLocation;
    }

    public String getRunSpeed() {
        if (runSpeed == null) {
            return INIT_RUN_SPEED;
        } else {
            return runSpeed;
        }
    }

    public String getStopTime() {
        if (stopTime == null) {
            return INIT_STOP_TIME;
        } else {
            return stopTime;
        }
    }

    public boolean getIsModelShown() {
        return isModelShown;
    }

    public HashMap<LotSequencingRule, Boolean> getLotSequencingRules() {
        if (lotSequencingRules == null) {
            lotSequencingRules = new HashMap<LotSequencingRule, Boolean>();
            lotSequencingRules.put(LotSequencingRule.FCFS, INIT_FCFS);
            lotSequencingRules.put(LotSequencingRule.SPT, INIT_SPT);
            lotSequencingRules.put(LotSequencingRule.MJ, INIT_MJ);
            lotSequencingRules.put(LotSequencingRule.RAND, INIT_RAND);
        }
        return lotSequencingRules;
    }

    public String getBatchSizeMinString() {
        if (batchSizeMinString == null) {
            return INIT_MIN_BATCH_SIZE;
        } else {
            return batchSizeMinString;
        }
    }

    public String getBatchSizeMaxString() {
        if (batchSizeMaxString == null) {
            return INIT_MAX_BATCH_SIZE;
        } else {
            return batchSizeMaxString;
        }
    }

    public String getBatchSizeStepString() {
        if (batchSizeMinString == null) {
            return INIT_STEP_SIZE;
        } else {
            return batchSizeStepString;
        }
    }

    public String getResourceSelectCriteria() {
        if (resourceSelectCriteria == null) {
            return INIT_RESOURCE_SELECT_CRITERIA;
        } else {
            return resourceSelectCriteria;
        }
    }

    public String getLotSelectionCriteria() {
        if (lotSelectionCriteria == null) {
            return INIT_LOT_SELECTION_CRITERIA;
        } else {
            return lotSelectionCriteria;
        }
    }

    public String getTrolleyLocationSelectCriteria() {
        if (trolleyLocationSelectCriteria == null) {
            return INIT_TROLLEY_LOCATION_SELECT_CRITERIA;
        } else {
            return trolleyLocationSelectCriteria;
        }
    }

    public String getBibLoadOnLotCriteria() {
        if (bibLoadOnLotCriteria == null) {
            return INIT_BIB_LOAD_ON_LOT_CRITERIA;
        } else {
            return bibLoadOnLotCriteria;
        }
    }
}
