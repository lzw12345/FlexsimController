package com.nusinfineon.ui;

import com.nusinfineon.core.Core;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
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

import java.io.IOException;


/**
 * Represent the whole window of the user interface, it should contain all units in the user interface.
 */
public class MainGui extends UiPart<Stage> {

    private static final String FXML = "MainGui.fxml";

    private Stage primaryStage;

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

    private Core core;


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
    }






    @FXML
    public void  modelDragOver(DragEvent event) {
        if (event.getGestureSource() != modelDragTarget
                && event.getDragboard().hasFiles()) {
            /* allow for both copying and moving, whatever user chooses */
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        }
        event.consume();
    }

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
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setHeaderText("Invalid Input");
            errorAlert.setContentText("file locations cannot be blank");
            errorAlert.showAndWait();
        }
        if (isNotDouble(runSpeed.getText()) && isNotDouble(stopTime.getText())) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setHeaderText("Invalid Input");
            errorAlert.setContentText("Runspeed and Stop time must be an integer or double");
            errorAlert.showAndWait();
        } else {
            core.execute(exeLocation.getText(), modelFileLocation.getText(), inputFileLocation.getText(),
                    outputFileLocation.getText(), runSpeed.getText(), warmUpPeriod.getText(), stopTime.getText());
        }
    }

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
                outputFileLocation.getText(), runSpeed.getText(), warmUpPeriod.getText(), stopTime.getText());
        primaryStage.hide();
    }


}
