package core;

import java.io.IOException;

public class Core {

    String flexsimLocation;
    String modelLocation;
    String inputLocation;
    double runTime;
    double warmUpPeriod;
    double stopTime;

    public Core() throws IOException {
    }

    public void execute(String flexsimLocation, String modelLocation, String inputLocation,
                        double runTime, double warmUpPeriod, double stopTime) throws IOException {
        this.flexsimLocation = '"' + flexsimLocation + '"';
        this.modelLocation = '"' + modelLocation + '"';
        this.inputLocation = '"' + inputLocation + '"';
        System.out.print(flexsimLocation);
        commandLineGenerator();
    }

    public void commandLineGenerator () throws IOException {
        Process a = Runtime.getRuntime().exec(flexsimLocation +
                modelLocation + " /maintenance runscript " +
                "/scriptpath \"C:\\Users\\lingz\\Documents\\y4 sem1\\SDP\\2107-runflexsimmodelsilently\\script.txt\"");
    }
}
