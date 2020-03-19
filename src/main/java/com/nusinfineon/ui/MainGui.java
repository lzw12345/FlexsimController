package com.nusinfineon.ui;

import java.io.File;
import java.io.IOException;

import com.nusinfineon.core.Core;
import com.nusinfineon.exceptions.CustomException;
import com.pretty_tools.dde.DDEException;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
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
    private static final String ICON_APPLICATION = "/images/infineon-technologies-squarelogo.png";

    private Stage primaryStage;
    private Core core;

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

    public MainGui(Stage primaryStage, Core core) {
        super(FXML, primaryStage);

        // Set dependencies
        this.primaryStage = primaryStage;
        this.core = core;

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
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setHeaderText("Input not valid");
            errorAlert.setContentText("File must be a Flexsim nus.infineon.model with extension .fsm");
            errorAlert.showAndWait();
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
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setHeaderText("Input not valid");
            errorAlert.setContentText("File must be an Excel file with extension .xlsx");
            errorAlert.showAndWait();
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
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setHeaderText("Input not valid");
            errorAlert.setContentText("File must be an Excel file with extension .xlsx");
            errorAlert.showAndWait();
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
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setHeaderText("Input not valid");
            errorAlert.setContentText("File must be a executable file with extension .exe");
            errorAlert.showAndWait();
        }
        /* let the source know whether the string was successfully
         * transferred and used */
        event.setDropCompleted(success);
        event.consume();
    }

    @FXML
    public void handleModelExecution() throws IOException {

        if (isBlankFiles()) {
            showErrorBox("File directories cannot be blank!");
        } else if (!isFoundFiles(exeLocation.getText())) {
            showErrorBox("Flexsim (.exe) address cannot be found!");
        } else if (!isFoundFiles(modelFileLocation.getText())) {
            showErrorBox("Model (.fsm) address cannot be found!");
        } else if (!isFoundFiles(inputFileLocation.getText())) {
            showErrorBox("Input (.xlsx) address cannot be found!");
        } else if (!isFoundFiles(outputFileLocation.getText())) {
            showErrorBox("Output (.xlsx) address cannot be found!");
        } else if (!isValidExeLocation()) {
            showErrorBox("Flexsim (.exe) must be the executable file: flexsim.exe");
        } else if (!isValidExtension(modelFileLocation.getText(), "fsm")) {
            showErrorBox("Model (.fsm) must be a Flexsim nus.infineon.model with extension .fsm!");
        } else if (!isValidExtension(inputFileLocation.getText(), "xlsx")) {
            showErrorBox("Input (.xlsx) must be an Excel file with extension .xlsx!");
        } else if (!isValidExtension(outputFileLocation.getText(), "xlsx")) {
            showErrorBox("Output (.xlsx) must be an Excel file with extension .xlsx!");
        } else if (isBlankRunParams()) {
            showErrorBox("Run Speed and/or Stop Time cannot be blank!");
        } else if (isNotDouble(runSpeed.getText()) || isNotDouble(stopTime.getText())) {
            showErrorBox("Run Speed and/or Stop Time must be a number (integer/double)!");
        } else if (!isValidMinBatchSize(batchSizeMin.getValueFactory().getValue())) {
            showErrorBox("Minimum batch size must be at least 1 and at most 24!");
        } else if (!isValidMaxBatchSize(batchSizeMax.getValueFactory().getValue())) {
            showErrorBox("Maximum batch size must be at least 1 and at most 24!");
        } else if (!isValidMinMax(batchSizeMin.getValueFactory().getValue(),
                batchSizeMax.getValueFactory().getValue())) {
            showErrorBox("Minimum batch size (" + batchSizeMin.getValueFactory().getValue() +
                    ") cannot be larger than maximum batch size (" + batchSizeMax.getValueFactory().getValue() + ")!");
        } else if (!isValidStepSize(batchSizeStep.getValueFactory().getValue(),
                batchSizeMin.getValueFactory().getValue(),
                batchSizeMax.getValueFactory().getValue())) {
            showErrorBox("Step Size between Runs cannot exceed " +
                    Math.max(1, (batchSizeMax.getValueFactory().getValue() - batchSizeMin.getValueFactory().getValue()))
                    + "!");
        } else {
            if (confirmRunModel(batchSizeMin.getValueFactory().getValue(), batchSizeMax.getValueFactory().getValue(),
                    batchSizeStep.getValueFactory().getValue())) {
                try {
                    saveInputDataToCore();
                    core.execute();
                    core.handleOutput();
                    showCompletedBox();
                } catch (IOException e) {
                    showExceptionBox("An IO Exception has occurred.\n" + e.getMessage());
                } catch (CustomException e) {
                    showExceptionBox("A Custom Exception has occurred.\n" + e.getMessage());
                } catch (InterruptedException | DDEException e) {
                    e.printStackTrace();
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
     * Helper function to raise alert box with a supplied alert text for Errors.
     * @param alertText Alert text string to be displayed to the user.
     */
    private void showErrorBox(String alertText) {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);

        // Text
        errorAlert.setTitle("Invalid Input");
        errorAlert.setHeaderText("Invalid Input");
        errorAlert.setContentText(alertText);

        // Properties
        errorAlert.setResizable(true);
        errorAlert.getDialogPane().setPrefSize(480, 240);
        Stage stage = (Stage) errorAlert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(this.getClass().getResource(ICON_APPLICATION).toString()));

        errorAlert.showAndWait();
    }

    /**
     * Helper function to raise alert box with a supplied alert text for Confirmation.
     * @return true when user clicks OK to confirm
     */
    private boolean confirmRunModel(int batchSizeMin, int batchSizeMax, int batchSizeStep) {
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);

        // Text
        confirmationAlert.setTitle("Confirm to run simulation?");
        confirmationAlert.setHeaderText("Confirm to run simulation?");
        String alertText = "There will be " + ((batchSizeMax - batchSizeMin) / batchSizeStep + 1) + " simulation runs."
                + "\nWarning: The more runs there are, the longer it will take until completion.";
        confirmationAlert.setContentText(alertText);

        // Properties
        confirmationAlert.setResizable(true);
        confirmationAlert.getDialogPane().setPrefSize(480, 240);
        Stage stage = (Stage) confirmationAlert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(this.getClass().getResource(ICON_APPLICATION).toString()));

        confirmationAlert.showAndWait();
        if (confirmationAlert.getResult() == ButtonType.OK) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Helper function to raise alert box with a supplied alert text for Errors.
     * @param alertText Alert text string to be displayed to the user.
     */
    private void showExceptionBox(String alertText) {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);

        // Text
        errorAlert.setTitle("Exception Error!");
        errorAlert.setHeaderText(null);
        errorAlert.setContentText(alertText);

        // Properties
        errorAlert.setResizable(true);
        errorAlert.getDialogPane().setPrefSize(480, 240);
        Stage stage = (Stage) errorAlert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(this.getClass().getResource(ICON_APPLICATION).toString()));

        errorAlert.showAndWait();
    }

    /**
     * Helper function to raise alert box with a supplied alert text for Completion.
     */
    private void showCompletedBox() {
        Alert completeAlert = new Alert(Alert.AlertType.INFORMATION);

        // Text
        completeAlert.setTitle("Simulation completed!");
        completeAlert.setHeaderText("Simulation completed!");
        String alertText = "You may run another simulation again or close the program.";
        completeAlert.setContentText(alertText);

        // Properties
        completeAlert.setResizable(true);
        completeAlert.getDialogPane().setPrefSize(480, 240);
        Stage stage = (Stage) completeAlert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(this.getClass().getResource(ICON_APPLICATION).toString()));

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

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    /* TODO: to be added
    private void setAccelerators() {
        setAccelerator(helpMenuItem, KeyCombination.valueOf("F1"));
    }
    */

    /**
     * Sets the accelerator of a MenuItem.
     * @param keyCombination the KeyCombination value of the accelerator
     */
    private void setAccelerator(MenuItem menuItem, KeyCombination keyCombination) {
        menuItem.setAccelerator(keyCombination);

        /*
         * TODO: the code below can be removed once the bug reported here
         * https://bugs.openjdk.java.net/browse/JDK-8131666
         * is fixed in later version of SDK.
         *
         * According to the bug report, TextInputControl (TextField, TextArea) will
         * consume function-key events. Because CommandBox contains a TextField, and
         * ResultDisplay contains a TextArea, thus some accelerators (e.g F1) will
         * not work when the focus is in them because the key event is consumed by
         * the TextInputControl(s).
         *
         * For now, we add following event filter to capture such key events and open
         * help window purposely so to support accelerators even when focus is
         * in CommandBox or ResultDisplay.
         */
        getRoot().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getTarget() instanceof TextInputControl && keyCombination.match(event)) {
                menuItem.getOnAction().handle(new ActionEvent());
                event.consume();
            }
        });
    }

    /**
     * Fills up all the placeholders of this window.
     * to be completed
     */
/*    void fillInnerParts() {
        flashcardListPanel = new FlashcardListPanel(logic.getFilteredFlashcardList());
        flashcardListPanelPlaceholder.getChildren().add(flashcardListPanel.getRoot());

        resultDisplay = new ResultDisplay();
        resultDisplayPlaceholder.getChildren().add(resultDisplay.getRoot());

        flashcardDisplay = new FlashcardDisplay();
        flashcardDisplayPlaceholder.getChildren().add(flashcardDisplay.getRoot());

        CommandBox commandBox = new CommandBox(this::executeCommand);
        commandBoxPlaceholder.getChildren().add(commandBox.getRoot());
        StatusBarFooter statusBarFooter = new StatusBarFooter(logic.getFlashcardListFilePath());
        statusbarPlaceholder.getChildren().add(statusBarFooter.getRoot());
    }*/

    /**
     * Sets the default size based on {@code guiSettings}. to be added
     */
/*    private void setWindowDefaultSize(GuiSettings guiSettings) {
        primaryStage.setHeight(guiSettings.getWindowHeight());
        primaryStage.setWidth(guiSettings.getWindowWidth());
        if (guiSettings.getWindowCoordinates() != null) {
            primaryStage.setX(guiSettings.getWindowCoordinates().getX());
            primaryStage.setY(guiSettings.getWindowCoordinates().getY());
        }
    }*/

    /**
     * Resets input fields to default.
     */
    @FXML
    private void handleDefault() {
        core.inputData(null, null, null, null, null, null, false, null, null, null, null, null, null, null, null);
        configureUi();
    }

    void show() {
        primaryStage.show();
    }

    /**
     * Closes the application.
     */
    @FXML
    private void handleExit() {
        saveInputDataToCore();
        primaryStage.hide();
    }
}
