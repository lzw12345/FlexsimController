package com.nusinfineon.util;

public class Messages {
    private static final String SAVE_FILE = "saveFile.txt";

    public static final String ABOUT_MESSAGE = "This application is an optimised interface for the IBIS simulation model on FlexSim.\n"
            + "\nYou can try a range of minimum batch sizes to simulate based on the supplied input information.\n"
            + "\nYou can also choose a lot sequencing rule which sorts the supplied lots in the desired sequence, and define a few other settings, for the simulation.\n"
            + "\nEnsure all fields are filled in correctly before running the simulation!\n"
            + "\nAll input fields are automatically saved upon running and exiting, and automatically loaded the next time the program is opened.\n";

    public static final String CONFIRM_SAVE_MESSAGE = "The input data will be saved in \"" + SAVE_FILE + "\" in the folder of \"IBIS_Simulation.exe\"."
            + "\nAny save files in the folder will be overwritten!";

    public static final String CONFIRM_LOAD_MESSAGE = "The input data will be loaded from \"" + SAVE_FILE + "\" in the folder of \"IBIS_Simulation.exe\"."
            + "\nEnsure that the save file is previously saved from the program.";

    public static final String CONFIRM_RUN_MESSAGE = "Warning: The more runs there are, the longer it will take until completion."
            + "\nPreviously generated output files in the Output folder will be overwritten."
            + "\nPlease save and close all opened files on Excel before you click OK.";

    public static final String SAVE_FILE_WRONG_FORMAT_MESSAGE = SAVE_FILE
            + " is of the wrong format!\nPlease place a previously saved file into the folder of \"IBIS_Simulation.exe\".";

    public static final String SAVE_FILE_NOT_FOUND_MESSAGE = SAVE_FILE
            + " cannot be found!\nPlease place a previously saved file into the folder of \"IBIS_Simulation.exe\".";
}
