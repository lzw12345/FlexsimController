package ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

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
        if (db.hasFiles()) {
            modelFileLocation.setText(db.getFiles().toString());
            success = true;
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
        if (db.hasFiles()) {
            inputFileLocation.setText(db.getFiles().toString());
            success = true;
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
        if (db.hasFiles()) {
            inputFileLocation.setText(db.getFiles().toString());
            success = true;
        }
        /* let the source know whether the string was successfully
         * transferred and used */
        event.setDropCompleted(success);

        event.consume();
    }



}
