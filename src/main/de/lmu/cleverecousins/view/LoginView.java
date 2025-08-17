package de.lmu.cleverecousins.view;

import de.lmu.cleverecousins.viewmodel.LoginViewModel;
import de.lmu.util.LogConfigurator;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoginView extends StackPane {

    private static final Logger log = Logger.getLogger(LoginView.class.getName());

    static {
        LogConfigurator.configureRootLogger(Level.FINE);
    }

    public LoginView(LoginViewModel viewModel) {
        // Hintergrundbild laden
        URL bgUrl = getClass().getResource("/design/login_background.jpeg");
        if (bgUrl != null) {
            BackgroundImage bgImage = new BackgroundImage(
                    new Image(bgUrl.toExternalForm(), true),
                    BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, false)
            );
            setBackground(new Background(bgImage));
        } else {
            log.severe("Login Hintergrundbild nicht gefunden: /design/login_background.jpeg");
        }

        // Zentrales Panel mit halbtransparentem Hintergrund
        VBox container = new VBox(15);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(30));
        container.setMaxWidth(350);
        container.setStyle("-fx-background-color: rgba(255, 255, 255, 0.85); -fx-background-radius: 15;");

        // Überschrift
        Label titleLabel = new Label("Willkommen");
        titleLabel.setFont(Font.font("Arial", 26));
        titleLabel.setStyle("-fx-text-fill: #da6713; -fx-font-weight: bold;");

        // Gruppenname
        TextField groupNameField = new TextField();
        groupNameField.setPromptText("Gruppenname");
        groupNameField.textProperty().bindBidirectional(viewModel.groupNameProperty());
        groupNameField.setMaxWidth(Double.MAX_VALUE);
        groupNameField.setStyle("-fx-font-size: 14px;");

        // Checkbox "Ich bin keine KI"
        CheckBox humanCheckBox = new CheckBox("Ich bin keine KI");
        humanCheckBox.selectedProperty().bindBidirectional(viewModel.isHumanProperty());
        humanCheckBox.setStyle("-fx-font-size: 14px; -fx-text-fill: #444;");

        // Spielername
        TextField playerNameField = new TextField();
        playerNameField.setPromptText("Spielername");
        playerNameField.textProperty().bindBidirectional(viewModel.playerNameProperty());
        playerNameField.setMaxWidth(Double.MAX_VALUE);
        playerNameField.setStyle("-fx-font-size: 14px;");

        // Überschrift für Roboter-Auswahl
        Label robotLabel = new Label("Deinen Roboter auswählen:");
        robotLabel.setStyle("-fx-font-size: 14px; -fx-font-family: 'System'; -fx-font-weight: normal;");
        // Alle möglichen Roboter IDs
        ObservableList<Integer> allRobots = FXCollections.observableArrayList(1, 2, 3, 4, 5, 6);
        // Liste für verfügbare Roboter, initial mit allen befüllt
        ObservableList<Integer> availableRobots = FXCollections.observableArrayList();
        availableRobots.setAll(allRobots);

        ComboBox<Integer> robotIdCombo = new ComboBox<>(availableRobots);
        robotIdCombo.valueProperty().bindBidirectional(viewModel.robotIdProperty());
        robotIdCombo.setMaxWidth(Double.MAX_VALUE);
        robotIdCombo.setPromptText("Roboter auswählen");

        // Listener auf die Liste der bereits verwendeten Roboter im ViewModel
        viewModel.getUsedRobots().addListener((ListChangeListener<Integer>) change -> {
            availableRobots.setAll(allRobots);
            availableRobots.removeAll(viewModel.getUsedRobots());

            // Wenn der aktuell ausgewählte Roboter nicht mehr verfügbar ist, Auswahl löschen
            Integer selected = viewModel.robotIdProperty().get();
            if (selected != null && !availableRobots.contains(selected)) {
                viewModel.robotIdProperty().set(null);
            }
        });
        // Login Button
        Button loginButton = new Button("Anmelden");
        loginButton.setDefaultButton(true);
        loginButton.disableProperty().bind(viewModel.loginDisabledProperty());
        loginButton.setOnAction(e -> viewModel.sendLogin());
        loginButton.setStyle("""
    -fx-background-color: linear-gradient(to bottom right, #da6713, #ea1616);
    -fx-text-fill: white;
    -fx-font-weight: bold;
    -fx-background-radius: 8;
    -fx-padding: 10 20 10 20;
    -fx-cursor: hand;
    """);
        loginButton.setMaxWidth(Double.MAX_VALUE);

        // Fehlermeldung Label (Optional, falls du errorMessageProperty ergänzt)
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");

        // Elemente hinzufügen
        container.getChildren().addAll(
                titleLabel,
                groupNameField,
                humanCheckBox,
                playerNameField,
                robotLabel,
                robotIdCombo,
                loginButton,
                errorLabel
        );

        getChildren().add(container);
        setAlignment(container, Pos.CENTER);
        setPadding(new Insets(20));
    }
}



