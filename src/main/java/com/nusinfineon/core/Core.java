package com.nusinfineon.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static com.nusinfineon.util.FlexScriptDefaultCodes.MAIN15CODE;
import static com.nusinfineon.util.FlexScriptDefaultCodes.ONRUNSTOPCODE;
import static com.nusinfineon.util.FlexScriptDefaultCodes.TIMEBLOCKCODE;
import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.apache.commons.io.FilenameUtils.getFullPath;

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
    private boolean isModelShown;


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
                        String stopTime, boolean isModelShown) throws IOException {
        file = new File(scriptFilepath);
        if (file.createNewFile()){}
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
     */
    public void inputData(String flexsimLocation, String modelLocation, String inputLocation,
                          String outputLocation, String runSpeed, String warmUpPeriod, String stopTime){
        this.flexsimLocation = flexsimLocation;
        this.modelLocation = modelLocation;
        this.inputLocation = inputLocation;
        this.outputLocation = outputLocation;
        this.runSpeed = runSpeed;
        this.warmUpPeriod = warmUpPeriod;
        this.stopTime = stopTime;
    }

    /**
     * creates the commandline to execute model
     * @throws IOException
     */
    public void commandLineGenerator (boolean isModelShown) throws IOException {
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
            + stopTime + "\n" /*+ "MAIN2LoadData (\"" + inputLocation + "\"," + inputFile + "\");\n"*/
            + editNodeCode("RunStop", "MODEL://Tools//OnRunStop", "concat(" + ONRUNSTOPCODE
                +  ",\"MAIN15WriteReports(true, \\\""
                +outputLocation  + "\", " + "\\\"" + outputFile
                + "\\\" , \\\"OutputNew\\\");\\n\\thideprogressbar();\\n}\")" )
            + editNodeCode("timeBlock", "VIEW://active//MainPanel//RunPanel//Time Block>hotlinkx", TIMEBLOCKCODE)
            + editNodeCode("MAIN15", "MODEL://Tools/UserCommands//MAIN15WriteReports//code", MAIN15CODE)
            + "MAINBuldAndRun ();\nresetmodel();\nshowprogressbar(\"\");go();");
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

    /**
     * Deletes existing output files to prevent excel overwrite popup
     * @param pathname
     */
    public void deleteExistingFile (String pathname){
        try
        {
            File f= new File(pathname);           //file to be delete
            if(f.delete())                      //returns Boolean value
            {
                System.out.println(f.getName() + " deleted");   //getting and printing the file name
            }
            else
            {
                System.out.println(pathname + "doesn't exist");
            }
        }
        catch(Exception e)
        {
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
    public String editNodeCode (String name ,String nodePath , String code){
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
