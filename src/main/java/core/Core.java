package core;

import java.io.IOException;

public class Core {

    String flexsimLocation;
    String modelLocation;
    String inputLocation;
    double runTime;
    double warmUpPeriod;
    double 

    public Core() throws IOException {
    }


    public void execute() throws IOException {
        Process a = Runtime.getRuntime().exec("\"C:\\Program Files (x86)\\FlexSim7.3\\program\\flexsim.exe\" " +
                "\"C:\\Users\\lingz\\Documents\\y4 sem1\\SDP\\onelevel IBIS\\IBIS Model V1.013 PreCreated.fsm\" /maintenance runscript " +
                "/scriptpath \"C:\\Users\\lingz\\Documents\\y4 sem1\\SDP\\2107-runflexsimmodelsilently\\script.txt\"");
    }
}
