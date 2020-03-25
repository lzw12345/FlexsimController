package com.nusinfineon.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.nusinfineon.core.Core;
import com.nusinfineon.exceptions.CustomException;
import com.nusinfineon.storage.JsonParser;
import com.nusinfineon.util.Messages;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * Represent the whole window of the user interface, it should contain all units in the user interface.
 */
public class MainGui extends UiPart<Stage> {
    private static final int MAX_ALLOWABLE_BATCH_SIZE = 24;
    private static final int MIN_ALLOWABLE_BATCH_SIZE = 1;
    private static final int MAX_ALLOWABLE_STEP_SIZE = MAX_ALLOWABLE_BATCH_SIZE - MIN_ALLOWABLE_BATCH_SIZE;
    private static final int MIN_ALLOWABLE_STEP_SIZE = 1;
    private static final String FXML = "MainGui.fxml";
    private static final String ICON_APPLICATION = "/images/icon_large.png";
    private static final String SAVE_FILE = "saveFile.txt";

    private Stage primaryStage;
    private Core core;
    private JsonParser jsonParser;

    @FXML
    private HBox exeDragTarget;
    @FXML
    private TextField exeLocation;
    @FXML
    private HBox modelDragTarget;
    @FXML
    private TextField modelFileLocation;
    @FXML
    private HBox inputFileDragTarget;
    @FXML
    private TextField inputFileLocation;
    @FXML
    private HBox outputFileDragTarget;
    @FXML
    private TextField outputFileLocation;
    @FXML
    private TextField runSpeed;
    @FXML
    private TextField stopTime;
    @FXML
    private Spinner<Integer> batchSizeMin;
    @FXML
    private Spinner<Integer> batchSizeMax;
    @FXML
    private Spinner<Integer> batchSizeStep;
    @FXML
    private ChoiceBox<String> lotSequencingRule;
    @FXML
    private RadioButton resourceSelectCriteria1;
    @FXML
    private RadioButton resourceSelectCriteria2;
    @FXML
    private RadioButton resourceSelectCriteria3;
    @FXML
    private RadioButton resourceSelectCriteria4;
    @FXML
    private RadioButton lotSelectionCriteria1;
    @FXML
    private RadioButton lotSelectionCriteria2;
    @FXML
    private RadioButton lotSelectionCriteria3;
    @FXML
    private RadioButton trolleyLocationSelectCriteria0;
    @FXML
    private RadioButton trolleyLocationSelectCriteria1;
    @FXML
    private RadioButton trolleyLocationSelectCriteria2;
    @FXML
    private RadioButton bibLoadOnLotCriteria1;
    @FXML
    private RadioButton bibLoadOnLotCriteria2;
    @FXML
    private CheckBox showModel;

    private ToggleGroup resourceSelectCriteria;
    private ToggleGroup lotSelectionCriteria;
    private ToggleGroup trolleyLocationSelectCriteria;
    private ToggleGroup bibLoadOnLotCriteria;

    public MainGui(Stage primaryStage, Core core, JsonParser jsonParser) {
        super(FXML, primaryStage);

        // Set dependencies
        this.primaryStage = primaryStage;
        this.core = core;
        this.jsonParser = jsonParser;

        configureUi();
    }

    private void configureUi() {
        exeLocation.setText(core.getFlexsimLocation());
        modelFileLocation.setText(core.getModelLocation());
        inputFileLocation.setText(core.getInputLocation());
        outputFileLocation.setText(core.getOutputLocation());
        runSpeed.setText(core.getRunSpeed());
        stopTime.setText(core.getStopTime());
        showModel.setSelected(core.getIsModelShown());

        lotSequencingRule.setItems(FXCollections.observableArrayList(core.getLotSequencingRulesList()));
        lotSequencingRule.setValue(core.getLotSequencingRuleString());

        batchSizeMin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                MIN_ALLOWABLE_BATCH_SIZE, MAX_ALLOWABLE_BATCH_SIZE, Integer.parseInt(core.getBatchSizeMinString())));
        batchSizeMax.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                MIN_ALLOWABLE_BATCH_SIZE, MAX_ALLOWABLE_BATCH_SIZE, Integer.parseInt(core.getBatchSizeMaxString())));
        batchSizeStep.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                MIN_ALLOWABLE_STEP_SIZE, MAX_ALLOWABLE_STEP_SIZE, Integer.parseInt(core.getBatchSizeStepString())));

        resourceSelectCriteria = new ToggleGroup();
        resourceSelectCriteria1.setToggleGroup(resourceSelectCriteria);
        resourceSelectCriteria2.setToggleGroup(resourceSelectCriteria);
        resourceSelectCriteria3.setToggleGroup(resourceSelectCriteria);
        resourceSelectCriteria4.setToggleGroup(resourceSelectCriteria);
        resourceSelectCriteria.selectToggle(getResourceSelectCriteria());

        lotSelectionCriteria = new ToggleGroup();
        lotSelectionCriteria1.setToggleGroup(lotSelectionCriteria);
        lotSelectionCriteria2.setToggleGroup(lotSelectionCriteria);
        lotSelectionCriteria3.setToggleGroup(lotSelectionCriteria);
        lotSelectionCriteria.selectToggle(getLotSelectionCriteria());

        trolleyLocationSelectCriteria = new ToggleGroup();
        trolleyLocationSelectCriteria0.setToggleGroup(trolleyLocationSelectCriteria);
        trolleyLocationSelectCriteria1.setToggleGroup(trolleyLocationSelectCriteria);
        trolleyLocationSelectCriteria2.setToggleGroup(trolleyLocationSelectCriteria);
        trolleyLocationSelectCriteria.selectToggle(getTrolleyLocationSelectCriteria());

        bibLoadOnLotCriteria = new ToggleGroup();
        bibLoadOnLotCriteria1.setToggleGroup(bibLoadOnLotCriteria);
        bibLoadOnLotCriteria2.setToggleGroup(bibLoadOnLotCriteria);
        bibLoadOnLotCriteria.selectToggle(getBibLoadOnLotCriteria());
    }

    /** Gets the saved radio button for Resource Select Criteria
     */
    private RadioButton getResourceSelectCriteria() {
        String selection = core.getResourceSelectCriteria();
        switch (selection) {
        case "1":
            return resourceSelectCriteria1;
        case "2":
            return resourceSelectCriteria2;
        case "3":
            return resourceSelectCriteria3;
        default:
            return resourceSelectCriteria4;
        }
    }

    /** Gets the saved radio button for Lot Selection Criteria
     */
    private RadioButton getLotSelectionCriteria() {
        String selection = core.getLotSelectionCriteria();
        switch (selection) {
        case "1":
            return lotSelectionCriteria1;
        case "2":
            return lotSelectionCriteria2;
        default:
            return lotSelectionCriteria3;
        }
    }

    /** Gets the saved radio button for Trolley Location Select Criteria
     */
    private RadioButton getTrolleyLocationSelectCriteria() {
        String selection = core.getTrolleyLocationSelectCriteria();
        switch (selection) {
        case "0":
            return trolleyLocationSelectCriteria0;
        case "1":
            return trolleyLocationSelectCriteria1;
        default:
            return trolleyLocationSelectCriteria2;
        }
    }

    /** Gets the saved radio button for BIB Load on Lot Criteria
     */
    private RadioButton getBibLoadOnLotCriteria() {
        String selection = core.getBibLoadOnLotCriteria();
        switch (selection) {
        case "1":
            return bibLoadOnLotCriteria1;
        default:
            return bibLoadOnLotCriteria2;
        }
    }

    /**
     * for drag and drop functionality
     * @param event
     */
    @FXML
    public void modelDragOver(DragEvent event) {
        if (event.getGestureSource() != modelDragTarget
                && event.getDragboard().hasFiles()) {
            /* allow for both copying and moving, whatever user chooses */
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        }
        event.consume();
    }

    /**
     * for drag and drop functionality
     * @param event
     */
    @FXML
    public void modelDragDropped(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;
        final boolean isAccepted = db.getFiles().get(0).getName().toLowerCase().endsWith(".fsm");

        if (db.hasFiles() && isAccepted ) {
            modelFileLocation.setText(db.getFiles().toString().replaceAll("\\[", "").replaceAll("\\]",""));
            success = true;
        } else {
            showInvalidBox("File must be a FlexSim model with extension .fsm");
        }
        /* let the source know whether the string was successfully
         * transferred and used */
        event.setDropCompleted(success);
        event.consume();
    }

    @FXML
    public void inputDragOver(DragEvent event) {
        if (event.getGestureSource() != inputFileDragTarget
                && event.getDragboard().hasFiles()) {
            /* allow for both copying and moving, whatever user chooses */
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        }
        event.consume();
    }

    @FXML
    public void inputDragDropped(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;
        final boolean isAccepted = db.getFiles().get(0).getName().toLowerCase().endsWith(".xlsx");

        if (db.hasFiles() && isAccepted ) {
            inputFileLocation.setText(db.getFiles().toString().replaceAll("\\[", "").replaceAll("\\]",""));
            success = true;
        } else {
            showInvalidBox("File must be an Excel file with extension .xlsx");
        }
        /* let the source know whether the string was successfully
         * transferred and used */
        event.setDropCompleted(success);
        event.consume();
    }

    @FXML
    public void outputDragOver(DragEvent event) {
        if (event.getGestureSource() != outputFileDragTarget
                && event.getDragboard().hasFiles()) {
            /* allow for both copying and moving, whatever user chooses */
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        }
        event.consume();
    }

    @FXML
    public void outputDragDropped(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;
        final boolean isAccepted = db.getFiles().get(0).getName().toLowerCase().endsWith(".xlsx");

        if (db.hasFiles() && isAccepted ) {
            outputFileLocation.setText(db.getFiles().toString().replaceAll("\\[", "").replaceAll("\\]",""));
            success = true;
        } else {
            showInvalidBox("File must be an Excel file with extension .xlsx");
        }
        /* let the source know whether the string was successfully
         * transferred and used */
        event.setDropCompleted(success);
        event.consume();
    }

    @FXML
    public void exeDragOver(DragEvent event) {
        if (event.getGestureSource() != exeDragTarget
                && event.getDragboard().hasFiles()) {
            /* allow for both copying and moving, whatever user chooses */
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        }
        event.consume();
    }

    @FXML
    public void exeDragDropped(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;
        final boolean isAccepted = db.getFiles().get(0).getName().toLowerCase().endsWith(".exe");

        if (db.hasFiles() && isAccepted) {
            exeLocation.setText(db.getFiles().toString().replaceAll("\\[", "").replaceAll("\\]",""));
            success = true;
        } else {
            showInvalidBox("File must be a executable file with extension .exe");
        }
        /* let the source know whether the string was successfully
         * transferred and used */
        event.setDropCompleted(success);
        event.consume();
    }

    /**
     * Show About box.
     */
    @FXML
    private void handleAbout() {
        String title = "About IBIS Simulation";
        String text = Messages.ABOUT_MESSAGE;

        Alert aboutAlert = raiseAlertBox(Alert.AlertType.INFORMATION, title, null, text, 480, 300);
        aboutAlert.showAndWait();
    }

    /**
     * Resets input fields to default.
     */
    @FXML
    private void handleDefault() {
        core.inputData(null, null, null, null, null, null, false, null, null, null, null, null, null, null, null);
        configureUi();
    }

    /**
     * Saves the current input
     */
    @FXML
    private void handleSave() throws IOException {
        if (confirmSave()) {
            saveInputDataToCore();
            jsonParser.storeData(core);
        }
    }

    /**
     * Loads the saved input
     */
    @FXML
    private void handleLoad() throws IOException {
        if (confirmLoad()) {
            try {
                core = jsonParser.loadData();
                configureUi();
            } catch (UnrecognizedPropertyException e) {
                showErrorBox(SAVE_FILE + " is of the wrong format!\nPlease place a previously saved file into the folder of \"IBIS_Simulation.exe\".");
            } catch (FileNotFoundException e) {
                showErrorBox(SAVE_FILE + " cannot be found!\nPlease place a previously saved file into the folder of \"IBIS_Simulation.exe\".");
            }
        }
    }

    /**
     * Closes the application.
     */
    @FXML
    private void handleExit() {
        saveInputDataToCore();
        primaryStage.hide();
    }

    @FXML
    public void handleModelExecution() throws IOException {
        if (isBlankFiles()) {
            showInvalidBox("File Directories cannot be blank!");
        } else if (!isFoundFiles(exeLocation.getText())) {
            showInvalidBox("FlexSim (.exe) address cannot be found!");
        } else if (!isFoundFiles(modelFileLocation.getText())) {
            showInvalidBox("Model (.fsm) address cannot be found!");
        } else if (!isFoundFiles(inputFileLocation.getText())) {
            showInvalidBox("Input (.xlsx) address cannot be found!");
        } else if (!isFoundFiles(outputFileLocation.getText())) {
            showInvalidBox("Output (.xlsx) address cannot be found!");
        } else if (!isValidExeLocation()) {
            showInvalidBox("FlexSim (.exe) must be the executable file: flexsim.exe");
        } else if (!isValidExtension(modelFileLocation.getText(), "fsm")) {
            showInvalidBox("Model (.fsm) must be a Flexsim model with extension .fsm!");
        } else if (!isValidExtension(inputFileLocation.getText(), "xlsx")) {
            showInvalidBox("Input (.xlsx) must be an Excel file with extension .xlsx!");
        } else if (!isValidExtension(outputFileLocation.getText(), "xlsx")) {
            showInvalidBox("Output (.xlsx) must be an Excel file with extension .xlsx!");
        } else if (isBlankRunParams()) {
            showInvalidBox("Run Parameters cannot be blank!");
        } else if (isNotDouble(runSpeed.getText()) || isNotDouble(stopTime.getText())) {
            showInvalidBox("Run Parameters must be numeric (integer/double)!");
        } else if (!isValidMinBatchSize(batchSizeMin.getValueFactory().getValue())) {
            showInvalidBox("Lowest batch size to run must be at least 1 and at most 24!");
        } else if (!isValidMaxBatchSize(batchSizeMax.getValueFactory().getValue())) {
            showInvalidBox("Highest batch size to run must be at least 1 and at most 24!");
        } else if (!isValidMinMax(batchSizeMin.getValueFactory().getValue(),
                batchSizeMax.getValueFactory().getValue())) {
            showInvalidBox("Lowest batch size (" + batchSizeMin.getValueFactory().getValue() +
                    ") cannot be larger than highest batch size (" + batchSizeMax.getValueFactory().getValue() + ")!");
        } else if (!isValidStepSize(batchSizeStep.getValueFactory().getValue(),
                batchSizeMin.getValueFactory().getValue(),
                batchSizeMax.getValueFactory().getValue())) {
            showInvalidBox("Step Size between Runs cannot exceed " +
                    Math.max(1, (batchSizeMax.getValueFactory().getValue() - batchSizeMin.getValueFactory().getValue()))
                    + "!");
        } else {
            if (confirmRun(batchSizeMin.getValueFactory().getValue(), batchSizeMax.getValueFactory().getValue(),
                    batchSizeStep.getValueFactory().getValue())) {
                try {
                    execute();
                } catch (IOException e) {
                    showExceptionBox("An IO Exception has occurred.\n" + e.getMessage() + "\nPlease try again.");
                } catch (CustomException e) {
                    showExceptionBox("A Custom Exception has occurred.\n" + e.getMessage() + "\nPlease try again.");
                }
            }
        }
    }

    /**
     * Saves input data to core.
     */
    private void saveInputDataToCore() {
        core.inputData(exeLocation.getText(), modelFileLocation.getText(), inputFileLocation.getText(),
                outputFileLocation.getText(), runSpeed.getText(), stopTime.getText(),
                showModel.isSelected(), lotSequencingRule.getValue(),
                Integer.toString(batchSizeMin.getValueFactory().getValue()),
                Integer.toString(batchSizeMax.getValueFactory().getValue()),
                Integer.toString(batchSizeStep.getValueFactory().getValue()),
                getSelectedResourceSelectCriteria(resourceSelectCriteria),
                getSelectedLotSelectionCriteria(lotSelectionCriteria),
                getSelectedTrolleyLocationSelectCriteria(trolleyLocationSelectCriteria),
                getSelectedBibLoadOnLotCriteria(bibLoadOnLotCriteria));
    }

    /**
     * Executes Core with confirmation, waiting and completion alerts
     */
    private void execute() throws IOException, CustomException {
        String title = "Simulation running...";
        String header = "Please wait for the simulation to complete...";
        Alert waitAlert = raiseAlertBox(Alert.AlertType.NONE, title, header, null, 480, 60);

        waitAlert.show();

        saveInputDataToCore();
        jsonParser.storeData(core);
        core.execute();

        Stage stage = (Stage) waitAlert.getDialogPane().getScene().getWindow();
        stage.close();
        showCompletedBox();
    }

    /** Get the selected radio button for Resource Select Criteria from user input
     * @param resourceSelectCriteria Toggle group for Resource Select Criteria
     */
    private String getSelectedResourceSelectCriteria(ToggleGroup resourceSelectCriteria) {
        Toggle selection = resourceSelectCriteria.getSelectedToggle();
        if (selection == resourceSelectCriteria1) {
            return "1";
        } else if (selection == resourceSelectCriteria2) {
            return "2";
        } else if (selection == resourceSelectCriteria3) {
            return "3";
        } else {
            return "4";
        }
    }

    /** Get the selected radio button for Lot Selection Criteria from user input
     * @param lotSelectionCriteria Toggle group for Lot Selection Criteria
     */
    private String getSelectedLotSelectionCriteria(ToggleGroup lotSelectionCriteria) {
        Toggle selection = lotSelectionCriteria.getSelectedToggle();
        if (selection == lotSelectionCriteria1) {
            return "1";
        } else if (selection == lotSelectionCriteria2) {
            return "2";
        } else {
            return "3";
        }
    }

    /** Get the selected radio button for Trolley Location Select Criteria from user input
     * @param trolleyLocationSelectCriteria Toggle group for Trolley Location Select Criteria
     */
    private String getSelectedTrolleyLocationSelectCriteria(ToggleGroup trolleyLocationSelectCriteria) {
        Toggle selection = trolleyLocationSelectCriteria.getSelectedToggle();
        if (selection == trolleyLocationSelectCriteria0) {
            return "0";
        } else if (selection == trolleyLocationSelectCriteria1) {
            return "1";
        } else {
            return "2";
        }
    }

    /** Get the selected radio button for BIB Load On Lot Criteria from user input
     * @param bibLoadOnLotCriteria Toggle group for BIB Load On Lot Criteria
     */
    private String getSelectedBibLoadOnLotCriteria(ToggleGroup bibLoadOnLotCriteria) {
        Toggle selection = bibLoadOnLotCriteria.getSelectedToggle();
        if (selection == bibLoadOnLotCriteria1) {
            return "1";
        } else {
            return "2";
        }
    }

    /**
     * Raise dialog box for save confirmation.
     * @return true when user clicks OK to confirm
     */
    private boolean confirmSave() {
        String title = "Save Input";
        String header = "Confirm to save input data?";
        String text = Messages.CONFIRM_SAVE_MESSAGE;
        Alert confirmationAlert = raiseAlertBox(Alert.AlertType.CONFIRMATION, title, header, text);

        confirmationAlert.showAndWait();
        if (confirmationAlert.getResult() == ButtonType.OK) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Raise dialog box for load confirmation.
     * @return true when user clicks OK to confirm
     */
    private boolean confirmLoad() {
        String title = "Open Input";
        String header = "Confirm to load saved input data?";
        String text = Messages.CONFIRM_LOAD_MESSAGE;
        Alert confirmationAlert = raiseAlertBox(Alert.AlertType.CONFIRMATION, title, header, text);

        confirmationAlert.showAndWait();
        if (confirmationAlert.getResult() == ButtonType.OK) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Helper function to raise alert box with a supplied alert text for Confirmation.
     * @return true when user clicks OK to confirm
     */
    private boolean confirmRun(int batchSizeMin, int batchSizeMax, int batchSizeStep) {
        String title = "Confirm Simulation";
        String header = "Confirm to run simulation?";
        String text = "There will be " + ((batchSizeMax - batchSizeMin) / batchSizeStep + 1) + " simulation run(s).\n"
                + Messages.CONFIRM_RUN_MESSAGE;
        Alert confirmationAlert = raiseAlertBox(Alert.AlertType.CONFIRMATION, title, header, text);

        confirmationAlert.showAndWait();
        if (confirmationAlert.getResult() == ButtonType.OK) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Helper function to raise alert box with a supplied alert text for invalid input.
     * @param alertText Alert text string to be displayed to the user.
     */
    private void showInvalidBox(String alertText) {
        String title = "Invalid Input";
        String header = "Invalid Input";
        Alert errorAlert = raiseAlertBox(Alert.AlertType.ERROR, title, header, alertText);

        errorAlert.showAndWait();
    }

    /**
     * Helper function to raise alert box with a supplied alert text for Errors.
     * @param alertText Alert text string to be displayed to the user.
     */
    private void showErrorBox(String alertText) {
        String title = "Error";
        Alert errorAlert = raiseAlertBox(Alert.AlertType.ERROR, title, null, alertText);

        errorAlert.showAndWait();
    }

    /**
     * Helper function to raise alert box with a supplied alert text for Exceptions.
     * @param alertText Alert text string to be displayed to the user.
     */
    private void showExceptionBox(String alertText) {
        String title = "Exception Error";
        Alert errorAlert = raiseAlertBox(Alert.AlertType.ERROR, title, null, alertText);

        errorAlert.showAndWait();
    }

    /**
     * Helper function to raise alert box with a supplied alert text for Completion.
     */
    private void showCompletedBox() {
        String title = "Simulation Complete";
        String header = "Simulation has completed!";
        String text = "You may run another simulation or close the program.";
        Alert completeAlert = raiseAlertBox(Alert.AlertType.INFORMATION, title, header, text);

        completeAlert.showAndWait();
    }

    /**
     * Returns true if any of the file locations are blank.
     * @return Boolean.
     */
    private boolean isBlankFiles() {
        if (exeLocation.getText() == null || exeLocation.getText().isBlank() ||
                modelFileLocation.getText() == null || modelFileLocation.getText().isBlank() ||
                inputFileLocation.getText() == null || inputFileLocation.getText().isBlank() ||
                outputFileLocation.getText() == null || outputFileLocation.getText().isBlank()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns true if the given file locations can be found.
     * @return Boolean.
     */
    private boolean isFoundFiles(String fileLocation) {
        File file = new File(fileLocation);

        if (file.exists()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns true if flexsim.exe is valid.
     * @return Boolean.
     */
    private boolean isValidExeLocation() {
        String fileName = exeLocation.getText().substring(exeLocation.getText().lastIndexOf("\\") + 1);

        if (fileName.equals("flexsim.exe")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns true if file extension is valid.
     * @return Boolean.
     */
    private boolean isValidExtension(String fileLocation, String extension) {
        String fileName = fileLocation.substring(fileLocation.lastIndexOf(".") + 1);

        if (fileName.equals(extension)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns true if any of the required simulation run parameters are blank.
     * @return Boolean.
     */
    private boolean isBlankRunParams() {
        if (runSpeed.getText() == null || runSpeed.getText().isBlank() ||
                stopTime.getText() == null || stopTime.getText().isBlank()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks if min is lesser than max.
     * @param minBatchSize representing minimum batch size.
     * @param maxBatchSize representing maximum batch size.
     * @return Boolean.
     */
    private boolean isValidMinMax(int minBatchSize, int maxBatchSize) {
        if (minBatchSize <= maxBatchSize) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns true if the batch step size is acceptable i.e. smaller than difference between min and max batch sizes.
     * @param stepSize representing batch step size.
     * @param minBatchSize representing minimum batch size.
     * @param maxBatchSize representing maximum batch size.
     * @return Boolean
     */
    private boolean isValidStepSize(int stepSize, int minBatchSize, int maxBatchSize) {
        try {
            if ((maxBatchSize == minBatchSize) && (stepSize == 1)){
                return true;
            } else if (stepSize <= (maxBatchSize - minBatchSize)) {
                return true;
            } else {
                return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Returns true if a batchMinString falls within the range of
     * MIN_ALLOWABLE_BATCH_SIZE and MAX_ALLOWABLE_BATCH_SIZE.
     * @param minBatchSize representing Batch Min Size.
     * @return Boolean.
     */
    private boolean isValidMinBatchSize(int minBatchSize) {
        try {
            if ( (minBatchSize >= MIN_ALLOWABLE_BATCH_SIZE) && (minBatchSize <= MAX_ALLOWABLE_BATCH_SIZE)) {
                return true;
            } else {
                return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Follows the same logic as "isValidMinBatchSize".
     * @param maxBatchSize representing Batch Max Size.
     * @return Boolean
     */
    private boolean isValidMaxBatchSize(int maxBatchSize){
        return isValidMinBatchSize(maxBatchSize);
    }

    /**
     * Returns true if the argument string can be converted to a Double primitive data type.
     * @param str String argument.
     * @return Boolean.
     */
    private boolean isNotDouble(String str) {
        try {
            Double.parseDouble(str);
            return false;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    /**
     * Generic function to raise alert box.
     * @return Alert box
     */
    private Alert raiseAlertBox(Alert.AlertType type, String title, String header, String text, int prefWidth, int prefHeight) {
        Alert alert = new Alert(type);

        // Text
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(text);

        // Properties
        alert.setResizable(true);
        alert.getDialogPane().setPrefSize(prefWidth, prefHeight);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(this.getClass().getResource(ICON_APPLICATION).toString()));

        return alert;
    }

    /**
     * Generic function to raise alert box with default dimensions.
     * @return Alert box
     */
    private Alert raiseAlertBox(Alert.AlertType type, String title, String header, String text) {
        return raiseAlertBox(type, title, header, text, 480, 240);
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    void show() {
        primaryStage.show();
    }
}
