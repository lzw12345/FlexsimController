package com.nusinfineon.core;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static com.nusinfineon.util.FlexScriptDefaultCodes.ONRUNSTOPCODE;

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
        inputFile = '"' + FilenameUtils.getBaseName(inputLocation)
                + "." + FilenameUtils.getExtension(inputLocation) + "\");";
        this.inputLocation =  "MAIN2LoadData (\"" +
                FilenameUtils.getFullPath(inputLocation).replace("\\", "\\\\") + "\",";
        outputFile = "\\\"" + FilenameUtils.getBaseName(outputLocation)
                + "." + FilenameUtils.getExtension(outputLocation) + "\\\" , \\\"Output\\\");\\n}\");\n";
        this.outputLocation =  ",\"MAIN15WriteReports(true, \\\"" +
                FilenameUtils.getFullPath(inputLocation).replace("\\", "\\\\\\\\\\") + "\", ";
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
}
