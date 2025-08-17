package de.lmu.cleverecousins;
/**
 * This class implements the communications by using multithreaded method in java to deal with multiple player,
 * thread of each player is responsible for receiving, sending messeages and broadcasting
 */

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.lmu.Board.Board;
import de.lmu.cleverecousins.protocol.MapBuilder;
import de.lmu.cleverecousins.protocol.cheats.CheatMoveMessage;
import de.lmu.cleverecousins.protocol.cheats.CheatTurnMessage;
import de.lmu.cleverecousins.protocol.message.*;
import de.lmu.cleverecousins.protocol.messageBody.*;
import de.lmu.util.LogConfigurator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The {@code ClientManager} class handles communication with a single client in a multiplayer game server.
 * <p>
 * Each instance runs in its own thread and is responsible for receiving and sending JSON messages,
 * processing protocol-specific commands, maintaining the connection, and updating the game state accordingly.
 * </p>
 *
 * <p>Main responsibilities include:</p>
 * <ul>
 *     <li>Listening for and parsing incoming JSON messages from the client</li>
 *     <li>Sending individual or broadcast messages to clients</li>
 *     <li>Managing player metadata such as name, ID, figure, and readiness</li>
 *     <li>Dispatching message types to the {@link GamePhaseController}</li>
 *     <li>Handling connection loss and removing disconnected clients cleanly</li>
 * </ul>
 *
 * <p>This class uses the Jackson library for JSON parsing and Java's logging framework for debugging and monitoring.</p>
 *
 * @author Gabriel
 * @version 1.0
 */
public class ClientManager extends Thread {

    /**
     * Logger used for debugging and tracking important events during game phases.
     * Configured to log detailed information (Level.FINE and above).
     */
    private static final Logger logger = Logger.getLogger(ClientManager.class.getName());

    // Configure the root logger to show fine-grained debug output
    static{
        LogConfigurator.configureRootLogger(Level.FINE);
    }

    // --- Network and Communication ---

    /**
     * The socket connection between this server thread and the client.
     */
    private Socket socket;

    /**
     * Input stream to receive messages from the client.
     */
    private BufferedReader in;

    /**
     * Output stream to send messages to the client.
     */
    private PrintWriter out;

    /**
     * A static set containing all connected client threads.
     * Used for broadcasting and managing shared client state.
     */
    private static Set<ClientManager> clients;

    // --- Client Metadata ---

    /**
     * The display name of the player associated with this client.
     */
    private String name; // this is the name of the player

    /**
     * The figure ID selected by the player to represent their robot.
     */
    private int figure;

    /**
     * Whether this client is controlled by an AI or a real player.
     */
    private boolean ifUsingAI;

    /**
     * The name of the group this player belongs to (used in multiplayer sessions).
     */
    private String groupName;

    /**
     * A unique identifier assigned to the client upon connection.
     */
    private int clientID;

    /**
     * Indicates whether the player has marked themselves as ready.
     */
    private boolean ready;

    /**
     * Timestamp of the last 'alive' signal received from the client.
     * Used to detect connection timeouts.
     */
    private long lastAliveTimestamp = System.currentTimeMillis();

    // --- Shared Session State ---

    /**
     * Tracks whether the map selection phase has started.
     * Only the first ready player will be allowed to choose the map.
     */
    private static boolean isMapSelectionPhase = false;

    /**
     * Holds a reference to the first player who marked themselves as ready.
     * This player will be prompted to select the game map.
     */
    private static ClientManager firstReadyPlayer = null;

    // --- JSON Utilities ---

    /**
     * Jackson ObjectMapper used for parsing and serializing JSON messages.
     */
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // --- Game Logic References ---

    /**
     * The list representing the player turn order. Filled during login.
     */
    private final List<Integer> playerOrder = new ArrayList<>();

    /**
     * Reference to the shared {@link Game} instance representing the full game state.
     */
    private final Game game;

    /**
     * Controller responsible for managing all game phases and logic.
     */
    private final GamePhaseController phaseController;

    /**
     * Creates a new {@code ClientManager} instance for managing communication with a single client.
     * This constructor initializes the socket streams for input and output, assigns the shared game instance,
     * and sets up the reference to the {@link GamePhaseController}.
     *
     * @param socket           the client socket used for communication
     * @param clients          the shared set of all connected {@code ClientManager} instances
     * @param phaseController  the controller responsible for managing game phases
     * @throws IOException if an I/O error occurs while creating the input or output streams
     */
    public ClientManager(Socket socket, Set<ClientManager> clients, GamePhaseController phaseController) throws IOException {
        this.socket = socket;
        this.clients = clients;
        this.game = Server.sharedGame; // alle greifen auf die gleiche Game-Instanz zu!
        this.phaseController = phaseController;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        this.lastAliveTimestamp = System.currentTimeMillis(); // neu
    }

    /**
     * Returns the name of the player associated with this client.
     *
     * @return the player's display name.
     */
    public String getPlayerName() {
        return this.name;
    }

    /**
     * Checks whether the connection to the client is closed.
     *
     * @return {@code true} if the socket is {@code null} or already closed; {@code false} otherwise.
     */
    public boolean isClosed() {
        return socket == null || socket.isClosed();
    }

    /**
     * Sends a JSON-formatted message to the connected client.
     *
     * @param json the message to send
     * @throws IOException if an I/O error occurs when sending the message
     */
    public void sendMessageToClient(String json) throws IOException {
        out.println(json);
    }

    /**
     * Receives a message from the client.
     *
     * @return the received message as a String
     * @throws IOException if an I/O error occurs while reading
     */
    public String receiveMessageFromClient() throws IOException {
        String msg = null;
        msg = in.readLine();
        return msg;
    }

    /**
     * Returns whether the player has marked themselves as ready.
     *
     * @return {@code true} if ready, otherwise {@code false}
     */
    public boolean getReady() {
        return this.ready;
    }

    /**
     * Returns the unique client ID assigned by the server.
     *
     * @return the client ID
     */
    public int getClientID() {
        return this.clientID;
    }

    /**
     * Continuously listens for incoming JSON messages from the connected client and dispatches them for processing.
     *
     * <p>This method is the main loop of the client thread. It:
     * <ul>
     *   <li>Sends an initial {@code HelloClient} message to confirm the protocol version.</li>
     *   <li>Continuously reads lines from the input stream and passes each message to the {@code messageDispatcher()} method.</li>
     *   <li>Handles exceptions gracefully and performs cleanup when the client disconnects or an error occurs.</li>
     * </ul>
     *
     * <p>Note: This method is automatically invoked when the thread is started.</p>
     */
    @Override
    public void run() {
        // notify the player to do the log in
        /** HelloClient: Give the protocol version when client first succesfully connected to server*/
        try {
            String helloClient = NetworkManager.serialize(
                    new HelloClientMessage(new HelloClientBody("Version 1.0"))
            );
            sendMessageToClient(helloClient);

            String line;
            while ((line = in.readLine()) != null) {
                if (line.isBlank()) continue;

                try {
                    messageDispatcher(line);
                } catch (IOException ex) {
                    logger.log(Level.WARNING, "[ERROR] Fehler im messageDispatcher", ex);
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, "[ERROR] Unerwarteter Fehler im Dispatcher", ex);
                }
            }

            logger.info("[INFO] Client hat die Verbindung geschlossen: " + getPlayerName());
            disconnectCleanup();

        } catch (IOException e) {
            logger.log(Level.SEVERE, "[ERROR] Verbindung zum Client verloren. ", e);
            disconnectCleanup();
        }

        //listening to the Client
        try {
            String line;
            while ((line = in.readLine()) != null) {
                try {
                    messageDispatcher(line);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException e) {
            // Falls Client Verbindung verliert
            logger.log(Level.WARNING, "[WARN] Verbindung zu Client " + clientID + " verloren!", e);
            handleDisconnect();
        }
    }

    /**
     * Dispatches a received JSON message to the corresponding handler based on its {@code messageType}.
     *
     * <p>This method parses the {@code messageType} from the JSON and uses a {@code switch-case}
     * structure to call the appropriate handler method (e.g. {@code handleHelloServer()}, {@code handlePlayerValues()}, etc.).
     *
     * <p>If the message type is unknown or missing, a warning is logged and no further action is taken.
     *
     * @param json the full JSON message received from the client
     * @throws Exception if a handler method throws an exception
     */
    public void messageDispatcher(String json) throws Exception {
        //TODO: a switch-case structure to dispatch different type of messages

        String type = NetworkManager.getMessageType(json);
        if (type == null) {
            logger.warning("Fehlendes messageType-Feld in JSON");
            return;
        }

        switch (type) {
            //TODO: fill all necessary JSON types, and writing their individual handle method
            case "HelloServer":
                handleHelloServer(json);
                break;

            case "PlayerValues":
                handlePlayerValues(json);
                break;

            case "SendChat":
                handleSendChat(json);
                break;

            case "SetStatus":
                handleSetStatus(json);
                break;

            // Newly added cases

            case "ActivePhase":
                handleActivePhase(json);
                break;


            case "SelectMap":
                handleSelectMap(json);
                break;

            case "MapSelected":
                handleMapSelected(this, json);
                break;

            // TODO Wie unten in der handleMethode "GameStarted" wird in SelectMap Logic eingebaut
            case "GameStarted":
                //handleGameStarted(json);
                break;

            case "SetStartingPoint":
                handleSetStartingPoint(json);
                break;


            case "SelectedCard":
                Server.phaseController.selectCard(clientID, json);
                break;

            case "PlayCard":
                Server.phaseController.playCard(clientID, json);
                break;

            case "SelectionFinished":
                handleSelectionFinished(json);
                break;

            case "Alive":
                handleAlive(json);
                break;

            case "Animation":
                handleAnimation(json);
                break;

            case "Energy":              // wird bald auch nicht mehr als gebraucht
                handleEnergy(json);
                break;

            case "Reboot":
                handleReboot(json);
                break;

            case "RebootDirection":     // wird bald auch nicht mehr als gebraucht
                handleRebootDirection(json);
                break;

            case "CheckPointReached":
                handleCheckPointReached(json);
                break;

            case "GameFinished":
                handleGameFinished(json);
                break;

            ///Neu hinzugefügt (PickDamage)
            case "PickDamage":
                //handlePickDamage(json);
                Server.phaseController.handlePickDamage(clientID, json);
                break;

            /// Admin Cheats
            case "CheatMove":
                handleCheatMove(objectMapper.treeToValue(objectMapper.readTree(json), CheatMoveMessage.class));
                break;

            case "CheatTurn":
                handleCheatTurn(objectMapper.treeToValue(objectMapper.readTree(json), CheatTurnMessage.class));
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }
    }

    /**
     * Handles the initial handshake from the client after connection.
     * Parses group name and AI status from the client,
     * assigns a unique client ID, and sends a welcome message back.
     *
     * @param json the HelloServer JSON message sent by the client
     * @throws IOException if parsing or communication fails
     */
    public void handleHelloServer(String json) throws IOException {
        logger.fine("[DEBUG] Full message: " + json);
        logger.fine("[DEBUG] messageBody: " + objectMapper.readTree(json).get("messageBody").toPrettyString());

        //save groupName, save ifUsingAI to the ClientManager
        JsonNode root = objectMapper.readTree(json); //解析整个json对象
        JsonNode body = root.get("messageBody");

        //setting the client information: group name and ifUsingAI
        this.groupName = body.get("group").asText();
        this.ifUsingAI = body.get("isAI").asBoolean();

        // server give a clientID to this client
        clientID = Server.generateUniqueClientID();
        game.addToPlayerOrder(clientID);

        //server then send the welcome message to the client
        String welcome = null;
        welcome = NetworkManager.serialize(new WelcomeMessage(new WelcomeBody(clientID)));
        sendMessageToClient(welcome);
        logger.fine("[DEBUG] Sending welcome JSON: " + welcome);
        logger.fine("[DEBUG] Spielerreihenfolge: " + game.getPlayerOrder());
    }

    /**
     * Handles player customization input, such as name and selected figure.
     * Broadcasts the new player to all other clients and sends the current player list to the new client.
     *
     * @param json the PlayerValues JSON message containing name and figure
     * @throws IOException if JSON parsing or broadcasting fails
     */
    public void handlePlayerValues(String json) throws IOException {
        logger.fine("[DEBUG] Full message: " + json);
        logger.fine("[DEBUG] messageBody " + objectMapper.readTree(json).get("messageBody").toPrettyString());

        JsonNode root = objectMapper.readTree(json);
        JsonNode body = root.get("messageBody");

        String newName = body.get("name").asText();
        int newFigure = body.get("figure").asInt();

        // Prüfen, ob Roboter bereits vergeben ist
        boolean figureTaken;
        synchronized (clients) {
            figureTaken = clients.stream()
                    .anyMatch(c -> c != this && c.figure == newFigure);
        }

        if (figureTaken) {
            logger.warning("[WARN] Roboter " + newFigure + " ist bereits vergeben. ");
            // Fehlermeldung an Client senden (optional)
            return;
        }


        // 1. Neue Werte setzen
        this.name = body.get("name").asText();
        this.figure = body.get("figure").asInt();

        // 2. Vorhandene Spieler an den neuen Client senden
        synchronized (clients) {
            for (ClientManager existing : clients) {
                if (existing != this && existing.name != null) {
                    PlayerAddedBody existingBody = new PlayerAddedBody(existing.clientID, existing.name, existing.figure);
                    PlayerAddedMessage existingMessage = new PlayerAddedMessage(existingBody);
                    String existingJson = NetworkManager.serialize(existingMessage);
                    this.sendMessageToClient(existingJson);
                }
            }

            // 3. Jetzt den neuen Spieler an alle senden
            PlayerAddedBody newBody = new PlayerAddedBody(this.clientID, this.name, this.figure);
            PlayerAddedMessage newMessage = new PlayerAddedMessage(newBody);
            String newJson = NetworkManager.serialize(newMessage);

            for (ClientManager c : clients) {
                c.sendMessageToClient(newJson);
            }
            logger.fine("[DEBUG] Broadcasting new PlayerAdded JSON: " + newJson);

            // Hier jetzt **zusätzlich** UsedRobots nur an diesen neuen Client senden
            List<Integer> currentlyUsedFigures = clients.stream()
                    .filter(c -> c.figure != 0)  // oder nach Default prüfen
                    .map(c -> c.figure)
                    .toList();

            UsedRobotsMessage usedRobotsMessage = new UsedRobotsMessage(new UsedRobotsBody(currentlyUsedFigures));
            String usedRobotsJson = NetworkManager.serialize(usedRobotsMessage);
            this.sendMessageToClient(usedRobotsJson);

            // Und dann die Liste an alle broadcasten (optional, falls nötig)
            broadcastUsedRobots();
        }
    }

    /**
     * Broadcasts the list of currently used robot figures to all connected clients.
     *
     * @throws IOException if JSON serialization or message sending fails
     */
    private void broadcastUsedRobots() throws IOException {
        List<Integer> takenRobots;
        synchronized (clients) {
            takenRobots = clients.stream()
                    .map(c -> c.figure)
                    .filter(f -> f > 0)
                    .toList();
        }

        UsedRobotsMessage usedMsg = new UsedRobotsMessage(new UsedRobotsBody(takenRobots));
        String usedJson = NetworkManager.serialize(usedMsg);

        synchronized (clients) {
            for (ClientManager c : clients) {
                c.sendMessageToClient(usedJson);
            }
        }
    }

    /**
     * Handles the final map selection from a client and loads the chosen map.
     * Sets up the board and starts the game setup phase.
     *
     * @param sender the client who selected the map
     * @param json the MapSelected JSON message
     * @throws IOException if map loading or broadcasting fails
     */
    private void handleMapSelected(ClientManager sender, String json) throws IOException {
        logger.fine("[DEBUG] Full message: " + json);
        JsonNode bodyNode = objectMapper.readTree(json).get("messageBody");
        logger.fine("[DEBUG] messageBody: " + bodyNode.toPrettyString());

        String mapName = bodyNode.get("mapName").asText();
        String playerName = sender.getPlayerName();
        logger.fine("[DEBUG] Player who selected map: " + playerName);

        // MapSelectedMessage an alle senden
        MapSelectedBody body = new MapSelectedBody(mapName, playerName);
        MapSelectedMessage msMsg = new MapSelectedMessage(body);
        String msJson = NetworkManager.serialize(msMsg);
        for (ClientManager cm : clients) {
            cm.sendMessageToClient(msJson);
        }
        logger.fine("[DEBUG] Broadcasted MapSelectedMessage: " + msJson);

        // Map laden
        GameStartedBody mapBody = MapLoader.loadMap("/map-" + mapName.toLowerCase().replace(" ", "-") + ".json");
        Board board = MapBuilder.buildBoard(mapBody);

        // alle Clients als Spieler hinzufügen
        for (ClientManager cm : clients) {
            int id = cm.getClientID();
            String pname = cm.getPlayerName();
            Position defaultPosition = new Position(0,0);
            Robot robot = new Robot(defaultPosition, Direction.TOP);

            Player player = new Player(id, robot, defaultPosition);
            Server.sharedGame.addPlayer(player);
            logger.fine("[DEBUG] Player hinzugefügt: " + pname + " (ID: " + id + ")");
        }

        // GamePhaseController starten
        Server.phaseController = new GamePhaseController(
                Server.sharedGame,
                board,
                Server.cardExecutor,
                Server.timerService,
                Server.clients
        );
        Server.phaseController.startSetupPhase();
        logger.fine("[DEBUG] Server: Setup-Phase gestartet");

        // GameStartedMessage an alle senden
        GameStartedMessage gsMsg = new GameStartedMessage(mapBody);
        String gsJson = NetworkManager.serialize(gsMsg);
        for (ClientManager cm : clients) {
            cm.sendMessageToClient(gsJson);
        }
        logger.fine("[DEBUG] Broadcasted GamestartedMessage: " + gsJson);
    }

    /**
     * Handles the current game phase notification from a client
     * and broadcasts the updated phase to all other clients.
     *
     * @param json the ActivePhase JSON message
     * @throws IOException if broadcasting fails
     */
    private void handleActivePhase(String json) throws IOException {
        logger.fine("[DEBUG] Full message: " + json);
        logger.fine("[DEBUG] messageBody: " + objectMapper.readTree(json).get("messageBody").toPrettyString());
        JsonNode root = objectMapper.readTree(json);
        JsonNode body = root.get("messageBody");
        int phase = body.get("phase").asInt();
        // Phase im Spiel speichern
        // Server.getGame().setCurrentPhase(phase);
        // Broadcast der aktiven Phase an alle Clients
        String phaseUpdate = NetworkManager.serialize(new ActivePhaseMessage(new ActivePhaseBody(phase)));
        for (ClientManager c : clients) {
            c.sendMessageToClient(phaseUpdate);
        }
        logger.fine("[DEBUG] Broadcasted active phase: " + phase + " to all Clients. ");
    }

    /**
     * Handles chat messages sent by a client. Supports both broadcast and private messages.
     *
     * @param json the SendChat JSON message
     * @throws IOException if parsing or message delivery fails
     */
    public void handleSendChat(String json) throws IOException {
        JsonNode root = objectMapper.readTree(json);
        JsonNode body = root.get("messageBody");

        String message = body.get("message").asText();
        int target = body.get("to").asInt(); // -1 = broadcast

        if (target == -1) {
            // 📢 Broadcast – an alle außer dem Sender
            String broadcast = NetworkManager.serialize(
                    new ReceivedChatMessage(new ReceivedChatBody(clientID, this.name, message, false))
            );

            for (ClientManager c : clients) {
                if (c.clientID != this.clientID) {
                    c.sendMessageToClient(broadcast);
                }
            }

            logger.fine("[DEBUG] Broadcasted to all except sender: " + broadcast);
        } else {
            // 🔐 Private Nachricht
            // Nachricht an den Empfänger
            ReceivedChatMessage toRecipient = new ReceivedChatMessage(
                    new ReceivedChatBody(clientID, this.name, message, true)
            );
            String toRecipientJson = NetworkManager.serialize(toRecipient);

            // Nachricht an den Sender selbst
            String recipientName = "Spieler " + target; // falls Name nicht bekannt
            for (ClientManager c : clients) {
                if (c.clientID == target) {
                    recipientName = c.name;
                }
            }

            ReceivedChatMessage toSender = new ReceivedChatMessage(
                    new ReceivedChatBody(clientID, "Du an " + recipientName, message, true)
            );
            String toSenderJson = NetworkManager.serialize(toSender);

            boolean found = false;
            for (ClientManager c : clients) {
                if (c.clientID == target) {
                    c.sendMessageToClient(toRecipientJson);
                    found = true;
                } else if (c.clientID == this.clientID) {
                    c.sendMessageToClient(toSenderJson);
                }
            }

            if (found) {
                logger.fine("[DEBUG] Send private message to " + target + " and sender");
            } else {
                logger.warning("[WARN] Private recipient not found: " + target);
            }
        }
    }

    /**
     * Handles the player's readiness status update.
     * If all clients are ready and the map selection phase hasn't started yet,
     * triggers the map selection process.
     *
     * @param json the SetStatus JSON message containing the ready flag
     * @throws IOException if broadcasting or parsing fails
     */
    public void handleSetStatus(String json) throws IOException {
        // 1) Parse Incoming JSON
        JsonNode root = objectMapper.readTree(json);
        boolean newReadyStatus = root.path("messageBody").path("ready").asBoolean();

        // 2) Setze den neuen Ready-Status und update Server-Liste
        this.ready = newReadyStatus;
        Server.changeReadyList(this);

        // 3) Merke den allerersten Ready-Spieler
        if (newReadyStatus && firstReadyPlayer == null) {
            firstReadyPlayer = this;
        }

        // 4) Broadcast des neuen PlayerStatus an alle
        String statusJson = NetworkManager.serialize(
                new PlayerStatusMessage(new PlayerStatusBody(clientID, ready))
        );
        for (ClientManager cm : clients) {
            cm.sendMessageToClient(statusJson);
        }

        // 5) Sobald **alle** Clients ready sind und noch nicht in Map-Selection-Phase…
        if (!isMapSelectionPhase && Server.getReadyPlayers().size() == clients.size()) {
            // a) System-Nachricht: Auswahlphase startet
            SystemMessage sysMsg = new SystemMessage(
                    "Spieler " + firstReadyPlayer.getPlayerName() + " wählt jetzt die Map aus."
            );
            String sysJson = NetworkManager.serialize(sysMsg);
            for (ClientManager cm : clients) {
                cm.sendMessageToClient(sysJson);
            }

            // b) Liste der verfügbaren Maps
            List<String> maps = List.of(
                    "Dizzy Highway",
                    "Lost Bearings",
                    "Extra Crispy",
                    "Death Trap"
            );

            // c) SelectMapMessage mit erlaubtem Chooser
            SelectMapMessage selMsg = new SelectMapMessage(
                    new SelectMapBody(maps, firstReadyPlayer.getClientID())
            );
            String selJson = NetworkManager.serialize(selMsg);

            // d) Broadcast SelectMapMessage an alle
            for (ClientManager cm : clients) {
                cm.sendMessageToClient(selJson);
            }

            isMapSelectionPhase = true;
            logger.fine("[DEBUG] Broadcasting SelectMapMessage: " + selJson);
        }
    }

    /**
     * Handles the map selection command from the first ready player.
     * Loads the map and starts the setup phase.
     *
     * @param json the SelectMap JSON message with the chosen map name
     * @throws IOException if the map loading or broadcasting fails
     */
    private void handleSelectMap(String json) throws IOException {
        logger.fine("[DEBUG] Full message: " + json);
        logger.fine("[DEBUG] messageBody: " + objectMapper.readTree(json).get("messageBody").toPrettyString());

        JsonNode root = objectMapper.readTree(json);
        JsonNode body = root.get("messageBody");

        String mapName = body.get("mapName").asText();

        try {
            // 加载 .json 地图
            GameStartedBody mapBody = MapLoader.loadMap("/map-" + mapName.toLowerCase().replace(" ", "-") + ".json");

            Board board = MapBuilder.buildBoard(mapBody);
            Server.phaseController = new GamePhaseController(Server.sharedGame, board, Server.cardExecutor, Server.timerService, Server.clients);
            Server.phaseController.startSetupPhase();

            // 构造 GameStarted 消息
            GameStartedMessage msg = new GameStartedMessage(mapBody);
            String jsonMessage = NetworkManager.serialize(msg);

            // 广播给所有客户端
            for (ClientManager cm : clients) {
                cm.sendMessageToClient(jsonMessage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the player's starting point selection during the setup phase.
     * Forwards the request to the GamePhaseController and transitions to the next phase when all players have selected.
     *
     * @param json the SetStartingPoint JSON message
     * @throws IOException if broadcasting or setup fails
     */
    private void handleSetStartingPoint(String json) throws IOException {
        // 1) Debug-Ausgabe
        logger.fine("[DEBUG] handleSetStartingPoint aufgerufen mit: " + json);


        // 2) An den GamePhaseController weiterreichen
        //    (broadcastet intern StartingPointTakenMessage und CurrentPlayerMessage)
        Server.phaseController.setStartingPoint(clientID, json);

        // 3) Wenn wirklich alle Spieler ihren Punkt gewählt haben…
        if (Server.phaseController.allPlayersChoseStart()) {
            logger.fine("[DEBUG] Alle Startpunkte gesetzt, sende ActivePhase=1");
            ActivePhaseMessage apMsg = new ActivePhaseMessage(new ActivePhaseBody(1));
            String apJson = NetworkManager.serialize(apMsg);

            // 4) Broadcast ActivePhase=1 an alle Clients
            for (ClientManager cm : clients) {
                cm.sendMessageToClient(apJson);
            }
            logger.fine("[DEBUG] Broadcasted ActivePhase 1: " + apJson);
        }
    }

    /**
     * Called when a player finishes selecting cards during the programming phase.
     * Notifies the GamePhaseController and optionally broadcasts the info to others.
     *
     * @param json the SelectionFinished JSON message
     * @throws IOException if broadcasting fails
     */
    private void handleSelectionFinished(String json) throws IOException {
        JsonNode root = objectMapper.readTree(json);
        JsonNode body = root.get("messageBody");
        int finishedClientId = body.get("clientID").asInt();
        logger.fine("[DEBUG] Spieler " + finishedClientId + " hat seine Kartenauswahl abgeschlossen.");

        // hier kannst du serverseitig reagieren, z.B. Timer starten:
        Server.phaseController.selectionFinished(finishedClientId);

        // Optional an alle broadcasten:
        var msg = new SystemMessage("Spieler " + finishedClientId + " hat alle Karten programmiert.");
        String msgJson = NetworkManager.serialize(msg);
        for (ClientManager cm : clients) {
            cm.sendMessageToClient(msgJson);
        }
    }

    /**
     * Updates the timestamp of the last received keep-alive message from the client.
     * Helps detect disconnects.
     *
     * @param json the Alive JSON message (contents ignored)
     */
    private void handleAlive(String json) {
        this.lastAliveTimestamp = System.currentTimeMillis();
    }

    /**
     * Handles a client disconnect, cleaning up its data and informing other clients.
     */
    private void handleDisconnect() {
        logger.info("[INFO] Verbindung zu Client " + clientID + " verloren");

        Player player = game.getPlayer(clientID);
        if (player != null) {
            player.getRobot().setPosition(null); // oder auf null setzen / deaktivieren
            game.getPlayerOrder().remove((Integer) clientID);
        }

        ConnectionUpdateBody body = new ConnectionUpdateBody(clientID, false, "Remove");
        ConnectionUpdateMessage msg = new ConnectionUpdateMessage(body);

        try {
            String json = NetworkManager.serialize(msg);
            for (ClientManager cm : clients) {
                cm.sendMessageToClient(json);
            }
        } catch (IOException ex) {
            logger.severe("[ERROR] Fehler beim Senden von ConnectionUpdate: " + ex);
        }
    }

    /**
     * Placeholder for animation messages.
     * Currently unused – may be extended later for UI animations.
     *
     * @param json the Animation JSON message
     */
    private void handleAnimation(String json) throws IOException {}

    /**
     * Placeholder for card played messages.
     * Not processed by the server.
     *
     * @param json the PlayCard JSON message
     */
    private void handleCardPlayed(String json) throws IOException {
        // Keine Logik nötig – wird vom Server nie aktiv verarbeitet
        logger.fine("[DEBUG] handleCardPlayed aufgerufen, aber auf Serverseite nicht relevant.");
    }

    /**
     * Placeholder for energy collection messages.
     * This message type may be removed or replaced later.
     *
     * @param json the Energy JSON message
     */
    private void handleEnergy(String json) throws IOException {
    }

    /**
     * Placeholder for reboot messages.
     * Currently unused – reboot logic is handled in GamePhaseController.
     *
     * @param json the Reboot JSON message
     */
    private void handleReboot(String json) throws IOException {
    }

    /**
     * Placeholder for setting reboot direction.
     * May be removed once handled via GamePhaseController.
     *
     * @param json the RebootDirection JSON message
     */
    private void handleRebootDirection(String json) throws IOException {
    }

    /**
     * Placeholder for checkpoint reached handling.
     * To be handled properly in future game logic or animations.
     *
     * @param json the CheckPointReached JSON message
     */
    private void handleCheckPointReached(String json) throws IOException {
    }

    /**
     * Placeholder for game finished handling.
     * Currently unused.
     *
     * @param json the GameFinished JSON message
     */
    private void handleGameFinished(String json) throws IOException {
    }

    /**
     * Returns the timestamp of the last received Alive message from the client.
     * Used for connection health monitoring.
     *
     * @return last alive timestamp in milliseconds
     */
    public long getLastAliveTimestamp() {
        return lastAliveTimestamp;
    }

    /**
     * Gracefully disconnects the client by closing all input/output streams
     * and the socket. Also interrupts the thread.
     */
    public void disconnect() {
        try {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            logger.info("[INFO] Verbindung zu Client " + clientID + " wurde sauber getrennt.");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "[ERROR] Fehler beim Schließen der Verbindung", e);
        }
        this.interrupt();  // <--- das hier wichtig
    }

    /**
     * Performs cleanup operations after a disconnect:
     * removes client from server structures and notifies others.
     */
    public void disconnectCleanup() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "[ERROR] Fehler beim Socket schließen", e);
        }

        synchronized (Server.clients) {
            Server.clients.remove(this);
            Server.readyPlayers.remove(Integer.valueOf(this.clientID));
            Server.releaseClientID(this.clientID);
        }

        // alle anderen Spieler informieren
        Server.broadcastPlayerDisconnected(this.clientID, this.name != null ? this.name : "Unbekannt");

        logger.info("[INFO] Spieler " + getPlayerName() + " (" + clientID + ") wurde entfernt.");

    }

    /**
     * Checks whether the client is currently disconnected.
     *
     * @return true if the socket is null or closed
     */
    public boolean isDisconnected() {
        return socket == null || socket.isClosed();
    }

    ///  Admin Cheats

    /**
     * Handles a cheat command that moves the robot forward or backward
     * by the specified number of steps.
     *
     * @param msg the CheatMoveMessage containing the step count
     */
    public void handleCheatMove(CheatMoveMessage msg){
        int steps = msg.getBody().getSteps();
        logger.fine("[CHEAT] Moving robot " + steps + " steps forward.");

        Player player = game.getPlayer(this.clientID);
        if (player != null && player.getRobot() != null) {
            if (steps >= 0) {
                Server.phaseController.executeMoveForward(player.getRobot(), steps);
            } else {
                Server.phaseController.executeMoveBackward(player.getRobot(), -steps);
            }
        }
    }

    /**
     * Handles a cheat command that turns the robot in a specified direction.
     * Accepts: "left", "right", or "u" (for U-turn).
     *
     * @param msg the CheatTurnMessage containing the direction
     */
    public void handleCheatTurn(CheatTurnMessage msg){
        String direction = msg.getBody().getDirection();
        logger.fine("[CHEAT] Turning robot " + direction);

        Player player = game.getPlayer(this.clientID);
        if (player != null && player.getRobot() != null) {
            switch (direction) {
                case "left" -> player.getRobot().rotateCounterclockwise();
                case "right" -> player.getRobot().rotateClockwise();
                case "u" -> player.getRobot().uturn();
                default -> logger.warning("[CHEAT] Unknown turn direction: " + direction);
            }
        }
    }
}