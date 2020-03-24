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

import com.pretty_tools.dde.DDEException;
import com.pretty_tools.dde.client.DDEClientConversation;

/**
 * Class to generate an excel listener using DDE
 */
public class runCore {

    private String flexsimLocation;
    private String modelLocation;
    private String inputLocation;
    private String inputFile;
    private String outputLocation;
    private String outputFile;
    private String runSpeed;
    private boolean isModelShown;
    private String stopTime;
    private String lotSequencingRule;
    private String scriptFilepath = "./script.txt";
    private File scriptFile;
    private int currentRunNum;
    private String excelOutputFileName;
    private ArrayList<File> excelInputFiles;
    private ArrayList<File> excelOutputFiles;
    private ArrayList<Integer> batchSizes;
    private DDEClientConversation conversation;


    public runCore(String flexsimLocation, String modelLocation, String outputLocation,
                   String runSpeed, String stopTime, boolean isModelShown) throws IOException {

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
     * @throws DDEException
     */
    public void executeRuns(ArrayList<File> excelInputFiles, ArrayList<Integer> batchSizes, String lotSequencingRule,
                            ArrayList<File> excelOutputFiles) throws InterruptedException, DDEException {
        this.excelOutputFiles = excelOutputFiles;
        this.excelInputFiles = excelInputFiles;
        this.batchSizes = batchSizes;
        this.lotSequencingRule = lotSequencingRule.replaceAll(" ", "_").toLowerCase();
        this.excelOutputFiles.clear();

        while (currentRunNum <= batchSizes.size()-1) {
            runModel(currentRunNum == batchSizes.size() - 1);
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
    public void runModel(boolean isLastRun) {
        System.out.println("Min batch size: " + batchSizes.get(currentRunNum) + ". Input file path: " + excelInputFiles.get(currentRunNum).toString());
        String tempInputFile = excelInputFiles.get(currentRunNum).toString();
        inputFile = '"' + getBaseName(tempInputFile) + "." + getExtension(tempInputFile);
        inputLocation = getFullPath(tempInputFile).replace("\\", "\\\\");
        excelOutputFileName = "min_" + batchSizes.get(currentRunNum) + "_BIB_" + lotSequencingRule + "_output";
        deleteExistingFile(getFullPath(outputLocation) + excelOutputFileName + ".xlsx");

        try {
            scriptCreator(isLastRun);
            Runtime.getRuntime().exec(commandLineGenerator(isModelShown));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates the Flexscript for the model
     *
     * @throws IOException
     */
    public void scriptCreator(boolean isLastRun) throws IOException {
        scriptFile = new File(scriptFilepath);
        scriptFile.createNewFile();
        FileWriter fileWriter = new FileWriter(scriptFilepath);
        fileWriter.write(runSpeed + "\n"
                + stopTime
                // TODO: Uncomment:
                //+ "MAIN2LoadData (\"" + inputLocation + "\"," + inputFile + "\");\n"
                + "excellaunch();"
                + editNodeCode("RunStop", "MODEL://Tools//OnRunStop", "concat(" + ONRUNSTOPCODE
                + ",\"MAIN15WriteReports(true, \\\""
                + outputLocation + "\", " + "\\\"" + outputFile
                + "\\\" , \\\"" + excelOutputFileName + "\\\");"
                + "\\n hideprogressbar();"
                + "\\nsocketinit();"
                + "\\nint socknum = clientcreate();"
                + "\\nclientconnect(socknum,\\\"127.0.0.1\\\",1880);"
                + "\\nclientsend(socknum,\\\"REQ:service\\\")\\n;"
                + "\\nclientclose (socknum);"
                + "\\nsocketend();"
                + "\\ncmdexit ();\\n}\")")
                + editNodeCode("ProcessTime", "MODEL:/Tools/UserCommands/ProcessTimeGetTotal/code", GETPROCESSTIMECODE)
                + editNodeCode("MAIN15", "MODEL://Tools/UserCommands//MAIN15WriteReports//code", MAIN15CODE)
                + "MAINBuldAndRun ();\nresetmodel();\ngo();");
        fileWriter.close();
    }

    /**
     * Function to generate a default template for replace code in a Flexsim node
     *
     * @param name
     * @param nodePath
     * @param code
     * @return
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
     *
     * @throws IOException
     */
    public String commandLineGenerator(boolean isModelShown) throws IOException {
        String command = '"' + flexsimLocation + '"' +
                '"' + modelLocation + '"' + " /maintenance " + (isModelShown ? "" : "nogui_") + "runscript " +
                "/scriptpath "  + '"' + scriptFile.getAbsolutePath() + '"' ;
        return command;
    }

    /**
     * Deletes existing output files to prevent excel overwrite popup
     *
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
