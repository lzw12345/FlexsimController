package com.nusinfineon;

import java.io.IOException;

import com.nusinfineon.core.Core;
import com.nusinfineon.storage.JsonParser;
import com.nusinfineon.ui.Ui;
import com.nusinfineon.ui.UiManager;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    private Core core;
    private JsonParser jsonParser;
    private Ui ui;

    public Main() throws IOException {
        jsonParser = new JsonParser();
        core = jsonParser.loadData();
        ui = new UiManager(core, jsonParser);
    }

    @Override
    public void start(Stage stage) {
        ui.start(stage);
    }

    @Override
    public void stop() throws IOException {
        jsonParser.storeData(core);
    }
}
