package com.nusinfineon.core;

import static com.nusinfineon.util.FlexScriptDefaultCodes.GETPROCESSTIMECODE;
import static com.nusinfineon.util.FlexScriptDefaultCodes.MAIN15CODE;
import static com.nusinfineon.util.FlexScriptDefaultCodes.ONRUNSTOPCODE;
import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.apache.commons.io.FilenameUtils.getFullPath;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import com.nusinfineon.exceptions.CustomException;

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
    private String scriptFilepath = "./script.txt";
    private File file;
    private String batchSizeMinString;
    private String batchSizeMaxString;
    private String batchSizeStepString;
    private boolean isModelShown;

    private final static Logger LOGGER = Logger.getLogger(Core.class.getName());

    /**
     * main execute function, generates script and runs model
     * @param flexsimLocation
     * @param modelLocation
     * @param inputLocation
     * @param outputLocation
     * @param runSpeed
     * @param warmUpPeriod
     * @param stopTime
     * @param isModelShown
     * @throws IOException
     */
    public void execute(String flexsimLocation, String modelLocation, String inputLocation,
                        String outputLocation, String runSpeed, String warmUpPeriod,
                        String stopTime, boolean isModelShown, String batchSizeMinString,
                        String batchSizeMaxString, String batchSizeStepString) throws IOException, CustomException {

        file = new File(scriptFilepath);
        if (!file.createNewFile()){}
        this.flexsimLocation = flexsimLocation;
        this.modelLocation =  modelLocation ;
        inputFile = '"' + getBaseName(inputLocation) + "." + getExtension(inputLocation);
        this.inputLocation = getFullPath(inputLocation).replace("\\", "\\\\") ;
        deleteExistingFile( getFullPath(outputLocation) + "OutputNew.xlsx");
        outputFile =  getBaseName(outputLocation) + "." + getExtension(outputLocation) ;
        this.outputLocation = getFullPath(outputLocation).replace("\\", "\\\\\\\\\\");
        this.runSpeed = "runspeed(" + runSpeed + ");" ;
        this.warmUpPeriod = warmUpPeriod;
        this.stopTime = "stoptime(" + stopTime + ");";
        this.isModelShown = isModelShown;
        scriptCreator();

        // Code block handling creation of excel file for batch iterating
        BatchSizeCore batchSizeCore = new BatchSizeCore(inputLocation, batchSizeMinString,
                batchSizeMaxString, batchSizeStepString);
        try {
            batchSizeCore.execute();
        } catch (IOException e) {
            LOGGER.severe("Unable to create files");
            throw new CustomException("Error in creating temp files");
        } catch (CustomException e) {
            LOGGER.severe(e.getMessage());
            throw e;
        }

        // Extract the array of files and sizes from batchSizeCore
        ArrayList<File> excelFiles = batchSizeCore.getExcelFiles();
        ArrayList<Integer> batchSizes = batchSizeCore.getListOfBatchSizes();

        for (int i = 0; i < excelFiles.size(); i++) {
            System.out.println("Batch size: " + batchSizes.get(i) + ". File path: " + excelFiles.get(i).toString());
        }

        // Executes the command line to run model
        commandLineGenerator(isModelShown);
    }

    /**
     * Used to store data into core before the json parser serializes it
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
                          String batchSizeMinString, String batchSizeMaxString, String batchSizeStepString){
        this.flexsimLocation = flexsimLocation;
        this.modelLocation = modelLocation;
        this.inputLocation = inputLocation;
        this.outputLocation = outputLocation;
        this.runSpeed = runSpeed;
        this.warmUpPeriod = warmUpPeriod;
        this.stopTime = stopTime;

        this.batchSizeMinString = batchSizeMinString;
        this.batchSizeMaxString = batchSizeMaxString;
        this.batchSizeStepString = batchSizeStepString;
    }

    /**
     * creates the commandline to execute model
     * @throws IOException
     */
    public void commandLineGenerator(boolean isModelShown) throws IOException {
        Process a = Runtime.getRuntime().exec('"' + flexsimLocation + '"' +
                '"' + modelLocation + '"' + " /maintenance " + (isModelShown?"":"nogui_") + "runscript " +
                "/scriptpath" + file.getAbsolutePath() );
    }

    /**
     * Creates the flexscript for the model
     * @throws IOException
     */
    public void scriptCreator() throws IOException {
        FileWriter fileWriter = new FileWriter(scriptFilepath);
        fileWriter.write(runSpeed + "\n"
            + stopTime + "\nmsg(\"Model Execution\", \"Begin loading input?\");\nshowprogressbar(\"\");\n"
            + "MAIN2LoadData (\"" + inputLocation + "\"," + inputFile + "\");\n"
            + editNodeCode("RunStop", "MODEL://Tools//OnRunStop", "concat(" + ONRUNSTOPCODE
                +  ",\"MAIN15WriteReports(true, \\\""
                +outputLocation  + "\", " + "\\\"" + outputFile
                + "\\\" , \\\"OutputNew\\\");\\n\\thideprogressbar();\\n}\")" )
            + editNodeCode("ProcessTime", "MODEL:/Tools/UserCommands/ProcessTimeGetTotal/code", GETPROCESSTIMECODE)
            + editNodeCode("MAIN15", "MODEL://Tools/UserCommands//MAIN15WriteReports//code", MAIN15CODE)
            + "msg(\"Model Execution\",\"loading complete, begin run?\");\n"
            + "MAINBuldAndRun ();\nresetmodel();\ngo();");
        fileWriter.close();

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
        return runSpeed;
    }

    public String getWarmUpPeriod() {
        return warmUpPeriod;
    }

    public String getStopTime() {
        return stopTime;
    }

    public String getBatchSizeMinString() {
        return batchSizeMinString;
    }

    public String getBatchSizeMaxString() {
        return batchSizeMaxString;
    }

    public String getBatchSizeStepString() {
        return batchSizeStepString;
    }

    /**
     * Deletes existing output files to prevent excel overwrite popup
     * @param pathname
     */
    public void deleteExistingFile(String pathname){
        try {
            File f= new File(pathname);                         //file to be delete
            if(f.delete()) {                                    //returns Boolean value
                System.out.println(f.getName() + " deleted");   //getting and printing the file name
            } else {
                System.out.println(pathname + "doesn't exist");
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Function to generate a default template for replace code in a flexsim node
     * @param name
     * @param nodePath
     * @param code
     * @return
     */
    public String editNodeCode(String name ,String nodePath , String code){
        String nodename = name + "Node";
        String codeName = name + "Code";
        String script = "treenode " + nodename + " = node(\"" + nodePath + "\");\n"
                + "string " + codeName + " = " + code + ";\n"
                + "setnodestr(" + nodename + "," + codeName + ");\n"
                + "enablecode(" + nodename + ");\n"
                + "buildnodeflexscript(" + nodename +");\n";

        return script;
    }
}
