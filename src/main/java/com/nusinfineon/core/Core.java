package com.nusinfineon.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static com.nusinfineon.util.FlexScriptDefaultCodes.ONRUNSTOPCODE;
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


    public void execute(String flexsimLocation, String modelLocation, String inputLocation,
                        String outputLocation, String runSpeed, String warmUpPeriod, String stopTime) throws IOException {
        file = new File(scriptFilepath);
        if (file.createNewFile()){}
        this.flexsimLocation = '"' + flexsimLocation + '"';
        this.modelLocation = '"' + modelLocation + '"';
        inputFile = '"' + getBaseName(inputLocation)
                + "." + getExtension(inputLocation) + "\");";
        this.inputLocation =  "MAIN2LoadData (\"" +
                getFullPath(inputLocation).replace("\\", "\\\\") + "\",";
        deleteExistingFile( getFullPath(outputLocation) + "OutputNew.xlsx");
        outputFile = "\\\"" + getBaseName(outputLocation)
                + "." + getExtension(outputLocation) + "\\\" , \\\"OutputNew\\\");\\n}\");\n";
        this.outputLocation =  ",\"MAIN15WriteReports(true, \\\"" +
                getFullPath(outputLocation).replace("\\", "\\\\\\\\\\") + "\", ";
        this.runSpeed = "runspeed(" + runSpeed + ");" ;
        this.warmUpPeriod = warmUpPeriod;
        this.stopTime = "stoptime(" + stopTime + ");";
        scriptCreator();
        commandLineGenerator();
    }

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

    public void commandLineGenerator () throws IOException {
        Process a = Runtime.getRuntime().exec(flexsimLocation +
                modelLocation + " /maintenance runscript " +
                "/scriptpath" + file.getAbsolutePath() );
    }

    public void scriptCreator() throws IOException {
        FileWriter fileWriter = new FileWriter(scriptFilepath);
        fileWriter.write(runSpeed + "\n"
        + stopTime + "\n" + /*inputLocation + inputFile +*/ "\n"
        + "treenode triggernode = node(\"MODEL://Tools//OnRunStop\");\n"
        + "string triggercode = concat(" + ONRUNSTOPCODE + outputLocation + outputFile
        + "\nsetnodestr(triggernode,triggercode);\n"
        + "enablecode(triggernode);\n"
        + "buildnodeflexscript(triggernode);\n"
        + "MAINBuldAndRun ();\nresetmodel();\ngo();\n"
       );
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
}
