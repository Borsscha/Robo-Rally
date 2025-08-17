package de.lmu.cleverecousins.view;

import de.lmu.cleverecousins.view.components.MapRenderer;
import de.lmu.cleverecousins.viewmodel.LobbyViewModel;
import de.lmu.util.LogConfigurator;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LobbyView extends BorderPane {

    private static final Logger log = Logger.getLogger(LobbyView.class.getName());

    static {
        LogConfigurator.configureRootLogger(Level.FINE);
    }

    private MapRenderer mapRenderer;

    public LobbyView(LobbyViewModel viewModel) {
        setPadding(new Insets(0));

        // ----- Linke Seite -----
        VBox leftBox = new VBox(10);
        leftBox.setPadding(new Insets(15));
        leftBox.setAlignment(Pos.TOP_CENTER);
        leftBox.prefWidthProperty().bind(widthProperty().multiply(0.3));

        // Hintergrundbild (links)
        URL leftBgUrl = getClass().getResource("/design/lobby_left_background.png");

        if (leftBgUrl == null) {
            log.severe("Hintergrundbild links nicht gefunden: /design/lobby_left_background.png");
        } else {
            BackgroundImage leftBg = new BackgroundImage(
                    new Image(leftBgUrl.toExternalForm(), true),
                    BackgroundRepeat.NO_REPEAT, BackgroundRepeat.REPEAT,
                    BackgroundPosition.DEFAULT,
                    new BackgroundSize(100, 100, true, true, true, false)
            );
            leftBox.setBackground(new Background(leftBg));
        }

        //spielerliste
        Label playersLabel = new Label("Spieler");
        playersLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, black, 2, 0.5, 0, 0);");

        // Benutzerdefinierte Zellenanzeige
        ListView<String> playerListView = new ListView<>(viewModel.getPlayers());
        playerListView.setStyle("""
        
                -fx-background-color: rgba(255, 255, 255, 0.8);
                        -fx-control-inner-background: transparent;
                        -fx-background-radius: 10;
                        -fx-padding: 10;
                        -fx-border-color: white;
                        -fx-border-radius: 10;
                                """);
        playerListView.setMouseTransparent(true);
        playerListView.setFocusTraversable(false);

        playerListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    int clientId = extractClientId(item);
                    boolean isReady = viewModel.getReadyPlayers().contains(clientId);
                    // boolean isAI = viewModel.getAiPlayers().contains(clientId);
                    boolean isMe = clientId == viewModel.getClientID();

                    String displayName =
                            item;
                    //  if (isAI) displayName += " 🤖";
                    displayName = (isReady ? "✔ " : "❌ ") + displayName;

                    Label nameLabel = new Label(displayName);
                    nameLabel.setStyle(
                            isMe ? "-fx-text-fill: rgba(11,129,11,0.91); -fx-font-weight: bold;" :
                                    "-fx-text-fill: #001aff;"
                    );

                    setText(null);
                    setGraphic(nameLabel);
                }
            }

            private int extractClientId(String text) {
                try {
                    int start = text.lastIndexOf('(');
                    int end = text.lastIndexOf(')');
                    if (start >= 0 && end > start) {
                        return Integer.parseInt(text.substring(start + 1, end));
                    }
                } catch (Exception e) {
                    log.warning(() -> "[WARN] clientId konnte nicht aus '" + text + "' extrahiert werden. ");
                }
                return -1;
            }
        });

        //ready button
        Button readyButton = new Button();
        readyButton.setStyle(
                """
                -fx-background-color: linear-gradient(to bottom right, #da6713, #ea1616);
                -fx-text-fill: white;
                -fx-font-weight: bold;
                -fx-background-radius: 6;
                -fx-padding: 6 16 6 16;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5,0,0,1);
                -fx-cursor: hand;
                """
        );
        readyButton.textProperty().bind(viewModel.readyButtonLabelProperty());
        readyButton.disableProperty().bind(viewModel.readyButtonDisabledProperty());
        readyButton.setOnAction(e -> viewModel.toggleReadyStatus());

        // Phase-Anzeige
        Label phaseLabel = new Label();
        phaseLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, black, 2, 0.5, 0, 0);");
        // phaseLabel.textProperty().bind(viewModel.currentPhaseTextProperty());

        Label infoLabel = new Label("Info");
        infoLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, black, 2, 0.5, 0, 0);");

        ListView<String> systemMessageList = new ListView<>(viewModel.getSystemMessages());
        systemMessageList.setMouseTransparent(true);
        systemMessageList.setFocusTraversable(false);
        systemMessageList.setStyle("""
        -fx-background-color: rgba(255,255,255,0.8);
        -fx-control-inner-background: transparent;
        -fx-background-radius: 10;
        -fx-padding: 5;
        -fx-border-color: white;
        -fx-border-radius: 10;
        """);
        systemMessageList.setMaxHeight(80); // Höhe begrenzen

        // Roboteranzeige
        Label myRobotLabel = new Label("Dein Roboter:");
        myRobotLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, black, 2, 0.5, 0, 0);");
        ImageView myRobotImage = new ImageView();
        myRobotImage.imageProperty().bind(viewModel.myRobotImageProperty());
        myRobotImage.setFitWidth(80);
        myRobotImage.setPreserveRatio(true);
        // Energie-Anzeige unter dem Roboter
        Label energyLabel = new Label();
        energyLabel.textProperty().bind(viewModel.energyProperty().asString("Energy: %d 🔋"));
        energyLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px; -fx-effect: dropshadow(gaussian, black, 1, 0.3, 0, 0);");

        // Roboter-Box inkl. Energieanzeige
        VBox robotBox = new VBox(5, myRobotLabel, myRobotImage, energyLabel);
        robotBox.setAlignment(Pos.CENTER);

        robotBox.setAlignment(Pos.
                CENTER);

        // TimerBox enthält Label, ProgressBar, Countdown
        Label timerLabel = new Label("Timer");
        timerLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, black, 2, 0.5, 0, 0);");

        ProgressBar timerBar = new ProgressBar(0);
        timerBar.progressProperty().bind(viewModel.timerProgressProperty());
        timerBar.setPrefHeight(16);
        timerBar.setMaxWidth(Double.MAX_VALUE);
        timerBar.setStyle("""
    -fx-accent: #da6713;
    -fx-control-inner-background: rgba(255,255,255,0.8);
    -fx-background-radius: 8;
    -fx-border-radius: 8;
    -fx-border-color: white;
""");

        Label countdownLabel = new Label();
        countdownLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, black, 1, 0.3, 0, 0);");

        // Reaktion auf Timerfortschritt
        viewModel.timerProgressProperty().addListener((obs, oldVal, newVal) -> {
            double progress = newVal.doubleValue();
            boolean active = progress > 0.0 && progress <= 1.0;

            int secondsLeft = (int) Math.ceil(progress * 30);
            countdownLabel.setText(active ? "Noch " + secondsLeft + " Sekunden" : "");
        });

        // Jetzt TimerBox zentriert anlegen
        VBox timerBox = new VBox(4, timerLabel, timerBar, countdownLabel);
        timerBox.setAlignment(Pos.CENTER);
        timerBox.setMaxWidth(Double.MAX_VALUE);


        // Chat-Bereich
        Label chatLabel = new Label("Chat");
        chatLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, black, 2, 0.5, 0, 0);");

        // Chat-List transparent hinterlegen
        ListView<String> chatList = new ListView<>(viewModel.getChatMessages());
        chatList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setBackground(null); // Hintergrund zurücksetzen
                } else {
                    setText(item);

                    // Hintergrund und Rahmen explizit entfernen (gegen Zebrastreifen)
                    setStyle("-fx-background-color: transparent;");

                    // Eigene Nachricht
                    if (item.startsWith("Du an ") || item.startsWith("[Command gesendet]")) {
                        setStyle("-fx-text-fill: rgba(11,129,11,0.91);");
                    }
                    // Systemnachricht
                    else if (item.startsWith("[System]") || item.startsWith("[Fehler]")) {
                        setTextFill(Color.BLACK);
                    }
                    // Nachricht von anderen
                    else {
                        setStyle("-fx-text-fill: #001aff;");
                    }
                }
            }
        });

        // Auto-Scroll bei neuer Nachricht
        viewModel.getChatMessages().addListener((javafx.collections.ListChangeListener<String>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    Platform.runLater(() -> chatList.scrollTo(viewModel.getChatMessages().size() - 1));
                }
            }
        });

        chatList.setStyle(
                "-fx-background-color: rgba(255,255,255,0.8); " +  // halbtransparentes Weiß
                        "-fx-control-inner-background: rgba(255, 255, 255, 0.85); " + // Innenbereich transparent
                        "-fx-background-radius: 8; " +
                        "-fx-border-radius: 8; " +
                        "-fx-border-color: white;"
        );




        Region chatBackground = new Region();
        chatBackground.setStyle("-fx-background-color: rgba(255, 255,255,0.2); -fx-background-radius: 8px;");
        chatBackground.setPrefHeight(150);
        chatBackground.setMaxWidth(Double.MAX_VALUE);

        StackPane chatWrapper = new StackPane(chatBackground, chatList);
        VBox.setVgrow(chatWrapper, Priority.ALWAYS);

        // Chat-Eingabe
        HBox chatInputRow = new HBox(5);

        ComboBox<String> recipientBox = new ComboBox<>(viewModel.getPrivateMessageRecipients());
        recipientBox.valueProperty().bindBidirectional(viewModel.selectedRecipientProperty());
        recipientBox.setPrefWidth(80);
        recipientBox.setStyle(
                "-fx-background-radius: 3; " +
                        "-fx-border-radius: 3; " +
                        "-fx-border-color: #ea1616; " +
                        "-fx-padding: 1 6 1 6; " +
                        "-fx-font-size: 11px;"
        );

        TextField chatInput = new TextField();
        chatInput.setPromptText("Nachricht eingeben…");
        chatInput.setMaxWidth(400);
        HBox.setHgrow(chatInput, Priority.ALWAYS);

        Button sendBtn = new Button("Senden");
        sendBtn.setOnAction(e -> {
            String msg = chatInput.getText();
            if (msg != null && !msg.isBlank()) {
                viewModel.currentChatMessageProperty().set(msg);
                viewModel.sendChatMessage();
                chatInput.clear();
            }
        });
        chatInput.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) sendBtn.fire();
        });

        chatInputRow.getChildren().addAll(recipientBox, chatInput, sendBtn);

        chatInputRow.setStyle(
                "-fx-background-color: rgba(255,255,255,0.8); " +
                        "-fx-background-radius: 3; " +
                        "-fx-border-radius: 3; " +
                        "-fx-border-color: white; " +
                        "-fx-padding: 6;"
        );
        chatInput.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) sendBtn.fire();
        });

        //lautstärke ändern
        Label volumeLabel = new Label("Musiklautstärke");
        volumeLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, black, 2, 0.5, 0, 0);");

        Slider volumeSlider = new Slider(0, 1, 0.2); // min=0 (leise), max=1 (laut), start=0.2
        volumeSlider.setShowTickLabels(true);
        volumeSlider.setShowTickMarks(true);
        volumeSlider.setMajorTickUnit(0.25);
        volumeSlider.setBlockIncrement(0.05);
        volumeSlider.setPrefWidth(200);

        // Listener, der beim Bewegen des Sliders die Lautstärke setzt
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            de.lmu.cleverecousins.util.MusicPlayer.setVolume(newVal.doubleValue());
        });


        // Zusammenfügen
        leftBox.getChildren().addAll(
                playersLabel, playerListView,
                readyButton,
                new Separator(), phaseLabel,
                infoLabel, systemMessageList,
                new Separator(), robotBox,
                new Separator(), timerBox,
                new Separator(), chatLabel,
                chatWrapper, chatInputRow,
                new Separator(),
                volumeLabel, volumeSlider
        );


        // ----- Rechte Seite -----
        VBox rightBox = new VBox(15);
        rightBox.setPadding(new Insets(15));
        rightBox.setAlignment(Pos.TOP_CENTER);
        rightBox.prefWidthProperty().bind(widthProperty().multiply(0.7));

        // Hintergrund für rechten Bereich (einmalig)
        URL rightBgUrl = getClass().getResource("/design/game_area_background.jpg");
        if (rightBgUrl != null) {
            BackgroundImage rightBg = new BackgroundImage(
                    new Image(rightBgUrl.toExternalForm(), true),
                    BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    new BackgroundSize(100, 100, true, true, true, true)
            );
            rightBox.setBackground(new Background(rightBg));
        } else {
            log.severe("Hintergrundbild rechts nicht gefunden: /design/game_area_background.jpg");
        }

        // Überschrift
        URL titleImageUrl = getClass().getResource("/design/game_title.png");
        ImageView titleImage = titleImageUrl != null ? new ImageView(new Image(titleImageUrl.toExternalForm())) : new ImageView();
        titleImage.setPreserveRatio(true);
        titleImage.setFitHeight(80);
        titleImage.fitHeightProperty().bind(rightBox.heightProperty().multiply(0.08));

        // Map-Auswahl
        ComboBox<String> mapCombo = new ComboBox<>(viewModel.getAvailableMaps());
        mapCombo.visibleProperty().bind(viewModel.showMapSelectionProperty());
        mapCombo.valueProperty().bindBidirectional(viewModel.selectedMapProperty());
        mapCombo.setPromptText("Map auswählen");
        mapCombo.setPrefWidth(200);
        mapCombo.setStyle("""
        -fx-background-radius: 8;
        -fx-border-radius: 8;
        -fx-border-color: white;
        -fx-padding: 4 8 4 8;
        -fx-background-color: rgba(255,255,255,0.9);
        -fx-font-size: 13px;
""");

        Tooltip mapTooltip = new Tooltip("Wähle eine Karte für das Spiel");
        Tooltip.install(mapCombo, mapTooltip);

        Button confirmMap = new Button("Map wählen");
        confirmMap.visibleProperty().bind(viewModel.showMapSelectionProperty());
        confirmMap.disableProperty().bind(viewModel.canSelectMapProperty().not());
        confirmMap.setOnAction(e -> {
            if (mapCombo.getValue() != null) viewModel.sendMapSelection(mapCombo.getValue());
        });
        confirmMap.setStyle("""
        -fx-background-color: linear-gradient(to bottom right, #d32d2d, #ea1616);
        -fx-text-fill: white;
        -fx-font-weight: bold;
        -fx-background-radius: 6;
        -fx-padding: 6 16 6 16;
        -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5,0,0,1);
        -fx-cursor: hand;
""");

        VBox mapSelectionBox = new VBox(8, mapCombo, confirmMap);
        mapSelectionBox.setAlignment(Pos.CENTER);
        mapSelectionBox.setPadding(new Insets(10));


        // Map-Container (StackPane)
        StackPane mapContainer = new StackPane();
        mapContainer.setMinHeight(0);
        VBox.setVgrow(mapContainer, Priority.ALWAYS);

        viewModel.renderedMapProperty().addListener((observable, oldNode, newNode) -> {
            if (newNode != null) {
                mapContainer.getChildren().setAll(newNode); // <-- zuerst einfügen, damit Größe berechnet werden kann

                if (newNode instanceof MapRenderer mr) {
                    mapRenderer = mr;

                    mapRenderer.setStartPointClickListener((x, y) -> {
                        log.fine(() -> "Startpunkt gewählt bei " + x + ", " + y);
                        viewModel.selectStartPoint(x, y);
                    });

                    // Jetzt: Resize-Listener setzen
                    setupMapResizeListener(mapContainer, mapRenderer);

                    // Initiales Resize triggern
                    Platform.runLater(() -> {
                        double width = mapContainer.getWidth();
                        double height = mapContainer.getHeight();
                        if (width > 0 && height > 0 && mr.getMapWidth() > 0 && mr.getMapHeight() > 0) {
                            int tileWidth = (int) (width / mr.getMapWidth());
                            int tileHeight = (int) (height / mr.getMapHeight());
                            int newTileSize = Math.max(20, Math.min(tileWidth, tileHeight));
                            mr.setTileSize(newTileSize);
                        }
                    });
                }
            } else {
                mapContainer.getChildren().clear();
                mapRenderer = null;
            }
        });

        // MapRenderer erzeugen mit der aktuell ausgewählten Map
        String currentMap = viewModel.selectedMapProperty().get();

        // Kartenbereich (VBox)
        CardView cardView = new CardView(viewModel);
        cardView.setMinHeight(120); // Mindesthöhe für Kartenzeile
        cardView.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(cardView, Priority.ALWAYS); // CardView darf wachsen

        VBox cardAreaBox = new VBox(cardView);
        cardAreaBox.setPadding(new Insets(10));
        cardAreaBox.setSpacing(10);
        // Lass die Höhe vom umgebenden Layout bestimmen – kein maxHeight begrenzen!
        cardAreaBox.setMinHeight(120);
        cardAreaBox.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(cardAreaBox, Priority.ALWAYS); // darf mitwachsen

        // Map- und Kartenbereich gruppieren
        VBox mapAndCardArea = new VBox(10);
        mapAndCardArea.setMinHeight(0);
        mapAndCardArea.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(mapAndCardArea, Priority.ALWAYS); // gesamter Bereich wächst

        mapAndCardArea.getChildren().setAll(
                mapContainer,
                new Separator(),
                cardAreaBox
        );

        // Map und Kartenbereich flexibel halten
        VBox.setVgrow(mapContainer, Priority.ALWAYS);
        VBox.setVgrow(cardAreaBox, Priority.ALWAYS); // jetzt darf auch Kartenbereich wachsen

        // Rechte Seite zusammenbauen
        rightBox.getChildren().setAll(
                titleImage,
                mapSelectionBox,
                mapAndCardArea
        );


        // Hauptlayout setzen
        HBox mainLayout = new HBox(leftBox, rightBox);
        mainLayout.setSpacing(0);
        HBox.setHgrow(leftBox, Priority.ALWAYS);
        HBox.setHgrow(rightBox, Priority.ALWAYS);
        leftBox.setMaxWidth(Double.MAX_VALUE);
        rightBox.setMaxWidth(Double.MAX_VALUE);
        setCenter(mainLayout);

        // Hauptlayout dynamisch binden
        mainLayout.prefWidthProperty().bind(widthProperty());
        mainLayout.prefHeightProperty().bind(heightProperty());
        mainLayout.setMinWidth(0);
        mainLayout.setMinHeight(0);
        mainLayout.setMaxWidth(Double.MAX_VALUE);
        mainLayout.setMaxHeight(Double.MAX_VALUE);

        // linke & rechte Box: kein fester Platz
        leftBox.setMinWidth(0);
        rightBox.setMinWidth(0);
        leftBox.setMaxWidth(Double.MAX_VALUE);
        rightBox.setMaxWidth(Double.MAX_VALUE);
        leftBox.setMinHeight(0);
        rightBox.setMinHeight(0);
        leftBox.setMaxHeight(Double.MAX_VALUE);
        rightBox.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(leftBox, Priority.ALWAYS);
        VBox.setVgrow(rightBox, Priority.ALWAYS);

        // Optionale Ausgangsgröße der Gesamt-View
        setPrefSize(1200, 900);
        setMinSize(600, 400); // hier bewusst kleiner gemacht

    }

    private void setupMapResizeListener(StackPane mapContainer, MapRenderer mapRenderer) {
        ChangeListener<Number> resizeListener = (obs, oldVal, newVal) -> {
            Platform.runLater(() -> {
                double width = mapContainer.getWidth();
                double height = mapContainer.getHeight();

                if (width <= 0 || height <= 0 || mapRenderer.getMapWidth() <= 0 || mapRenderer.getMapHeight() <= 0) return;

                int tileWidth = (int) (width / mapRenderer.getMapWidth());
                int tileHeight = (int) (height / mapRenderer.getMapHeight());

                int newTileSize = Math.max(20, Math.min(tileWidth, tileHeight));

                if (newTileSize != mapRenderer.getTileSize()) {
                    mapRenderer.setTileSize(newTileSize);
                }
            });
        };

        mapContainer.widthProperty().addListener(resizeListener);
        mapContainer.heightProperty().addListener(resizeListener);
    }

}
