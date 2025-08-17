package de.lmu.cleverecousins;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * A simple JavaFX application that loads and displays the hello-view.fxml file.
 * This class can serve as a test launcher for the RoboRally login interface.
 */
public class HelloApplication extends Application {

    /**
     * The main entry point for all JavaFX applications.
     * Loads the FXML-defined UI and displays it in a window.
     *
     * @param stage the primary stage for this application
     * @throws Exception if the FXML file cannot be loaded
     */
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/de/lmu/cleverecousins/hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 400, 300);
        stage.setTitle("RoboRally Login");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * The main method that launches the JavaFX application.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        launch();
    }
}
