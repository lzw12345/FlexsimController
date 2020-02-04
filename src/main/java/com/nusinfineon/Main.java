package com.nusinfineon;

import com.nusinfineon.ui.UiManager;
import com.nusinfineon.core.Core;
import javafx.application.Application;
import javafx.stage.Stage;
import com.nusinfineon.storage.JsonParser;
import com.nusinfineon.ui.Ui;

import java.io.IOException;

public class Main extends Application {
    private Core core;
    private JsonParser jsonParser;
    private Ui ui;

    public Main() throws IOException {
        jsonParser = new JsonParser();
        core = jsonParser.loadData();
        ui = new UiManager(core);
    }

    @Override
    public void start(Stage stage) throws IOException {
        ui.start(stage);
    }

    @Override
    public void stop() throws IOException {
        jsonParser.storeData(core);
    }
}
