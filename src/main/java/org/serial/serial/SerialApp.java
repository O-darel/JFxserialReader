package org.serial.serial;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.serial.serial.ui.MainView;
import org.serial.serial.util.LogManager;

public class SerialApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        LogManager.getInstance().info("Application starting...");

        MainView mainView = new MainView();
        Scene scene = new Scene(mainView.getRoot(), 700, 600);
        scene.getStylesheets().add(getClass().getResource("/static/styles/styles.css").toExternalForm());

        primaryStage.setTitle("Serial to MQTT Bridge");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> {
            mainView.shutdown();
            LogManager.getInstance().info("Application stopped");
        });

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}