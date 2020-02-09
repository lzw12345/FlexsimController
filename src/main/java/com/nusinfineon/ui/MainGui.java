package com.nusinfineon.ui;

import java.io.IOException;

import com.nusinfineon.core.Core;
import com.nusinfineon.exceptions.CustomException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.ToggleGroup;
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
    private TextField warmUpPeriod;
    @FXML
    private TextField stopTime;
    @FXML
    private Spinner<Integer> batchSizeMin;
    @FXML
    private Spinner<Integer> batchSizeMax;
    @FXML
    private Spinner<Integer> batchSizeStep;
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
    private CheckBox showModel;

    public MainGui(Stage primaryStage, Core core) {
        super(FXML, primaryStage);

        // Set dependencies
        this.primaryStage = primaryStage;
        this.core = core;

        // Configure the UI
        exeLocation.setText(core.getFlexsimLocation());
        modelFileLocation.setText(core.getModelLocation());
        inputFileLocation.setText(core.getInputLocation());
        outputFileLocation.setText(core.getOutputLocation());
        runSpeed.setText(core.getRunSpeed());
        warmUpPeriod.setText(core.getWarmUpPeriod());
        stopTime.setText(core.getStopTime());

        batchSizeMin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                MIN_ALLOWABLE_BATCH_SIZE, MAX_ALLOWABLE_BATCH_SIZE, Integer.parseInt(core.getBatchSizeMinString())));
        batchSizeMax.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                MIN_ALLOWABLE_BATCH_SIZE, MAX_ALLOWABLE_BATCH_SIZE, Integer.parseInt(core.getBatchSizeMaxString())));
        batchSizeStep.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                MIN_ALLOWABLE_STEP_SIZE, MAX_ALLOWABLE_STEP_SIZE, Integer.parseInt(core.getBatchSizeStepString())));

        ToggleGroup resourceSelectCriteria = new ToggleGroup();
        resourceSelectCriteria1.setToggleGroup(resourceSelectCriteria);
        resourceSelectCriteria2.setToggleGroup(resourceSelectCriteria);
        resourceSelectCriteria3.setToggleGroup(resourceSelectCriteria);
        resourceSelectCriteria4.setToggleGroup(resourceSelectCriteria);

        ToggleGroup lotSelectionCriteria = new ToggleGroup();
        lotSelectionCriteria1.setToggleGroup(lotSelectionCriteria);
        lotSelectionCriteria2.setToggleGroup(lotSelectionCriteria);
        lotSelectionCriteria3.setToggleGroup(lotSelectionCriteria);
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
            errorAlert.setContentText("file must be a flexsim nus.infineon.model with extension .fsm");
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
            errorAlert.setContentText("file must be a excel file with extension .xlsx");
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
            errorAlert.setContentText("file must be a excel file with extension .xlsx");
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
            errorAlert.setContentText("file must be a executable file with extension .exe");
            errorAlert.showAndWait();
        }
        /* let the source know whether the string was successfully
         * transferred and used */
        event.setDropCompleted(success);

        event.consume();
    }

    @FXML
    public void handleModelExecution() throws IOException {

        if (exeLocation.getText().isBlank() && inputFileLocation.getText().isBlank()
                && outputFileLocation.getText().isBlank() && modelFileLocation.getText().isBlank()) {
            /*
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setHeaderText("Invalid Input");
            errorAlert.setContentText("file locations cannot be blank");
            errorAlert.showAndWait();
             */
            showErrorBox("File locations cannot be blank!");
        }
        if (runSpeed.getText().isBlank() || stopTime.getText().isBlank()) {
            showErrorBox("Run Speed and/or Stop Time cannot be blank!");
        } else if (isNotDouble(runSpeed.getText()) || isNotDouble(stopTime.getText())) {
            /*
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setHeaderText("Invalid Input");
            errorAlert.setContentText("Run speed and Stop time must be an integer or double");
            errorAlert.showAndWait();
             */
            showErrorBox("Run Speed and/or Stop Time must be a number (integer/double)!");
        } else if (!isValidMinBatchSize(batchSizeMin.getValueFactory().getValue())) {
            showErrorBox("Min batch size must be at least 1 and at most 24!");
        } else if (!isValidMaxBatchSize(batchSizeMax.getValueFactory().getValue())) {
            showErrorBox("Max batch size must be at least 1 and at most 24!");
        } else if (!isValidMinMax(batchSizeMin.getValueFactory().getValue(),
                batchSizeMax.getValueFactory().getValue())) {
            showErrorBox("Min batch size (" + batchSizeMin.getValueFactory().getValue() +
                    ") cannot be larger than max batch size (" + batchSizeMax.getValueFactory().getValue() + ")!");
        } else if (!isValidStepSize(batchSizeStep.getValueFactory().getValue(),
                batchSizeMin.getValueFactory().getValue(),
                batchSizeMax.getValueFactory().getValue())) {
            showErrorBox("Step size can at most be " +
                    (batchSizeMax.getValueFactory().getValue() - batchSizeMin.getValueFactory().getValue()) + "!");
        } else {
            try {
                core.execute(exeLocation.getText(), modelFileLocation.getText(), inputFileLocation.getText(),
                        outputFileLocation.getText(), runSpeed.getText(), warmUpPeriod.getText(), stopTime.getText(),
                        showModel.isSelected(), Integer.toString(batchSizeMin.getValueFactory().getValue()),
                        Integer.toString(batchSizeMax.getValueFactory().getValue()),
                        Integer.toString(batchSizeStep.getValueFactory().getValue()));
            } catch (IOException e) {
                showErrorBox("An IO Exception has occurred.");
            } catch (CustomException e) {
                showErrorBox(e.getMessage());
            }
        }
    }

    /**
     * Helper function to raise alert box with a supplied alert text.
     * @param alertText Alert text string to be displayed to the user.
     */
    private void showErrorBox(String alertText) {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setHeaderText("Invalid Input");
        errorAlert.setContentText(alertText);
        errorAlert.showAndWait();
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

    void show() {
        primaryStage.show();
    }

    /**
     * Closes the application.
     */
    @FXML
    private void handleExit() {
        core.inputData(exeLocation.getText(), modelFileLocation.getText(), inputFileLocation.getText(),
                outputFileLocation.getText(), runSpeed.getText(), warmUpPeriod.getText(), stopTime.getText(),
                Integer.toString(batchSizeMin.getValueFactory().getValue()),
                Integer.toString(batchSizeMax.getValueFactory().getValue()),
                Integer.toString(batchSizeStep.getValueFactory().getValue()));
        primaryStage.hide();
    }
}
