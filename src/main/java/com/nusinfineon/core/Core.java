package com.nusinfineon.core;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Core {

    private String flexsimLocation;
    private String modelLocation;
    private String inputLocation;
    private String inputFile;
    private String runSpeed;
    private String warmUpPeriod;
    private String stopTime;
    private String scriptFilepath = "./script.txt";
    private File file;


    public void execute(String flexsimLocation, String modelLocation, String inputLocation,
                        String runSpeed, String warmUpPeriod, String stopTime) throws IOException {
        file = new File(scriptFilepath);
        if (file.createNewFile()){}
        this.flexsimLocation = '"' + flexsimLocation + '"';
        this.modelLocation = '"' + modelLocation + '"';
        inputFile = '"' + FilenameUtils.getBaseName(inputLocation)
                + "." + FilenameUtils.getExtension(inputLocation) + "\");";
        this.inputLocation =  "MAIN2LoadData (\"" +
                FilenameUtils.getFullPath(inputLocation).replace("\\", "\\\\") + "\",";
        this.runSpeed = "runspeed(" + runSpeed + ");" ;
        this.stopTime = "stoptime(" + stopTime + ");";
        scripCreator();
        commandLineGenerator();
    }

    public void inputData(String flexsimLocation, String modelLocation, String inputLocation,
                          String runSpeed, String warmUpPeriod, String stopTime){
        this.flexsimLocation = flexsimLocation;
        this.modelLocation = modelLocation;
        this.inputLocation = inputLocation;
        this.runSpeed = runSpeed;
        this.warmUpPeriod = warmUpPeriod;
        this.stopTime = stopTime;
    }

    public void commandLineGenerator () throws IOException {
        Process a = Runtime.getRuntime().exec(flexsimLocation +
                modelLocation + " /maintenance runscript " +
                "/scriptpath" + file.getAbsolutePath() );
    }

    public void scripCreator() throws IOException {
        FileWriter fileWriter = new FileWriter(scriptFilepath);
        fileWriter.write(runSpeed + "\n"
        + stopTime + "\n" + inputLocation + inputFile + "\n"
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

    public String getInputFile() {
        return inputFile;
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
