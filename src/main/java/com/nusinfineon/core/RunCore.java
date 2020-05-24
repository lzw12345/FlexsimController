package com.nusinfineon.core;

import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.apache.commons.io.FilenameUtils.getFullPath;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.nusinfineon.core.util.ScriptGenerator;
import com.nusinfineon.core.util.Server;

/**
 * Represents the component that interfaces with FlexSim to run all the simulation runs.
 */
public class RunCore {

    private static final Logger LOGGER = Logger.getLogger(RunCore.class.getName());

    private String flexsimLocation;
    private String modelLocation;
    private String inputLocation;
    private String inputFile;
    private String outputLocation;
    private String outputFile;
    private boolean isModelShown;
    private File scriptFile;
    private int currentRunNum;
    private String excelOutputFileName;
    private ArrayList<File> excelInputFiles;
    private ArrayList<File> excelOutputFiles;
    private ScriptGenerator scriptGenerator;
    private Server server;

    /**
     * Constructor for RunCore object based on the user-defined input.
     * Generates a server to connect with FlexSim.
     */
    public RunCore(String flexsimLocation, String modelLocation, String outputLocation,
                   String runSpeed, String stopTime, boolean isModelShown) {

        this.flexsimLocation = flexsimLocation;
        this.modelLocation = modelLocation;
        this.outputFile = getBaseName(outputLocation) + "." + getExtension(outputLocation);
        this.outputLocation = getFullPath(outputLocation).replace("\\", "\\\\\\\\\\");
        this.isModelShown = isModelShown;

        this.excelInputFiles = new ArrayList<>();
        this.excelOutputFiles = new ArrayList<>();

        this.scriptGenerator = new ScriptGenerator(runSpeed, stopTime);
        this.server = new Server(1880);
        this.currentRunNum = 0;
    }

    /**
     * Main execute function to start runs.
     * @return excelOutputFiles
     */
    public ArrayList<File> executeRuns(ArrayList<File> excelInputFiles) {
        this.excelInputFiles = excelInputFiles;

        // Iterate through list of runs and run the model with server to establish connection with FlexSim
        while (currentRunNum < this.excelInputFiles.size()) {
            runModel();
            server.checkForConnection();
            excelOutputFiles.add(new File(getFullPath(outputLocation) + excelOutputFileName + ".xlsx"));
            currentRunNum++;
        }

        int i = 1;
        for (File iter : excelOutputFiles) {
            LOGGER.info("Output file "+  i + ": " + iter.toString());
            i++;
        }

        return excelOutputFiles;
    }

    /**
     * Main code the runs the FlexSim program.
     */
    public void runModel() {
        LOGGER.info("Input file path: " + excelInputFiles.get(currentRunNum).toString());
        String tempInputFile = excelInputFiles.get(currentRunNum).toString();
        inputFile =  getBaseName(tempInputFile) + "." + getExtension(tempInputFile);
        inputLocation = getFullPath(tempInputFile).replace("\\", "\\\\");
        excelOutputFileName = getBaseName(inputFile).substring(0,getBaseName(inputFile).lastIndexOf("_")) + "_output";
        deleteExistingFile(getFullPath(outputLocation) + excelOutputFileName + ".xlsx");

        try {
           scriptFile = scriptGenerator.generateScript(inputLocation, inputFile, outputLocation, outputFile,
                   excelOutputFileName);
            Runtime.getRuntime().exec(commandLineGenerator(isModelShown));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates the command to execute model.
     * @return command
     */
    public String commandLineGenerator(boolean isModelShown) {
        String command = '"' + flexsimLocation + '"' +
                '"' + modelLocation + '"' + " /maintenance " + (isModelShown ? "" : "nogui_") + "runscript " +
                "/scriptpath "  + '"' + scriptFile.getAbsolutePath() + '"' ;
        return command;
    }

    /**
     * Deletes existing output files to prevent Excel overwrite popup.
     * @param pathname
     */
    public void deleteExistingFile(String pathname) {
        try {
            File f = new File(pathname);                         //file to be delete
            if (f.delete()) {                                    //returns Boolean value
                LOGGER.info( f.getName() + " was deleted");   //getting and printing the file name
            } else {
                LOGGER.info(pathname.replace("\\\\\\\\\\", "\\") + " already doesn't exist");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
