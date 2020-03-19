package com.nusinfineon.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.nusinfineon.exceptions.CustomException;
import com.pretty_tools.dde.DDEException;

public class Core {

    private String flexsimLocation;
    private String modelLocation;
    private String inputLocation;
    private String inputFile;
    private String outputLocation;
    private String outputFile;
    private String runSpeed;
    private String warmUpPeriod;
    private String stopTime;
    private String lotSequencingRuleString;
    private String batchSizeMinString;
    private String batchSizeMaxString;
    private String batchSizeStepString;
    private String resourceSelectCriteria;
    private String lotSelectionCriteria;
    private String trolleyLocationSelectCriteria;
    private String bibLoadOnLotCriteria;
    private boolean isModelShown;
    private ArrayList<File> excelOutputFiles;

    private final static Logger LOGGER = Logger.getLogger(Core.class.getName());
    private final static String LOT_SEQUENCE_FCFS = "First-Come-First-Served (Default)";
    private final static String LOT_SEQUENCE_SPT = "Shortest Processing Time";
    private final static String LOT_SEQUENCE_MJ = "Most Jobs";
    private final static String LOT_SEQUENCE_RAND = "Random";
    private final static String INIT_RUN_SPEED = "4";
    private final static String INIT_STOP_TIME = "1140";
    private final static String INIT_MAX_BATCH_SIZE = "24";
    private final static String INIT_MIN_BATCH_SIZE = "1";
    private final static String INIT_STEP_SIZE = "1";
    private final static String INIT_RESOURCE_SELECT_CRITERIA = "4";
    private final static String INIT_LOT_SELECTION_CRITERIA = "3";
    private final static String INIT_TROLLEY_LOCATION_SELECT_CRITERIA = "2";
    private final static String INIT_BIB_LOAD_ON_LOT_CRITERIA = "2";

    /**
     * Main execute function to generate input files. run model and generate output file
     * @throws IOException, CustomException, InterruptedException, DDEException
     */
    public void execute() throws IOException, CustomException, InterruptedException, DDEException {
        // Code block handling creation of excel file for min batch size iterating
        ExcelInputCore excelInputCore = new ExcelInputCore(this.inputLocation, this.lotSequencingRuleString,
                this.batchSizeMinString, this.batchSizeMaxString, this.batchSizeStepString, this.resourceSelectCriteria,
                this.lotSelectionCriteria, this.trolleyLocationSelectCriteria, this.bibLoadOnLotCriteria);

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
        ArrayList<Integer> batchSizes = excelInputCore.getListOfMinBatchSizes();
        excelOutputFiles = new ArrayList<File>();

        // Running of simulation runs
        ExcelListener excelListener = new ExcelListener(excelInputFiles, batchSizes, this.flexsimLocation,
                this.modelLocation, this.outputLocation, this.runSpeed, this.warmUpPeriod, this.stopTime,
                this.isModelShown, excelOutputFiles);
    }

    /**
     * Used to store data into core before execute and save (the json parser serializes it)
     *
     * @param flexsimLocation
     * @param modelLocation
     * @param inputLocation
     * @param outputLocation
     * @param runSpeed
     * @param warmUpPeriod
     * @param stopTime
     * @param batchSizeMinString
     * @param batchSizeMaxString
     * @param batchSizeStepString
     */
    public void inputData(String flexsimLocation, String modelLocation, String inputLocation,
                          String outputLocation, String runSpeed, String warmUpPeriod, String stopTime,
                          boolean isModelShown, String lotSequencingRuleString, String batchSizeMinString,
                          String batchSizeMaxString, String batchSizeStepString, String resourceSelectCriteria,
                          String lotSelectionCriteria, String trolleyLocationSelectCriteria,
                          String bibLoadOnLotCriteria) {
        this.flexsimLocation = flexsimLocation;
        this.modelLocation = modelLocation;
        this.inputLocation = inputLocation;
        this.outputLocation = outputLocation;
        this.runSpeed = runSpeed;
        this.warmUpPeriod = warmUpPeriod;
        this.stopTime = stopTime;
        this.isModelShown = isModelShown;

        this.lotSequencingRuleString = lotSequencingRuleString;

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

    public String getWarmUpPeriod() {
        return warmUpPeriod;
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

    public ArrayList<String> getLotSequencingRulesList() {
        ArrayList<String> rulesList = new ArrayList<>();

        rulesList.add(LOT_SEQUENCE_FCFS);
        rulesList.add(LOT_SEQUENCE_SPT);
        rulesList.add(LOT_SEQUENCE_MJ);
        rulesList.add(LOT_SEQUENCE_RAND);

        return rulesList;
    }

    public String getLotSequencingRuleString() {
        if (lotSequencingRuleString == null) {
            return LOT_SEQUENCE_FCFS;
        } else {
            return lotSequencingRuleString;
        }
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
