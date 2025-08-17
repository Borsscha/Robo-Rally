package de.lmu.cleverecousins;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import de.lmu.cleverecousins.cards.Card;
import de.lmu.cleverecousins.cards.programmingCards.CardFactory;
import de.lmu.cleverecousins.protocol.messageBody.*;
import de.lmu.cleverecousins.protocol.messageBody.UsedRobotsBody;
import de.lmu.cleverecousins.viewmodel.LobbyViewModel;
import de.lmu.cleverecousins.viewmodel.LoginViewModel;
import de.lmu.util.LogConfigurator;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Central dispatcher that parses incoming JSON protocol messages from the server
 * and forwards them to the active {@link ViewModel} (or one of its subclasses)
 * as UI updates, system prompts, or game state changes.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>JSON deserialization using Jackson ({@link ObjectMapper}).</li>
 *   <li>Switch-based routing on <code>messageType</code>.</li>
 *   <li>Thread confinement of UI updates to the JavaFX application thread via {@link Platform#runLater(Runnable)}.</li>
 *   <li>Logging of malformed or unhandled messages.</li>
 * </ul>
 *
 * <h4>Threading</h4>
 * All UI-related mutations are wrapped in {@code Platform.runLater} to ensure they run on the JavaFX thread.
 * If you add heavy parsing or long-running tasks, move those off the FX thread before updating the UI.
 */
public class MessageHandler {

    /** Logger used for diagnostics and debugging. */
    private static final Logger logger = Logger.getLogger(MessageHandler.class.getName());

    static {
        LogConfigurator.configureRootLogger(Level.FINE);
    }

    /** Shared Jackson object mapper instance. */
    private final ObjectMapper mapper = new ObjectMapper();

    /** Current view model that receives the processed events. */
    private ViewModel viewModel;

    /**
     * Creates a new handler bound to the given view model.
     *
     * @param viewModel primary view model to notify (may be swapped later)
     */
    public MessageHandler(ViewModel viewModel) {
        this.viewModel = viewModel;
    }

    /**
     * Replaces the view model reference. Useful when changing scenes (e.g. lobby → game view).
     *
     * @param viewModel new view model instance
     */
    public void setViewModel(ViewModel viewModel) {
        this.viewModel = viewModel;
    }

    /**
     * @return the currently active view model
     */
    public ViewModel getViewModel() {
        return viewModel;
    }

    /**
     * Entry point for raw server messages. Performs basic validation, parses the JSON, extracts
     * <code>messageType</code> and dispatches to the appropriate branch. All further work happens on the
     * JavaFX thread to safely update UI state.
     *
     * @param rawJson full JSON string received from the server. Blank or {@code null} strings are ignored.
     */
    public void handle(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) {
            // ignore null or blank messages (e.g. from sudden disconnect)
            return;
        }

        Platform.runLater(() -> {
            try {
                JsonNode root = mapper.readTree(rawJson);
                if (root == null || !root.has("messageType")) {
                    // Nicht mehr im Chat anzeigen, nur still im Log
                    logger.warning("[WARN] Ungültige Nachricht ohne messageType empfangen und verworfen.");

                    return;
                }


                String type = root.get("messageType").asText();
                JsonNode body = root.get("messageBody");

                switch (type) {


                    case "HelloClient" -> {
                        viewModel.showSystemPrompt("Server protocol: " + body.get("protocol").asText());
                        viewModel.showSystemPrompt("Please respond with command /helloServer <groupName> <ifUsingAI>");
                    }

                    case "PlayerAdded" -> {
                        String name = body.get("name").asText();
                        int clientId = body.get("clientID").asInt();
                        int robot = body.get("figure").asInt();

                        if (viewModel instanceof LobbyViewModel lobbyVM) {
                            lobbyVM.updatePlayerList(name, clientId);
                        }

                        viewModel.showSystemPrompt("Added player: " + name +
                                " (Client ID: " + clientId + ", Robot: " + robot + ")");
                    }

                    case "Welcome" -> {
                        int clientId = body.get("clientID").asInt();
                        if (viewModel != null) {
                            viewModel.setClientID(clientId);
                            viewModel.showSystemPrompt("Welcome! Deine Client ID: " + clientId);
                        }
                    }

                    case "PlayerStatus" -> {
                        int clientId = body.get("clientID").asInt();
                        boolean ready = body.get("ready").asBoolean();

                        if (viewModel instanceof LobbyViewModel lobbyViewModel) {
                            lobbyViewModel.receiveReadyStatus(clientId, ready);
                        }
                    }

                    case "PlayerDisconnected" -> {
                        PlayerDisconnectedBody bodyObj = mapper.treeToValue(body, PlayerDisconnectedBody.class);
                        int clientId = bodyObj.getClientID();
                        String playerName = bodyObj.getPlayerName();
                        viewModel.showSystemPrompt(playerName + " (ID " + clientId + ") hat die Verbindung verloren.");

                        if (viewModel instanceof LobbyViewModel lobbyVM) {
                            lobbyVM.removePlayer(clientId);
                        }
                    }

                    case "ReceivedChat", "ReceivedChatMessage" -> {
                        int fromId = body.get("from").asInt();
                        String fromName = body.has("fromName") ? body.get("fromName").asText() : ("Spieler " + fromId);
                        String message = body.get("message").asText();
                        boolean isPrivate = body.has("isPrivate") && body.get("isPrivate").asBoolean();

                        viewModel.receiveChatMessage(fromName, message, fromId, isPrivate);
                    }

                    case "SystemMessage" -> {
                        SystemBody systemBody = mapper.treeToValue(body, SystemBody.class);
                        viewModel.showSystemPrompt(systemBody.getMessage());
                    }

                    case "Alive" -> {
                        // NICHT anzeigen im Chat
                    }

                    case "SelectMapMessage" -> {
                        SelectMapBody mapBody = mapper.treeToValue(body, SelectMapBody.class);
                        List<String> maps = mapBody.getAvailableMaps();
                        int chooserId = mapBody.getAllowedClientId();

                        if (viewModel instanceof LobbyViewModel lobbyVM) {
                            boolean allowSelection = chooserId == lobbyVM.getClientID();
                            lobbyVM.showMapSelection(maps, allowSelection);
                        }
                    }

                    case "MapSelected" -> {
                        MapSelectedBody mapSel = mapper.treeToValue(body, MapSelectedBody.class);
                        if (viewModel instanceof LobbyViewModel lobbyVM) {
                            lobbyVM.updateMapSelection(
                                    mapSel.getMapName(),
                                    mapSel.getPlayerName()   // jetzt der echte Name angezeigt
                            );
                        }
                    }

                    case "GameStarted" -> {
                        // Payload in GameStartedBody parsen
                        GameStartedBody gsBody = mapper.treeToValue(body, GameStartedBody.class);

                        // ViewModel in Spiel-Modus versetzen
                        if (viewModel instanceof LobbyViewModel lobbyVM) {
                            lobbyVM.startGameInLobby(gsBody);
                        }

                        // System-Hinweis ausgeben
                        viewModel.showSystemPrompt("Das Spiel startet jetzt!");
                    }

                    case "PlayCard" -> {} // wird nicht benötigt

                    case "CardPlayed" -> {
                        int clientId = body.get("clientId").asInt();
                        String cardName = body.get("card").asText();
                        viewModel.showSystemPrompt("Spieler " + clientId + " spielte: " + cardName);
                        // viewModel.addPlayedCard(clientId, cardName);
                    }

                    case "ActivePhase" -> {
                        int phase = body.get("phase").asInt();
                        if (viewModel instanceof LobbyViewModel vm) {
                            vm.onActivePhase(phase);
                            if (phase >= 1) {
                                vm.getShowStartPointSelection().set(false);
                                System.out.println("[DEBUG] Hide startpoint selection because phase " + phase);
                            }
                            if (phase == 1) {
                                vm.showCardSelectionProperty().set(true);
                            }
                        }
                        viewModel.showSystemPrompt("Spielphase " + phase);
                    }

                    case "StartingPointTakenMessage", "StartingPointTaken" -> {
                        StartingPointTakenBody bodyObj = mapper.treeToValue(body, StartingPointTakenBody.class);
                        int x = bodyObj.getX();
                        int y = bodyObj.getY();
                        String direction = bodyObj.getDirection();
                        int clientID = bodyObj.getClientID();
                        viewModel.showSystemPrompt("Spieler " + clientID + " hat Startpunkt bei (" + x + ", " + y + ") Richtung " + direction + " gesetzt.");
                    }

                    case "YourCards", "YourCardsMessage" -> {
                        YourCardsBody bodyObj = mapper.treeToValue(body, YourCardsBody.class);
                        List<String> cardNames = bodyObj.getCardsInHand();
                        viewModel.showSystemPrompt("Deine Handkarten: " + String.join(", ", cardNames));

                        if (viewModel instanceof LobbyViewModel lobbyVM) {
                            var cardVM = lobbyVM.getCardViewModel();
                            Platform.runLater(() -> {
                                cardVM.getHandCards().clear();
                                cardVM.getRegisterSlots().clear(); // <<< Register leeren
                                for (int i = 0; i < 5; i++) cardVM.getRegisterSlots().add(null); // wieder 5 leere Slots
                                cardVM.resetSelectionFinished(); // damit Auswahl wieder freigegeben ist
                                for (String name : cardNames) {
                                    Card card = CardFactory.create(name);
                                    if (card != null) {
                                        cardVM.getHandCards().add(card);
                                    } else {
                                        logger.warning("[WARN] Unbekannte Karte vom Server: " + name);
                                    }
                                }
                                lobbyVM.getShowCardSelection().set(true);
                            });
                        }
                    }

                    case "NotYourCards", "NotYourCardsMessage" -> {
                        NotYourCardsBody nyc = mapper.treeToValue(body, NotYourCardsBody.class);
                        int clientID = nyc.getClientID();
                        int cardsCount = nyc.getCardsInHand();
                        viewModel.showSystemPrompt("Spieler " + clientID + " hat " + cardsCount + " Karten auf der Hand.");
                    }

                    case "ShuffleCoding" -> {}  // wird nicht benötig

                    case "CardSelected" -> {
                        int clientId = body.get("clientID").asInt();
                        int register = body.get("register").asInt();
                        boolean filled = body.get("filled").asBoolean();
                        viewModel.showSystemPrompt("Spieler " + clientId +
                                (filled ? " hat Karte in Register " : " hat Karte aus Register ") + register +
                                (filled ? " gelegt." : " entfernt."));
                    }

                    case "TimerEnded" -> {
                        TimerEndedBody bodyObj = mapper.treeToValue(body, TimerEndedBody.class);
                        List<Integer> finishedClients = bodyObj.getClientIDs();
                        viewModel.showSystemPrompt("Timer ended for clients: " + finishedClients);
                        //viewModel.handleTimerEnded(finishedClients);
                    }

                    case "TimerStarted" -> {
                        // leerer Body, nur Event auslösen
                        viewModel.showSystemPrompt("Timer wurde gestartet.");
                        if (viewModel instanceof LobbyViewModel vm) {
                            vm.startLocalTimer(30); // Dauer in Sekunden
                        }
                        // viewModel.handleTimerStarted();
                    }

                    case "CardsYouGotNow" -> {
                        ArrayNode cardsNode = (ArrayNode) body.get("cards");
                        List<String> cards = new ArrayList<>();
                        int clientId = body.get("clientID").asInt();
                        int energyCount = body.get("count").asInt();
                        String source = body.get("source").asText();

                        viewModel.showSystemPrompt("Spieler " + clientId + " hat jetzt " + energyCount + " Energie (Quelle: " + source + ")");

                        if (viewModel instanceof LobbyViewModel lobbyVM) {
                            if (clientId == lobbyVM.getClientID()) {
                                lobbyVM.setEnergy(energyCount);
                            }
                        }
                    }

                    case "CurrentCards" -> {
                        CurrentCardsBody currentCardsBody = mapper.treeToValue(body, CurrentCardsBody.class);
                        List<CurrentCardsBody.ActiveCard> activeCards = currentCardsBody.getActiveCards();

                        StringBuilder sb = new StringBuilder("Aktive Karten in dieser Runde:\n");
                        for (CurrentCardsBody.ActiveCard card : activeCards) {
                            sb.append("Spieler ").append(card.getClientID()).append(": ").append(card.getCard()).append("\n");
                        }
                        viewModel.showSystemPrompt(sb.toString());

                        // EIGENE Karte spielen
                        if (viewModel instanceof LobbyViewModel lobbyVM) {
                            for (CurrentCardsBody.ActiveCard card : activeCards) {
                                if (card.getClientID() == viewModel.getClientID()) {
                                    lobbyVM.sendPlayCard(card.getCard());
                                    viewModel.showSystemPrompt("Ich spiele automatisch: " + card.getCard());
                                }
                            }
                        }
                    }

                    case "CurrentPlayer" -> {
                        CurrentPlayerBody cp = mapper.treeToValue(body, CurrentPlayerBody.class);
                        if (viewModel instanceof LobbyViewModel vm) {
                            vm.onCurrentPlayer(cp.getClientID());
                        }
                        viewModel.showSystemPrompt("Jetzt ist Spieler " + cp.getClientID() + " am Zug.");
                    }

                    case "Energy" -> {
                        int clientId = body.get("clientID").asInt();
                        int energyCount = body.get("count").asInt();
                        String source = body.get("source").asText();
                        viewModel.showSystemPrompt("Spieler " + clientId + " hat jetzt " + energyCount + " Energie (Quelle: " + source + ")");

                        if (viewModel instanceof LobbyViewModel lobbyVM) {
                            int newEnergy = lobbyVM.getEnergy() + energyCount; // oder einfach = amount;
                            lobbyVM.setEnergy(newEnergy);
                        }
                    }

                    case "ReplaceCard" -> {
                        ReplaceCardBody replaceCardBody = mapper.treeToValue(body, ReplaceCardBody.class);

                        int clientID = replaceCardBody.getClientID();
                        int register = replaceCardBody.getRegister();
                        String newCard = replaceCardBody.getNewCard();

                        // Beispiel: Update im ViewModel
                        // viewModel.replaceCardForPlayer(clientID, register, newCard);

                        /// Neu (nur INFO hinzugefügt)
                        viewModel.showSystemPrompt("[INFO] Spieler " + clientID + " hat Karte im Register " + register + " ersetzt durch " + newCard);
                    }

                    case "Movement" -> {
                        if (!body.has("clientID") || !body.has("x") || !body.has("y")) {
                            logger.severe("[ERROR] Ungültige Movement-Nachricht: " + body);

                            return;
                        }
                        int movedClientID = body.get("clientID").asInt();
                        int x = body.get("x").asInt();
                        int y = body.get("y").asInt();

                        viewModel.showSystemPrompt("Spieler " + movedClientID + " bewegt sich zu (" + x + ", " + y + ")");
                    }

                    case "PlayerTurning" -> {
                        if (!body.has("clientID") || !body.has("rotation")) {
                            logger.severe("[ERROR] Ungültige PlayerTurning-Nachricht: " + body);
                            return;
                        }
                        int turningClientID = body.get("clientID").asInt();
                        String rotation = body.get("rotation").asText();

                        String directionStr = switch (rotation) {
                            case "clockwise" -> "im Uhrzeigersinn";
                            case "counterclockwise" -> "gegen den Uhrzeigersinn";
                            case "uturn" -> "um 180°";
                            default -> "unbekannt";
                        };

                        viewModel.showSystemPrompt("Spieler " + turningClientID + " dreht sich " + directionStr);
                        viewModel.rotateRobotOnMap(turningClientID, rotation);  // ← NEU!
                    }

                    case "ConnectionUpdate" -> {
                        ConnectionUpdateBody bodyObj = mapper.treeToValue(body, ConnectionUpdateBody.class);

                        int clientID = bodyObj.getClientID();
                        boolean isConnected = bodyObj.isConnected();
                        String action = bodyObj.getAction();

                        /*
                        String status = isConnected ? "wieder verbunden" : "verloren";
                        viewModel.showSystemPrompt("Verbindung von Spieler " + clientID + " wurde " + status + " – Aktion: " + action);

                         */

                        if(action.equals("Removed")){
                            viewModel.showSystemPrompt("Spieler " + clientID + " wurde aus dem Spiel entfernt.");
                        }
                    }

                    case "SelectionFinished" -> {
                        SelectionFinishedBody sf = mapper.treeToValue(body, SelectionFinishedBody.class);
                        viewModel.showSystemPrompt("Spieler " + sf.getClientID() + " hat seine Kartenauswahl abgeschlossen.");
                    }

                    case "SetStartingPoint" -> {
                        SetStartingPointBody ssp = mapper.treeToValue(body, SetStartingPointBody.class);
                        viewModel.showSystemPrompt("Startpunkt gewählt bei (" + ssp.getX() + "," + ssp.getY() + ")");
                    }

                    //case "TimerEnded" -> {
                    //TimerEndedBody te = mapper.treeToValue(body, TimerEndedBody.class);
                    //viewModel.showSystemPrompt("Timer abgelaufen für: " + te.getClientIDs());
                    //}

                    case "RobotPosition" -> {
                        RobotPositionBody rp = mapper.treeToValue(body, RobotPositionBody.class);

                        int clientID = rp.getClientID();
                        int x = rp.getX();
                        int y = rp.getY();
                        String dir = rp.getDirection();

                        viewModel.showSystemPrompt("Roboter " + clientID + " Position: (" + x + "," + y + ") Richtung: " + dir);

                        if (viewModel instanceof LobbyViewModel lobbyVM) {
                            lobbyVM.updateRobotPosition(clientID, x, y, dir);
                        }
                    }

                    case "UsedRobots" -> {
                        UsedRobotsBody bodyObj = mapper.treeToValue(body, UsedRobotsBody.class);
                        if (viewModel instanceof LoginViewModel loginVM) {
                            loginVM.updateUsedRobots(bodyObj.getUsedRobots());
                        }
                    }



                    // TODO cases

                    case "Animation" -> {}

                    case "HelloServer" -> {}    // wird nicht benötigt

                    case "Reboot" -> {}

                    case "RebootDirection" -> {}

                    /// Logik hinzugefügt
                    case "CheckPointReached" -> {
                        CheckPointReachedBody checkpointBody = mapper.treeToValue(body, CheckPointReachedBody.class);
                        int clientID = body.get("clientID").asInt();
                        int number = body.get("number").asInt();

                        viewModel.showSystemPrompt("Spieler " + clientID + " hat Checkpoint " + number + " erreicht, ");
                    }

                    /// Logik hinzugefügt
                    case "GameFinished" -> {
                        GameFinishedBody bodyObj = mapper.treeToValue(body, GameFinishedBody.class);
                        int clientID = body.get("clientID").asInt();

                        viewModel.showSystemPrompt("Spieler " + clientID + " hat das Spiel gewonnen!");
                    }

                    /// Neu hinzugefügt (SelectedDamage)
                    case "SelectedDamage" -> {
                        SelectedDamageBody selectedDamage = mapper.treeToValue(body, SelectedDamageBody.class);
                        List<String> cards = selectedDamage.getCards();

                        viewModel.showSystemPrompt("Du erhältst folgende Spielkarten: " + String.join(", ", cards));
                    }

                    default -> {
                        viewModel.showSystemPrompt("Unhandled message type: " + type);
                    }
                }
            } catch (Exception e) {
                viewModel.showSystemPrompt("Fehler beim Verarbeiten der Nachricht: " + e.getMessage());
            }
        });
    }
}
