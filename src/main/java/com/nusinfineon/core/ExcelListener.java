package com.nusinfineon.core;

import static com.nusinfineon.util.FlexScriptDefaultCodes.GETPROCESSTIMECODE;
import static com.nusinfineon.util.FlexScriptDefaultCodes.MAIN15CODE;
import static com.nusinfineon.util.FlexScriptDefaultCodes.ONRUNSTOPCODE;
import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.apache.commons.io.FilenameUtils.getFullPath;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.pretty_tools.dde.DDEException;
import com.pretty_tools.dde.DDEMLException;
import com.pretty_tools.dde.client.DDEClientConversation;
import com.pretty_tools.dde.client.DDEClientEventListener;

/**
 * Class to generate an excel listener using DDE
 */
public class ExcelListener {

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
    private File statusFile;
    private String scriptFilepath = "./script.txt";
    private File scriptFile;
    private int currentRunNum;
    private String excelOutputFileName;
    private ArrayList<File> excelInputFiles;
    private ArrayList<File> excelOutputFiles;
    private ArrayList<Integer> batchSizes;
    private DDEClientConversation conversation;

    private final String DEFAULT_STATUS_SHEET = "#()STATUS#()#";

    public ExcelListener(String flexsimLocation, String modelLocation, String outputLocation,
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
        statusFile = generateExcelStatusFile(getFullPath(outputLocation));

        // DDE client
        conversation = new DDEClientConversation();
        // We can use UNICODE format if server prefers it
        //conversation.setTextFormat(ClipboardFormat.CF_UNICODETEXT);
        try {
            //creates the event listener methods
            conversation.setEventListener(new DDEClientEventListener() {
                public void onDisconnect() {
                    System.out.println("onDisconnect()");
                }

                //function called when a change is detected in the status excel file
                public void onItemChanged(String topic, String item, String data) {
                    excelOutputFiles.add(new File(getFullPath(outputLocation) + excelOutputFileName + ".xlsx"));
                    if (currentRunNum == batchSizes.size()) {
                        System.out.println("onItemChanged(" + topic + "," + item + "," + data.trim() + " i = " + currentRunNum + ")");
                        System.out.println("No. of min. batch sizes = " + batchSizes.size());
                        int i = 1;
                        for (File iter : excelOutputFiles) {
                            System.out.println("output file "+  i + ": " + iter.toString());
                            i++;
                        }
                        try {
                            endRuns();
                        } catch (DDEException e) {
                            e.printStackTrace();
                        }
                    } else {
                        System.out.println("onItemChanged(" + topic + "," + item + "," + data.trim() + " i = " + currentRunNum + ")");
                        if (data.trim().equals("finished")) {
                            runModel(currentRunNum == batchSizes.size()-1);
                        }
                    }
                    currentRunNum++;
                }
            });

            Desktop.getDesktop().open(statusFile);

            boolean fileIsNotLocked = statusFile.renameTo(statusFile);
            while (fileIsNotLocked) {
                TimeUnit.SECONDS.sleep(1);
                fileIsNotLocked = statusFile.renameTo(statusFile);
            }

            TimeUnit.SECONDS.sleep(5);

            System.out.println("Connecting...");
            conversation.connect("Excel", DEFAULT_STATUS_SHEET);
            conversation.startAdvice("R1C1");
            System.out.println("Connected!!");

        } catch (DDEMLException e) {
            System.out.println("DDEMLException: 0x" + Integer.toHexString(e.getErrorCode()) + " " + e.getMessage());
        } catch (DDEException e) {
            System.out.println("DDEClientException: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }
    }

    /**
     * Main execute function to start runs
     * @throws DDEException
     */
    public void executeRuns(ArrayList<File> excelInputFiles, ArrayList<Integer> batchSizes, String lotSequencingRule,
                            ArrayList<File> excelOutputFiles) throws InterruptedException {
        this.excelOutputFiles = excelOutputFiles;
        this.excelInputFiles = excelInputFiles;
        this.batchSizes = batchSizes;
        this.lotSequencingRule = lotSequencingRule.replaceAll(" ", "_").toLowerCase();
        this.excelOutputFiles.clear();

        runModel(currentRunNum == batchSizes.size()-1);
        currentRunNum++;

        while (currentRunNum <= batchSizes.size()){
            TimeUnit.SECONDS.sleep(1);
        }
    }

    /**
     * Terminates runs
     * @throws DDEException
     */
    public void endRuns() throws DDEException {
        System.out.println("Runs terminated!");
        conversation.disconnect();
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
        String statusFilepath = statusFile.getAbsolutePath().replace("\\" , "\\\\\\\\");
        scriptFile = new File(scriptFilepath);
        scriptFile.createNewFile();
        FileWriter fileWriter = new FileWriter(scriptFilepath);
        fileWriter.write(runSpeed + "\n"
                + stopTime + "showprogressbar(\"\");\n"
                + "MAIN2LoadData (\"" + inputLocation + "\"," + inputFile + "\");\n"
                + editNodeCode("RunStop", "MODEL://Tools//OnRunStop", "concat(" + ONRUNSTOPCODE
                + ",\"MAIN15WriteReports(true, \\\""
                + outputLocation + "\", " + "\\\"" + outputFile
                + "\\\" , \\\"" + excelOutputFileName + "\\\");"
                + "\\n hideprogressbar();\\n\\texcelopen(\\\""
                + statusFilepath
                + "\\\");\\n\\tmaintenance(1000, 1);\\n\\texcelsetsheet(\\\"#()STATUS#()#\\\");\\n\\texcelwritestr(1,1,\\\"finished\\\");"
                +  (isLastRun ? "\\nexcelclose(0);" : "")
                + "\\ncmdexit ();\\n}\")")
                + editNodeCode("ProcessTime", "MODEL:/Tools/UserCommands/ProcessTimeGetTotal/code", GETPROCESSTIMECODE)
                + editNodeCode("MAIN15", "MODEL://Tools/UserCommands//MAIN15WriteReports//code", MAIN15CODE)
                + "MAINBuldAndRun ();\nresetmodel();\ngo();");
        fileWriter.close();
    }

    public File generateExcelStatusFile(String outputLocation) throws IOException {
        File excel = new File("./FlexsimControllerStatus.xlsx");

        if (excel.createNewFile()) {
            Workbook wb = new XSSFWorkbook();
            OutputStream fileOut = new FileOutputStream(excel);
            wb.createSheet("#()STATUS#()#");
            System.out.println("Sheets Has been created successfully");
            wb.write(fileOut);
            fileOut.close();
        }
        return excel;
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
