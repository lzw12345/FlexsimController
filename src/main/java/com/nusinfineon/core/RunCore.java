package com.nusinfineon.core;

import static com.nusinfineon.util.FlexScriptDefaultCodes.GET_PROCESS_TIME_CODE;
import static com.nusinfineon.util.FlexScriptDefaultCodes.MAIN_15_CODE;
import static com.nusinfineon.util.FlexScriptDefaultCodes.ON_RUN_STOP_CODE;
import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.apache.commons.io.FilenameUtils.getFullPath;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Class to generate a server to connect with FlexSim for running the simulation runs
 */
public class RunCore {

    private String flexsimLocation;
    private String modelLocation;
    private String inputLocation;
    private String inputFile;
    private String outputLocation;
    private String outputFile;
    private String runSpeed;
    private boolean isModelShown;
    private String stopTime;
    private String scriptFilepath = "./script.txt";
    private File scriptFile;
    private int currentRunNum;
    private String excelOutputFileName;
    private ArrayList<File> excelInputFiles;
    private ArrayList<File> excelOutputFiles;

    public RunCore(String flexsimLocation, String modelLocation, String outputLocation,
                   String runSpeed, String stopTime, boolean isModelShown) {

        this.flexsimLocation = flexsimLocation;
        this.modelLocation = modelLocation;
        deleteExistingFile(getFullPath(outputLocation) + "OutputNew.xlsx");
        outputFile = getBaseName(outputLocation) + "." + getExtension(outputLocation);
        this.outputLocation = getFullPath(outputLocation).replace("\\", "\\\\\\\\\\");
        this.runSpeed = "runspeed(" + runSpeed + ");";
        this.stopTime = "stoptime(" + stopTime + ");";
        this.isModelShown = isModelShown;

        currentRunNum = 0;
    }

    /**
     * Main execute function to start runs
     */
    public void executeRuns(ArrayList<File> excelInputFiles, ArrayList<File> excelOutputFiles) {
        this.excelInputFiles = excelInputFiles;
        this.excelOutputFiles = excelOutputFiles;
        this.excelOutputFiles.clear();

        // Iterate through list of runs and run the model with server to establish connection with FlexSim
        while (currentRunNum <= excelInputFiles.size()-1) {
            runModel();
            Server server = new Server(1880);
            excelOutputFiles.add(new File(getFullPath(outputLocation) + excelOutputFileName + ".xlsx"));
            currentRunNum++;
        }
        int i = 1;
        for (File iter : excelOutputFiles) {
            System.out.println("output file "+  i + ": " + iter.toString());
            i++;
        }
    }

    /**
     * Main code the runs the program
     */
    public void runModel() {
        System.out.println("Input file path: " + excelInputFiles.get(currentRunNum).toString());
        String tempInputFile = excelInputFiles.get(currentRunNum).toString();
        inputFile = getBaseName(tempInputFile) + "." + getExtension(tempInputFile);
        inputLocation = getFullPath(tempInputFile).replace("\\", "\\\\");
        excelOutputFileName = getBaseName(inputFile).substring(0,getBaseName(inputFile).lastIndexOf("_")) + "_output";
        deleteExistingFile(getFullPath(outputLocation) + excelOutputFileName + ".xlsx");

        try {
            scriptCreator();
            Runtime.getRuntime().exec(commandLineGenerator(isModelShown));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates the Flexscript for the model
     * @throws IOException
     */
    public void scriptCreator() throws IOException {
        scriptFile = new File(scriptFilepath);
        scriptFile.createNewFile();
        FileWriter fileWriter = new FileWriter(scriptFilepath);
        fileWriter.write(runSpeed + "\n"
                + stopTime + "\n"
                + "MAIN2LoadData (\"" + inputLocation + "\"," + inputFile + "\");\n"
                + "excellaunch();\n"
                + editNodeCode("RunStop", "MODEL://Tools//OnRunStop", "concat(" + ON_RUN_STOP_CODE
                + ",\"MAIN15WriteReports(true, \\\""
                + outputLocation + "\", " + "\\\"" + outputFile
                + "\\\" , \\\"" + excelOutputFileName + "\\\");"
                + "\\nhideprogressbar();"
                + "\\nsocketinit();"
                + "\\nint socknum = clientcreate();"
                + "\\nclientconnect(socknum,\\\"127.0.0.1\\\",1880);"
                + "\\nclientsend(socknum,\\\"REQ:service\\\")\\n;"
                + "\\nclientclose (socknum);"
                + "\\nsocketend();"
                + "\\ncmdexit ();\\n}\")")
                + editNodeCode("ProcessTime", "MODEL:/Tools/UserCommands/ProcessTimeGetTotal/code", GET_PROCESS_TIME_CODE)
                + editNodeCode("MAIN15", "MODEL://Tools/UserCommands//MAIN15WriteReports//code", MAIN_15_CODE)
                + "MAINBuldAndRun ();\nresetmodel();\ngo();");
        fileWriter.close();
    }

    /**
     * Function to generate a default template for replace code in a Flexsim node
     * @param name
     * @param nodePath
     * @param code
     * @return script
     */
    public String editNodeCode(String name, String nodePath, String code) {
        String nodename = name + "Node";
        String codeName = name + "Code";
        String script = "treenode " + nodename + " = node(\"" + nodePath + "\");\n"
                + "string " + codeName + " = " + code + ";\n"
                + "setnodestr(" + nodename + "," + codeName + ");\n"
                + "enablecode(" + nodename + ");\n"
                + "buildnodeflexscript(" + nodename + ");\n";
        return script;
    }

    /**
     * Creates the commandline to execute model
     * @throws IOException
     * @return command
     */
    public String commandLineGenerator(boolean isModelShown) {
        String command = '"' + flexsimLocation + '"' +
                '"' + modelLocation + '"' + " /maintenance " + (isModelShown ? "" : "nogui_") + "runscript " +
                "/scriptpath "  + '"' + scriptFile.getAbsolutePath() + '"' ;
        return command;
    }

    /**
     * Deletes existing output files to prevent excel overwrite popup
     * @param pathname
     */
    public void deleteExistingFile(String pathname) {
        try {
            File f = new File(pathname);                         //file to be delete
            if (f.delete()) {                                    //returns Boolean value
                System.out.println(f.getName() + " deleted");   //getting and printing the file name
            } else {
                System.out.println(pathname + " doesn't exist");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
