package com.nusinfineon.core;

import com.pretty_tools.dde.DDEException;
import com.pretty_tools.dde.DDEMLException;
import com.pretty_tools.dde.client.DDEClientConversation;
import com.pretty_tools.dde.client.DDEClientEventListener;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static com.nusinfineon.util.FlexScriptDefaultCodes.GETPROCESSTIMECODE;
import static com.nusinfineon.util.FlexScriptDefaultCodes.MAIN15CODE;
import static com.nusinfineon.util.FlexScriptDefaultCodes.ONRUNSTOPCODE;
import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.apache.commons.io.FilenameUtils.getFullPath;

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
    private String warmUpPeriod;
    private boolean isModelShown;
    private String stopTime;
    private File statusFile;
    private String scriptFilepath = "./script.txt";
    private File scriptFile;
    private int currentRunNum;
    private String excelOutputFileName;
    private ArrayList<File> excelInputFiles;
    private ArrayList<File> excelOutputFiles;
    private ArrayList<Integer> batchSizes;
    private DDEClientConversation conversation;

    private final String DEFAULTSTATUSSHEET = "#()STATUS#()#";


    public ExcelListener(ArrayList<File> excelFiles, ArrayList<Integer> batchSizes, String flexsimLocation, String modelLocation,
                         String outputLocation,
                         String runSpeed, String warmUpPeriod, String stopTime, boolean isModelShown,
                         ArrayList<File> excelOutputFiles  ) throws IOException {

        this.flexsimLocation = flexsimLocation;
        this.modelLocation = modelLocation;
        deleteExistingFile(getFullPath(outputLocation) + "OutputNew.xlsx");
        outputFile = getBaseName(outputLocation) + "." + getExtension(outputLocation);
        this.outputLocation = getFullPath(outputLocation).replace("\\", "\\\\\\\\\\");
        this.runSpeed = "runspeed(" + runSpeed + ");";
        this.warmUpPeriod = warmUpPeriod;
        this.stopTime = "stoptime(" + stopTime + ");";
        this.isModelShown = isModelShown;
        this.excelOutputFiles = excelOutputFiles;
        this.excelInputFiles = excelFiles;
        this.batchSizes = batchSizes;
        this.excelOutputFiles.clear();

        currentRunNum = 0;
        statusFile = generateExcelStatusFile(getFullPath(outputLocation));

        // DDE client
        conversation = new DDEClientConversation();
        // We can use UNICODE format if server prefers it
        //conversation.setTextFormat(ClipboardFormat.CF_UNICODETEXT);
        try {
            //creates the event listener methods
            conversation.setEventListener(new DDEClientEventListener() {
                public void onDisconnect()
                {
                    System.out.println("onDisconnect()");

                }

                //function called when a change is detected in the status excel file
                public void onItemChanged(String topic, String item, String data) {
                    excelOutputFiles.add(new File(getFullPath(outputLocation) + excelOutputFileName + ".xlsx"));
                    if(currentRunNum == batchSizes.size()) {
                        System.out.println(" batchsizes = " + batchSizes.size());
                        for (File iter : excelOutputFiles){
                            System.out.println("outputput file "+  currentRunNum + " = " + iter.toString());
                        }
                        try {
                            endRuns();
                        } catch (DDEException e) {
                            e.printStackTrace();
                        }
                    }else {
                        System.out.println("onItemChanged(" + topic + "," + item + "," + data.trim() + " i = " + currentRunNum + ")");
                        if (data.trim().equals("finished")) {
                            runModel();

                        }
                    }
                    currentRunNum++;
                }
            });

            runModel();
            currentRunNum++;

            Desktop.getDesktop().open(statusFile);

            boolean fileIsNotLocked = statusFile.renameTo(statusFile);
            while (fileIsNotLocked) {
                TimeUnit.SECONDS.sleep(1);

                fileIsNotLocked = statusFile.renameTo(statusFile);
            }



            System.out.println("Connecting...");
            conversation.connect("Excel", DEFAULTSTATUSSHEET);
            try {

                conversation.startAdvice("R1C1");
                System.out.println("Connected!!");
                while (currentRunNum <= batchSizes.size() ){

                    TimeUnit.SECONDS.sleep(1);
                }
            } finally {
                //conversation.disconnect();
            }
        } catch (DDEMLException e) {
            System.out.println("DDEMLException: 0x" + Integer.toHexString(e.getErrorCode()) + " " + e.getMessage());
        } catch (DDEException e) {
            System.out.println("DDEClientException: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }



    }

    /**
     * generates a listner and starts multiple runs of the program
     * @throws IOException
     */
    public void startIterations() throws IOException, DDEException {
        currentRunNum = 0;
        statusFile = generateExcelStatusFile(getFullPath(outputLocation));

            // DDE client
            conversation = new DDEClientConversation();
            // We can use UNICODE format if server prefers it
            //conversation.setTextFormat(ClipboardFormat.CF_UNICODETEXT);
        try {
            //creates the event listener methods
            conversation.setEventListener(new DDEClientEventListener() {
                public void onDisconnect()
                {
                    System.out.println("onDisconnect()");

                }

                //function called when a change is detected in the status excel file
                public void onItemChanged(String topic, String item, String data) {
                    excelOutputFiles.add(new File(getFullPath(outputLocation) + excelOutputFileName + ".xlsx"));
                    if(currentRunNum == batchSizes.size()) {
                        System.out.println(" batchsizes = " + batchSizes.size());
                            for (File iter : excelOutputFiles){
                                System.out.println("outputput file 1 = " + iter.toString());
                            }

                    }else {
                        System.out.println("onItemChanged(" + topic + "," + item + "," + data.trim() + " i = " + currentRunNum + ")");
                        if (data.trim().equals("finished")) {
                            runModel();
                            currentRunNum++;
                        }
                    }

                }
            });

            runModel();
            currentRunNum++;

            Desktop.getDesktop().open(statusFile);

            boolean fileIsNotLocked = statusFile.renameTo(statusFile);
            while (fileIsNotLocked) {
                TimeUnit.SECONDS.sleep(1);

                fileIsNotLocked = statusFile.renameTo(statusFile);
            }



            System.out.println("Connecting...");
            conversation.connect("Excel", DEFAULTSTATUSSHEET);
            try {

                conversation.startAdvice("R1C1");
                System.out.println("Connected!!");
                //TimeUnit.SECONDS.sleep(20);
                //System.out.println("ending");
               // conversation.stopAdvice("R1C1");
            } finally {
                //conversation.disconnect();
            }
        } catch (DDEMLException e) {
            System.out.println("DDEMLException: 0x" + Integer.toHexString(e.getErrorCode()) + " " + e.getMessage());
        } catch (DDEException e) {
            System.out.println("DDEClientException: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }


    }

    /**
     * terminates runs
     * @throws DDEException
     */
    public void endRuns () throws DDEException {
        System.out.println("Runs terminated early!");
        conversation.stopAdvice("R1C1");
    }



    //main code the runs the program
    public void runModel (){

        System.out.println("Batch size: " + batchSizes.get(currentRunNum) + ". File path: " + excelInputFiles.get(currentRunNum).toString());
        String tempInputFile = excelInputFiles.get(currentRunNum).toString();
        inputFile = '"' + getBaseName(tempInputFile) + "." + getExtension(tempInputFile);
        inputLocation = getFullPath(tempInputFile).replace("\\", "\\\\");
        excelOutputFileName = "outputFileForBatchofSize " + batchSizes.get(currentRunNum);
        deleteExistingFile(getFullPath(outputLocation) + excelOutputFileName + ".xlsx");
        //generate status file and make sure its open

        try {
            scriptCreator();
            Process a = Runtime.getRuntime().exec(commandLineGenerator(isModelShown));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /**
     * Creates the Flexscript for the model
     *
     * @throws IOException
     */
    public void scriptCreator() throws IOException {
        scriptFile = new File(scriptFilepath);
        scriptFile.createNewFile();
        FileWriter fileWriter = new FileWriter(scriptFilepath);
        fileWriter.write(runSpeed + "\n"
                + stopTime + "showprogressbar(\"\");\n"
                + "MAIN2LoadData (\"" + inputLocation + "\"," + inputFile + "\");\n"
                + editNodeCode("RunStop", "MODEL://Tools//OnRunStop", "concat(" + ONRUNSTOPCODE
                + ",\"MAIN15WriteReports(true, \\\""
                + outputLocation + "\", " + "\\\"" + outputFile
                + "\\\" , \\\"" + excelOutputFileName + "\\\");\\n hideprogressbar();\\n\\texcelopen(\\\"C:\\\\\\\\Users\\\\\\\\lingz\\\\\\\\Documents\\\\\\\\y4 sem1\\\\\\\\SDP\\\\\\\\onelevel IBIS\\\\\\\\FlexsimControllerStatus.xlsx\\\");\\n\\texcelsetsheet(\\\"sheet1\\\");\\n\\texcelwritestr(1,1,\\\"finished\\\");\\ncmdexit ();\\n}\")")
                + editNodeCode("ProcessTime", "MODEL:/Tools/UserCommands/ProcessTimeGetTotal/code", GETPROCESSTIMECODE)
                + editNodeCode("MAIN15", "MODEL://Tools/UserCommands//MAIN15WriteReports//code", MAIN15CODE)
                + "MAINBuldAndRun ();\nresetmodel();\ngo();");
        fileWriter.close();

    }

    public File generateExcelStatusFile(String outputLocation) throws IOException {

        File excel = new File(outputLocation + "FlexsimControllerStatus.xlsx");

        if(excel.createNewFile()) {
            Workbook wb = new XSSFWorkbook();
            OutputStream fileOut = new FileOutputStream(excel);
            wb.createSheet("#()STATUS#()#");
            System.out.println("Sheets Has been Created successfully");
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
                "/scriptpath" + scriptFile.getAbsolutePath();

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
