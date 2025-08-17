package de.lmu.cleverecousins.viewmodel;

import de.lmu.cleverecousins.Client;
import de.lmu.cleverecousins.UserInputHandler;
import de.lmu.cleverecousins.ViewModel;
import de.lmu.cleverecousins.protocol.message.MapSelectedMessage;
import de.lmu.cleverecousins.protocol.message.SendChatMessage;
import de.lmu.cleverecousins.protocol.message.SetStartingPointMessage;
import de.lmu.cleverecousins.protocol.messageBody.GameStartedBody;
import de.lmu.cleverecousins.protocol.messageBody.MapSelectedBody;
import de.lmu.cleverecousins.protocol.messageBody.SendChatBody;
import de.lmu.cleverecousins.protocol.messageBody.SetStartingPointBody;
import de.lmu.cleverecousins.view.components.MapRenderer;
import de.lmu.util.LogConfigurator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JavaFX ViewModel for the lobby and early-game phases.
 * <p>
 * It wires the network {@link Client} to observable UI state (players, chat, map selection,
 * start-point picking, timers, energy, etc.). The class exposes numerous JavaFX properties
 * that views can bind to, and provides methods that the controller/UI can trigger
 * (e.g. sending chat, toggling readiness, selecting a map/start point, programming cards).
 * </p>
 *
 * <h3>Main responsibilities</h3>
 * <ul>
 *   <li>Maintain chat state and dispatch outgoing chat/command messages.</li>
 *   <li>Track players (names, IDs, ready/AI status) and update recipient lists.</li>
 *   <li>Handle map selection UI flow and render the chosen map through {@link MapRenderer}.</li>
 *   <li>Guide the setup phase (start-point selection) and subsequent programming/execution phases.</li>
 *   <li>Mirror server-driven phase/timer events into observable properties for the UI.</li>
 * </ul>
 *
 * <h4>Threading</h4>
 * All UI mutations are wrapped with {@link Platform#runLater(Runnable)} to ensure they run on the FX thread.
 * Any external callers from networking code should respect this pattern.
 */
public class LobbyViewModel implements ViewModel {

    private static final Logger log = Logger.getLogger(LobbyViewModel.class.getName());

    static {
        LogConfigurator.configureRootLogger(Level.FINE);
    }

    /** Network client used to send messages back to the server. */
    private final Client client;

    // ---------------- Chat ----------------
    private final StringProperty currentChatMessage = new SimpleStringProperty();
    private final StringProperty selectedRecipient = new SimpleStringProperty("Alle");
    private final ObservableList<String> chatMessages = FXCollections.observableArrayList();

    // ---------------- Players ----------------
    final ObservableList<String> players = FXCollections.observableArrayList();
    private final Map<Integer, String> playerMap = new HashMap<>();
    private final ObservableList<String> privateMessageRecipients = FXCollections.observableArrayList();

    // ---------------- Ready & Map selection ----------------
    private final BooleanProperty isReadyProperty = new SimpleBooleanProperty(false);
    private final StringProperty readyButtonLabel = new SimpleStringProperty("Bereit");
    private final BooleanProperty readyButtonDisabled = new SimpleBooleanProperty(false);

    private final ObservableList<String> availableMaps = FXCollections.observableArrayList();
    private final StringProperty selectedMap = new SimpleStringProperty();
    private final BooleanProperty showMapSelection = new SimpleBooleanProperty(false);
    private final BooleanProperty canSelectMap = new SimpleBooleanProperty(false);
    private final StringProperty chosenMapName = new SimpleStringProperty();

    private final ReadOnlyObjectWrapper<Image> myRobotImage = new ReadOnlyObjectWrapper<>();

    // ---------------- Start point phase ----------------
    private final ObservableList<String> availableStartPoints = FXCollections.observableArrayList();
    private final BooleanProperty showStartPointSelection = new SimpleBooleanProperty(false);
    public BooleanProperty getShowStartPointSelection() { return showStartPointSelection; }

    // ---------------- Rendered map node ----------------
    private final ObjectProperty<Node> renderedMap = new SimpleObjectProperty<>();
    public ObjectProperty<Node> renderedMapProperty() { return renderedMap; }

    // ---------------- Card programming ----------------
    private final CardViewModel cardViewModel = new CardViewModel();
    public CardViewModel getCardViewModel() { return cardViewModel; }

    // Map renderer helper
    private MapRenderer mapRenderer;

    // ---------------- Phase flags ----------------
    private final BooleanProperty showCardSelection = new SimpleBooleanProperty(false);
    private final BooleanProperty showProgrammingPhase = new SimpleBooleanProperty(false);
    private final BooleanProperty showExecutionPhase = new SimpleBooleanProperty(false);
    private int clientID = -1;
    private final BooleanProperty isMyTurn = new SimpleBooleanProperty(false);

    // System message bridge (chat + separate UI element)
    private final StringProperty systemPrompt = new SimpleStringProperty();
    public StringProperty systemPromptProperty() { return systemPrompt; }
    public String getSystemPrompt() { return systemPrompt.get(); }

    private final ObservableList<String> systemMessages = FXCollections.observableArrayList();
    public ObservableList<String> getSystemMessages() {
        return systemMessages;
    }

    // Additional status lists
    private final ObservableList<Integer> readyPlayers = FXCollections.observableArrayList();
    private final ObservableList<Integer> aiPlayers = FXCollections.observableArrayList();
    private int ownClientId = -1;

    public ObservableList<Integer> getReadyPlayers() { return readyPlayers; }
    public ObservableList<Integer> getAIPlayers() { return aiPlayers; }
    public int getClientID() { return ownClientId; }

    public BooleanProperty getShowCardSelection() {
        return showCardSelection;
    }

    // View getters
    public BooleanProperty showCardSelectionProperty() { return showCardSelection; }
    public ReadOnlyBooleanProperty showProgrammingPhaseProperty() { return showProgrammingPhase; }
    public ReadOnlyBooleanProperty showExecutionPhaseProperty() { return showExecutionPhase; }

    /**
     * Returns the player's display name for a given client ID.
     *
     * @param clientId server-assigned client ID
     * @return name or fallback ("Spieler <id>")
     */
    public String getPlayerNameById(int clientId) {
        return playerMap.getOrDefault(clientId, "Spieler " + clientId);
    }

    private final Map<Integer, int[]> playerStartPoints = new HashMap<>();

    /**
     * Injects a {@link MapRenderer} instance and wires the start-point click listener.
     *
     * @param renderer renderer to display the map
     */
    public void setMapRenderer(MapRenderer renderer) {
        this.mapRenderer = renderer;
        this.mapRenderer.setStartPointClickListener((x, y) -> selectStartPoint(x, y));
    }

    /**
     * Sets this client's ID, updates the legacy backing field, and refreshes the
     * list of possible private message recipients.
     *
     * @param clientID server-assigned ID of this client
     */
    @Override
    public void setClientID(int clientID) {
        this.ownClientId = clientID;
        this.clientID = clientID; // bestehend
        updatePrivateRecipients();
    }

    /**
     * Creates a new LobbyViewModel bound to the given client.
     * Sets up listeners to keep derived UI state (e.g. ready button label) in sync.
     *
     * @param client network client used for sending protocol messages
     */
    public LobbyViewModel(Client client) {
        this.client = client;
        selectedRecipient.set("Alle");

        isReadyProperty.addListener((obs, oldVal, newVal) ->
                readyButtonLabel.set(newVal ? "Nicht mehr bereit" : "Bereit")
        );

        updatePrivateRecipients();

        // Refresh the visual player list when ready state changes
        readyPlayers.addListener((javafx.collections.ListChangeListener<? super Integer>) change -> {
            Platform.runLater(() -> {
                ObservableList<String> refreshed = FXCollections.observableArrayList(players);
                players.setAll(refreshed);
            });
        });
    }


    // -------------------------------------------------
    // Chat API
    // -------------------------------------------------


    /**
     * @return bound text the user is currently typing into the chat input field
     */
    public StringProperty currentChatMessageProperty() { return currentChatMessage; }

    /**
     * @return selected recipient name/entry (e.g. "Alle" or "Alice (3)") for outgoing chat messages
     */
    public StringProperty selectedRecipientProperty() { return selectedRecipient; }

    /**
     * @return observable list of all chat lines shown in the lobby chat view
     */
    public ObservableList<String> getChatMessages() { return chatMessages; }

    /**
     * Convenience overload that delegates to
     * {@link #receiveChatMessage(String, String, int, boolean)} with {@code fromId = -1}
     * and {@code isPrivate = false}.
     *
     * @param from    sender name
     * @param message chat text
     */
    @Override
    public void receiveChatMessage(String from, String message) { receiveChatMessage(from, message, -1, false); }

    /**
     * Handles an incoming chat message (optionally private) from another client.
     * Ignores echoes of our own messages.
     *
     * @param from      sender name
     * @param message   chat text
     * @param fromId    sender client ID (use -1 if unknown)
     * @param isPrivate {@code true} if the message is private, otherwise {@code false}
     */
    @Override
    public void receiveChatMessage(String from, String message, int fromId, boolean isPrivate) {
        if (fromId == this.clientID) return;
        chatMessages.add(isPrivate ? "[Privat] " + from + " an dich: " + message : from + ": " + message);
    }

    /**
     * Displays a system/status message. The text is appended to the chat log
     * and also exposed via {@code systemPrompt} for dedicated UI elements.
     *
     * @param message system message to show
     */
    @Override
    public void showSystemPrompt(String message) {
        Platform.runLater(() -> {
            chatMessages.add("[System] " + message);       // ← bleibt im Chat
            systemPrompt.set(message);                     // ← neues UI-Element
        });
    }

    /** Sends a chat or command message depending on prefixes. */
    public void sendChatMessage() {
        String msg = currentChatMessage.get();
        String recipient = selectedRecipient.get();
        if (msg != null && !msg.isBlank()) {
            currentChatMessage.set("");

            // Slash / hash commands are delegated to UserInputHandler
            if (msg.startsWith("/") || msg.startsWith("#")) {
                try {
                    String json = new UserInputHandler().processInput(msg);
                    client.sendRaw(json);
                    chatMessages.add("[Command gesendet] " + msg);
                    return;
                } catch (Exception e) {
                    chatMessages.add("[Fehler] Command konnte nicht verarbeitet werden");
                    return;
                }
            }

            // Normal chat message
            int to = -1;
            if (!"Alle".equals(recipient)) {
                Matcher matcher = Pattern.compile(".*\\((\\d+)\\)$").matcher(recipient);
                if (matcher.find()) to = Integer.parseInt(matcher.group(1));
                else {
                    chatMessages.add("[Fehler] Empfänger-ID konnte nicht ermittelt werden.");
                    return;
                }
            }
            chatMessages.add(to == -1 ? "Du an Alle: " + msg : "Du an " + recipient + ": " + msg);
            client.send(new SendChatMessage(new SendChatBody(msg, to)));
        }
    }


    // -------------------------------------------------
    // Player list & status
    // -------------------------------------------------

    /**
     * @return observable list of player display strings (e.g. "Alice (3)") shown in the lobby
     */
    public ObservableList<String> getPlayers() { return players; }

    /**
     * Adds a player (name + client ID) to the lobby list and updates helper maps/recipients.
     *
     * @param name     player name
     * @param clientId server-assigned client ID
     */
    @Override
    public void updatePlayerList(String name, int clientId) {
        String display = name + " (" + clientId + ")";
        if (!players.contains(display)) {
            players.add(display);
            playerMap.put(clientId, name);
            updatePrivateRecipients();
        }
    }

    /** Updates ready list and chat when a player's ready state changes. */
    public void receiveReadyStatus(int clientId, boolean isReady) {
        if (clientId == this.clientID) return;

        Platform.runLater(() -> {
            if (isReady) {
                if (!readyPlayers.contains(clientId)) readyPlayers.add(clientId);
            } else {
                readyPlayers.remove((Integer) clientId);
            }

            chatMessages.add("[System] " + playerMap.getOrDefault(clientId, "Spieler " + clientId)
                    + (isReady ? " ist jetzt bereit." : " ist nicht mehr bereit."));
        });
    }

    /** Removes a player from all local lists (e.g., when disconnected). */
    public void removePlayer(int clientId) {
        Platform.runLater(() -> {
            players.removeIf(p -> p.endsWith("(" + clientId + ")"));
            playerMap.remove(clientId);
            chatMessages.add("[System] Spieler " + clientId + " wurde aus der Spielerliste entfernt.");
            updatePrivateRecipients();
        });
    }

    /** Refreshes the private-recipient dropdown. */
    private void updatePrivateRecipients() {
        Platform.runLater(() -> {
            privateMessageRecipients.clear();
            privateMessageRecipients.add("Alle");
            players.stream()
                    .filter(p -> !p.equals("Alle") && !p.endsWith("(" + clientID + ")"))
                    .forEach(privateMessageRecipients::add);
            if (!privateMessageRecipients.contains(selectedRecipient.get())) selectedRecipient.set("Alle");
        });
    }

    /**
     * @return observable list of selectable private chat recipients (excludes yourself and “Alle”)
     */
    public ObservableList<String> getPrivateMessageRecipients() { return privateMessageRecipients; }


    // -------------------------------------------------
    // Ready & Map selection
    // -------------------------------------------------

    /**
     * @return label text for the “ready” button (bindable JavaFX property)
     */
    public StringProperty readyButtonLabelProperty() { return readyButtonLabel; }

    /**
     * @return whether the “ready” button is currently disabled (bindable JavaFX property)
     */
    public BooleanProperty readyButtonDisabledProperty() { return readyButtonDisabled; }

    /** Toggles player's ready flag and updates server + chat. */
    public void toggleReadyStatus() {
        if (readyButtonDisabled.get()) return;

        boolean newStatus = !isReadyProperty.get();
        isReadyProperty.set(newStatus);

        // Maintain our own ready list entry
        if (newStatus) {
            if (!readyPlayers.contains(clientID)) {
                readyPlayers.add(clientID);
            }
        } else {
            readyPlayers.remove(Integer.valueOf(clientID));
        }

        chatMessages.add("[System] " + (newStatus ? "Du bist jetzt bereit." : "Bereitschaft zurückgezogen."));
        client.send(String.format("/setStatus %s", newStatus));
    }

    /**
     * @return observable list of map names offered by the server for selection
     */
    public ObservableList<String> getAvailableMaps() { return availableMaps; }

    /**
     * @return bindable property holding the currently selected/preselected map name
     */
    public StringProperty selectedMapProperty() { return selectedMap; }

    /**
     * @return flag (property) indicating whether the map selection UI should be shown
     */
    public BooleanProperty showMapSelectionProperty() { return showMapSelection; }

    /**
     * @return flag (property) indicating whether this client is allowed to pick a map
     */
    public BooleanProperty canSelectMapProperty() { return canSelectMap; }

    /** Shows/hides the map selection UI and preselects the first map if available. */
    public void showMapSelection(List<String> maps, boolean allowSelection) {
        Platform.runLater(() -> {
            log.fine(() -> "[DEBUG] showMapSelection aufgerufen. alloSelection = " + allowSelection);
            log.fine(() -> "[DEBUG] Verfügbare Maps: " + maps);

            availableMaps.setAll(maps);

            if (!maps.isEmpty()) {
                selectedMap.set(maps.get(0)); // Erste Map vorauswählen
            }

            if (allowSelection) {
                log.fine("[DEBUG] Spieler darf Map auswählen - Auswahlfenster wird angezeigt. ");
                showMapSelection.set(true);
                canSelectMap.set(true);
                readyButtonDisabled.set(true); // Ready-Button blockieren während Auswahl
            } else {
                log.fine("[DEBUG] Spieler darf keine Map auswählen - Auswahlfenster bleibt verborgen");
                showMapSelection.set(false);
                canSelectMap.set(false);
                readyButtonDisabled.set(false); // Optional: wieder aktivieren
            }
        });
    }

    /** Sends the chosen map to the server and hides the selection UI. */
    public void sendMapSelection(String mapName) {
        if (!canSelectMap.get()) return;
        String myName = client.getPlayerName();
        client.send(new MapSelectedMessage(
                new MapSelectedBody(mapName, myName)
        ));
        showMapSelection.set(false);
        canSelectMap.set(false);
    }

    /** Updates UI after a map was selected (by us or another player) and renders it. */
    public void updateMapSelection(String mapName, String playerName) {
        Platform.runLater(() -> {
            chosenMapName.set(mapName);
            showMapSelection.set(false);
            canSelectMap.set(false);

            chatMessages.add("[System] "
                    + playerName
                    + " hat die Map '"
                    + mapName
                    + "' ausgewählt.");

            String path = switch (mapName) {
                case "Dizzy Highway" -> "map-dizzy-highway.json";
                case "Lost Bearings" -> "map-lost-bearings.json";
                case "Extra Crispy" -> "map-extra-crispy.json";
                case "Death Trap" -> "map-death-trap.json";
                default -> mapName.toLowerCase().replace(" ", "-") + ".json";
            };

            int tileSize = 80;  // Hier die gewünschte Größe setzen

            this.mapRenderer = new MapRenderer(path, tileSize);
            setMapRenderer(this.mapRenderer);
            this.mapRenderer.setStartPointClickListener((x, y) -> {
                log.fine(() -> "Startpunkt gewählt bei " + x + ", " + y);
                selectStartPoint(x, y);
            });
            renderedMap.set(this.mapRenderer);
        });
    }

    /** Called when the server announces a new active phase. */
    public void onActivePhase(int phase) {
        Platform.runLater(() -> {
            showCardSelection.set(phase == 1);
            showProgrammingPhase.set(phase == 2);
            showExecutionPhase.set(phase == 3);

            if (phase > 0) {
                showStartPointSelection.set(false);
                availableStartPoints.clear(); // <<< hier ergänzen
            }
        });
    }

    /** Sets turn ownership and, if it's us, reveals local start-point options. */
    public void onCurrentPlayer(int currentPlayerId) {
        Platform.runLater(() -> {
            isMyTurn.set(currentPlayerId == clientID); // ⬅️ Hier setzen

            if (currentPlayerId == clientID) {
                List<String> points = List.of("0,3", "0,6", "1,1", "2,4");
                availableStartPoints.setAll(points);
                showStartPointSelection.set(true);
            } else {
                showStartPointSelection.set(false);
            }
        });
    }

    // ---------------- Game start & start point ----------------
    /**
     * @return read-only property with the name of the map that was chosen (may be {@code null} before selection)
     */
    public ReadOnlyStringProperty chosenMapNameProperty() { return chosenMapName; }

    /**
     * @return observable list of available start points formatted as {@code "x,y"} strings
     */
    public ObservableList<String> getAvailableStartPoints() { return availableStartPoints; }

    /**
     * @return read-only flag indicating whether the UI should display the start-point selection dialog
     */
    public ReadOnlyBooleanProperty showStartPointSelectionProperty() { return showStartPointSelection; }

    /**
     * Called when the server sends {@code GameStarted}. Initializes map rendering and collects start points.
     */
    public void startGameInLobby(GameStartedBody gsBody) {
        Platform.runLater(() -> {
            // 1) Map erneut zeichnen
            if (chosenMapName.get() != null) {
                String mapPath = switch (chosenMapName.get()) {
                    case "Dizzy Highway" -> "map-dizzy-highway.json";
                    case "Lost Bearings" -> "map-lost-bearings.json";
                    case "Extra Crispy"  -> "map-extra-crispy.json";
                    case "Death Trap"    -> "map-death-trap.json";
                    default              -> chosenMapName.get().toLowerCase().replace(" ", "-") + ".json";
                };
                try {
                    int tileSize = 100;
                    this.mapRenderer = new MapRenderer(mapPath, tileSize);
                    renderedMap.set(this.mapRenderer);
                } catch (Exception e) {
                    chatMessages.add("[Fehler] Map konnte nicht geladen werden: " + e.getMessage());
                }
            }

            // 2) Collect start points
            availableStartPoints.clear();
            for (var col : gsBody.getGameMap())
                for (var row : col)
                    for (var tile : row)
                        if ("StartPoint".equals(tile.getType()))
                            availableStartPoints.add(tile.getX() + "," + tile.getY());

            // 3) Switch UI to setup phase
            showStartPointSelection.set(true);
            readyButtonDisabled.set(true);
            showMapSelection.set(false);
            canSelectMap.set(false);

            chatMessages.add("[System] Setup-Phase: Wähle deinen Startpunkt.");
        });
    }

    /** Called when user selects a start point on the rendered map. */
    public void selectStartPoint(int x, int y) {
        log.fine(() -> "Startpunkt gewählt bei " + x + ", " + y);
        sendStartingPoint(x + "," + y);
        Platform.runLater(() -> {
            showStartPointSelection.set(false);
            readyButtonDisabled.set(false);
        });
    }

    /** Sends the SetStartingPoint message to the server and locks the UI. */
    public void sendStartingPoint(String xy) {
        String[] parts = xy.split(",");
        int x = Integer.parseInt(parts[0]);
        int y = Integer.parseInt(parts[1]);

        SetStartingPointBody body = new SetStartingPointBody();
        body.setX(x);
        body.setY(y);

        client.send(new SetStartingPointMessage(body));

        Platform.runLater(() -> {
            showStartPointSelection.set(false);
            readyButtonDisabled.set(true);  // Ready-Button dauerhaft deaktivieren
        });
    }

    /** Stores a player's start point and logs it to chat. */
    public void updatePlayerStartingPoint(int clientID, int x, int y, String direction) {
        playerStartPoints.put(clientID, new int[]{x,y});
        String name = playerMap.getOrDefault(clientID, "Spieler " + clientID);
        Platform.runLater(() -> chatMessages.add("[System] " + name + " hat Startpunkt bei (" + x + "," + y + ") Richtung " + direction + " gesetzt."));
    }

    /** Sends a SelectedCard message for a given register. */
    public void sendSelectedCard(String cardName, int register) {
        var body = new de.lmu.cleverecousins.protocol.messageBody.SelectedCardBody(cardName, register);
        var message = new de.lmu.cleverecousins.protocol.message.SelectedCardMessage(body);
        client.send(message);
        Platform.runLater(() -> chatMessages.add("[System] Register " + register + " " +
                (cardName != null ? ("gesetzt auf " + cardName) : "geleert.")));
    }

    /** Automatically sent when all 5 slots are filled. */
    public void sendSelectionFinished() {
        var body = new de.lmu.cleverecousins.protocol.messageBody.SelectionFinishedBody(clientID);
        var message = new de.lmu.cleverecousins.protocol.message.SelectionFinishedMessage(body);
        client.send(message);
        Platform.runLater(() -> chatMessages.add("[System] Deine Karten sind bestätigt."));
    }

    /** Sends a PlayCard message for immediate execution. */
    public void sendPlayCard(String cardName) {
        var playCardBody = new de.lmu.cleverecousins.protocol.messageBody.PlayCardBody(cardName);
        var playCardMessage = new de.lmu.cleverecousins.protocol.message.PlayCardMessage(playCardBody);
        client.send(playCardMessage);
        Platform.runLater(() -> chatMessages.add("[System] Deine Karte wurde gespielt: " + cardName));
    }

    /** Updates the robot position on the map and caches our own robot image. */
    public void updateRobotPosition(int clientID, int x, int y, String direction) {
        if (mapRenderer != null) {
            // Bildzuweisung für aktuellen Client sicherstellen
            if (clientID == this.clientID) {
                mapRenderer.assignImageIfNeeded(clientID); // <--- synchron
                Image myImage = mapRenderer.peekAssignedImage(clientID);
                log.fine("[DEBUG] Setze myRobotImage für clientID:" + clientID + " → " + myImage);
                if (myImage != null) {
                    myRobotImage.set(myImage);
                } else {
                    log.fine("[DEBUG] Bild für clientID " + clientID + " ist null. ");
                }
            }
            mapRenderer.updateRobotPosition(clientID, x, y, direction);
        }
    }

    /** Rotates a robot sprite on the map. */
    public void rotateRobotOnMap(int clientID, String rotation) {
        mapRenderer.rotateRobot(clientID, rotation);  // Methode im MapRenderer
    }

    /**
     * @return bindable flag indicating whether the execution phase UI is active
     */
    public BooleanProperty isExecutionPhaseProperty() {
        return showExecutionPhase;
    }

    /**
     * @return bindable flag that is {@code true} when it's this client's turn
     */
    public BooleanProperty isMyTurnProperty() {
        return isMyTurn;
    }

    /**
     * @return read-only property exposing this client's robot image (if assigned)
     */
    public ReadOnlyObjectProperty<Image> myRobotImageProperty() {
        return myRobotImage.getReadOnlyProperty();
    }


    // ---------------- System message property ----------------
    /**
     * @return bindable property holding the latest system/status message for dedicated UI elements
     */
    private final StringProperty currentSystemMessage = new SimpleStringProperty();

    /**
     * @return bindable property holding the most recent system/status message
     */
    public StringProperty currentSystemMessageProperty() {
        return currentSystemMessage;
    }

    /**
     * Sets (and publishes) a new system/status message on the FX thread.
     *
     * @param message text to display as the current system message
     */
    public void showSystemMessage(String message) {
        Platform.runLater(() -> currentSystemMessage.set(message));
    }

    // ---------------- Local timer (visual countdown) ----------------
    /**
     * @return bindable progress value in the range {@code 0.0 .. 1.0} for the local countdown bar
     */
    private final DoubleProperty timerProgress = new SimpleDoubleProperty(0);

    /** Internal Timeline driving the local countdown animation; {@code null} if no timer is active. */
    private Timeline timerTimeline;

    /**
     * @return bindable progress value (0.0–1.0) for the local countdown bar
     */
    public DoubleProperty timerProgressProperty() {
        return timerProgress;
    }

    /** Starts a local visual countdown (linear) for the given duration. */
    public void startLocalTimer(int seconds) {
        timerProgress.set(1.0);

        if (timerTimeline != null) {
            timerTimeline.stop();
        }

        timerTimeline = new Timeline();

        double interval = 0.1;
        int steps = (int) (seconds / interval);

        for (int i = 0; i <= steps; i++) {
            double progressValue = 1.0 - (i * interval / seconds);
            timerTimeline.getKeyFrames().add(new KeyFrame(
                    Duration.seconds(i * interval),
                    new KeyValue(timerProgress, progressValue)
            ));
        }
        timerTimeline.setOnFinished(e -> {
            timerProgress.set(0.0);
            showSystemMessage("Timer abgelaufen.");
        });
        timerTimeline.play();
    }

    // ---------------- Energy property ----------------
    /** Player’s current energy reserve (bindable JavaFX property). Initial value: 5. */
    private final IntegerProperty energy = new SimpleIntegerProperty(5);

    /**
     * @return bindable property representing the player's energy reserve
     */
    public IntegerProperty energyProperty() {
        return energy;
    }

    /**
     * @return current energy value
     */
    public int getEnergy() {
        return energy.get();
    }

    /**
     * Updates the energy value on the FX thread.
     *
     * @param value new energy amount
     */
    public void setEnergy(int value) {
        Platform.runLater(() -> energy.set(value));
    }
}