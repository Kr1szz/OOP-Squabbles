package com.squabbles;

import com.squabbles.view.WelcomeView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("OOP Squabbles");

        // Load Welcome View
        setScene(new WelcomeView().getScene());

        primaryStage.show();
    }

    public static void setScene(Scene scene) {
        // Apply global stylesheet if not already applied (though usually applied per
        // scene)
        scene.getStylesheets().add(Main.class.getResource("/styles.css").toExternalForm());
        primaryStage.setScene(scene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
