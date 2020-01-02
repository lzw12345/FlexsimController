import core.Core;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ui.MainGui;

import java.io.IOException;

public class Main extends Application {
    private Core core = new Core();

    public Main() throws IOException {
    }

    @Override
    public void start (Stage stage) throws IOException {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/view/MainGui.fxml"));
            BorderPane borderPane = fxmlLoader.load();
            Scene scene = new Scene(borderPane);
            stage.setScene(scene);
            //fxmlLoader.<MainGui>getController();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
