package com.nusinfineon.ui;

import java.io.IOException;

import com.nusinfineon.core.Core;
import com.nusinfineon.exceptions.CustomException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
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
    private static final int MAX_ALLOWABLE_STEP_SIZE = MAX_ALLOWABLE_BATCH_SIZE - 1;
    private static final int MIN_ALLOWABLE_BATCH_SIZE = 0;
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
    private CheckBox showModel;
    @FXML
    private TextField batchSizeMin;
    @FXML
    private TextField batchSizeMax;
    @FXML
    private TextField batchSizeStep;

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
        batchSizeMin.setText(core.getBatchSizeMinString());
        batchSizeMax.setText(core.getBatchSizeMaxString());
        batchSizeStep.setText(core.getBatchSizeStepString());
    }


    /**
     * for drag and drop functionality
     * @param event
     */
    @FXML
    public void  modelDragOver(DragEvent event) {
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
        }else {
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
    public void  inputDragOver(DragEvent event) {
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
        }else {
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
    public void  outputDragOver(DragEvent event) {
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
        }else {
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
    public void  exeDragOver(DragEvent event) {
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
                && outputFileLocation.getText().isBlank() && modelFileLocation.getText().isBlank()){
            /*
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setHeaderText("Invalid Input");
            errorAlert.setContentText("file locations cannot be blank");
            errorAlert.showAndWait();
             */
            showErrorBox("file locations cannot be blank");
        }

        if (isNotDouble(runSpeed.getText()) && isNotDouble(stopTime.getText())) {
            /*
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setHeaderText("Invalid Input");
            errorAlert.setContentText("Run speed and Stop time must be an integer or double");
            errorAlert.showAndWait();
             */
            showErrorBox("Run speed and Stop time must be an integer or double");
        } else if (!isValidMinBatchSize(batchSizeMin.getText())) {
            showErrorBox("Min batch size must be > 0 and < 24");
        } else if (!isValidMaxBatchSize(batchSizeMax.getText())) {
            showErrorBox("Max batch size must be > 0 and < 24");
        } else if (!isValidStepSize(batchSizeStep.getText())) {
            showErrorBox("Step size must be an integer and < " + MAX_ALLOWABLE_STEP_SIZE);
        } else if (!isValidMinMax(batchSizeMin.getText(), batchSizeMax.getText())) {
            showErrorBox("Min batch (" + batchSizeMin.getText() + ") must be smaller than max ("
                                + batchSizeMax.getText() + ")");
        } else {
            try {
                core.execute(exeLocation.getText(), modelFileLocation.getText(), inputFileLocation.getText(),
                        outputFileLocation.getText(), runSpeed.getText(), warmUpPeriod.getText(), stopTime.getText(),
                        showModel.isSelected(), batchSizeMin.getText(), batchSizeMax.getText(), batchSizeStep.getText());
            } catch (IOException e) {
                showErrorBox("Oops, an IO Exception has occurred");
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
     * Checks if min is lesser than max. Assumes min and max can be converted to valid integers.
     *
     * @param minBatchString String representing minimum batch size.
     * @param maxBatchString String representing maximum batch size.
     * @return Boolean.
     */
    private boolean isValidMinMax(String minBatchString, String maxBatchString) {
        int min = Integer.parseInt(minBatchString);
        int max = Integer.parseInt(maxBatchString);

        if (min < max) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns true if the batch step size is acceptable ie between 0 and MAX_ALLOWABLE_sTEP_SIZE.
     * @param batchStepSizeString String representing batch step size.
     * @return Boolean
     */
    private boolean isValidStepSize(String batchStepSizeString) {
        try {
            int batchStepSize = Integer.parseInt(batchStepSizeString);

            if ( (batchStepSize > 0) && (batchStepSize<MAX_ALLOWABLE_STEP_SIZE) ) {
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
     * @param maxBatchSizeString String representing Batch Max Size.
     * @return Boolean
     */
    private boolean isValidMaxBatchSize(String maxBatchSizeString){
        return isValidMinBatchSize(maxBatchSizeString);
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
     * Returns true if a batchMinString is (i) a valid int and (ii) falls within the range of
     * MIN_ALLOWABLE_BATCH_SIZE and MAX_ALLOWABLE_BATCH_SIZE.
     * @param minBatchSizeString String representing batchMin
     * @return Boolean.
     */
    private boolean isValidMinBatchSize(String minBatchSizeString) {
        try {
            int batchMinSize = Integer.parseInt(minBatchSizeString);
            if ( (batchMinSize > MIN_ALLOWABLE_BATCH_SIZE) && (batchMinSize < MAX_ALLOWABLE_BATCH_SIZE)) {
                return true;
            } else {
                return false;
            }

        } catch (NumberFormatException e) {
            return false;
        }
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    //to be added
/*    private void setAccelerators() {
        setAccelerator(helpMenuItem, KeyCombination.valueOf("F1"));
    }*/

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
                batchSizeMin.getText(), batchSizeMax.getText(), batchSizeStep.getText());
        primaryStage.hide();
    }


}
