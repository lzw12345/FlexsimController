package ui;

import core.Core;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.io.IOException;

public class MainGui extends BorderPane {
    @FXML
    HBox exeDragTarget;

    @FXML
    TextField exeLocation;

    @FXML
    HBox modelDragTarget;

    @FXML
    TextField modelFileLocation;

    @FXML
    HBox inputFileDragTarget;

    @FXML
    TextField inputFileLocation;

    private Core core;

    public void setGui (Core core) {
        this.core = core;
    }

    @FXML
    public void initialize() {

        //unfocus pathField
        Platform.runLater( () -> modelDragTarget.requestFocus() );
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
            Alert errorAlert = new Alert(AlertType.ERROR);
            errorAlert.setHeaderText("Input not valid");
            errorAlert.setContentText("file must be a flexsim model with extension .fsm");
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
            Alert errorAlert = new Alert(AlertType.ERROR);
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
        if (event.getGestureSource() != inputFileDragTarget
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
            Alert errorAlert = new Alert(AlertType.ERROR);
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
        core.execute(exeLocation.getText(), modelFileLocation.getText(), inputFileLocation.getText(),
                0,0 ,0);
    }


}
