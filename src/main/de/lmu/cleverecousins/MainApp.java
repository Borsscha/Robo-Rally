package de.lmu.cleverecousins;

import de.lmu.cleverecousins.util.MusicPlayer;
import de.lmu.cleverecousins.view.LobbyView;
import de.lmu.cleverecousins.view.LoginView;
import de.lmu.cleverecousins.viewmodel.LobbyViewModel;
import de.lmu.cleverecousins.viewmodel.LoginViewModel;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * The main entry point of the JavaFX client application.
 * It initializes the UI and establishes the client-server connection.
 */
public class MainApp extends Application {

    /** The primary window (stage) of the application. */
    private static Stage primaryStage;

    /**
     * Called by the JavaFX runtime to start the application.
     * It sets up the client connection, login view, and shared message handler.
     *
     * @param stage the primary stage for this application
     */
    @Override
    public void start(Stage stage) {
        primaryStage = stage;

        try {
            Client client = new Client();
            client.connect("localhost", 12345);

            // 🧠 Gemeinsamer MessageHandler, ViewModel wird dynamisch gesetzt
            MessageHandler sharedHandler = new MessageHandler(null);
            client.setMessageConsumer(sharedHandler::handle);

            // LoginViewModel + Login-View
            LoginViewModel loginViewModel = new LoginViewModel(client);
            sharedHandler.setViewModel(loginViewModel);

            LoginView loginView = new LoginView(loginViewModel);
            Scene loginScene = new Scene(loginView, 400, 300);

            // Login-Erfolg: Lobby anzeigen
            loginViewModel.setOnLoginSuccess(() -> showLobby(client, sharedHandler));

            stage.setTitle("Clevere Cousins");
            stage.setScene(loginScene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Transitions the application from the login view to the lobby view
     * after a successful login. Also starts background music.
     *
     * @param client the connected client instance
     * @param handler the shared message handler to use
     */
    private void showLobby(Client client, MessageHandler handler) {
        LobbyViewModel lobbyViewModel = new LobbyViewModel(client);
        handler.setViewModel(lobbyViewModel);

        LobbyView lobbyView = new LobbyView(lobbyViewModel);

        Scene scene = new Scene(lobbyView);  // Keine feste Größe hier
        primaryStage.setScene(scene);
        primaryStage.setWidth(1200);
        primaryStage.setHeight(900);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.setResizable(true);

        // Musik starten, wenn Lobby gezeigt wird
        MusicPlayer.playBackgroundMusic();
    }

    /**
     * Main method to launch the JavaFX application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
