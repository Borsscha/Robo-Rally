package de.lmu.cleverecousins;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.lmu.cleverecousins.protocol.message.*;
import de.lmu.cleverecousins.protocol.messageBody.*;
import de.lmu.util.LogConfigurator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A lightweight, stateful client for the RoboRally protocol ("Version 2.0").
 * <p>
 * {@code AIClient} connects to the game server via TCP socket, identifies itself,
 * keeps the connection alive by periodically sending <em>Alive</em> messages and reacts to
 * various incoming messages (e.g. game start, map selection, hand cards, etc.).
 * </p>
 *
 * <h3>Main responsibilities</h3>
 * <ul>
 *   <li>Establish and maintain the connection (Hello, PlayerValues, Alive timer)</li>
 *   <li>Process server messages via {@link #listenToServer()}</li>
 *   <li>Select a starting point and (rudimentary) card strategy</li>
 *   <li>Maintain internal state such as client ID, start points, checkpoints and current position</li>
 * </ul>
 *
 * <strong>Threading note:</strong> Server communication runs in a dedicated thread, the alive timer in another.
 * Methods that access shared collections should be synchronized if necessary.
 */
public class AIClient {

    /** Logger for the whole client. */
    private static final Logger logger = Logger.getLogger(AIClient.class.getName());

    static{
        // Global logger configuration
        LogConfigurator.configureRootLogger(Level.FINE);
    }

    // --- Configuration parameters ---
    /** Server address (hostname or IP). */
    private final String serverAddress;

    /** TCP port of the game server. */
    private final int port;

    /** Group name reported to the server. */
    private final String groupName;

    /** Player name reported to the server. */
    private final String playerName;

    /** Chosen robot ID. */
    private final int robotId;

    // --- Connection / IO components ---
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Timer aliveTimer;

    // --- Runtime state ---
    /** Client ID assigned by the server. */
    private int thisClientId;

    /** RNG for simple heuristics. */
    private final Random random = new Random();

    /** All start points that were found on the map. */
    private List<Position> startPoints = new ArrayList<>();

    /** All checkpoints. */
    private List<Position> checkpoints = new ArrayList<>();

    /** Remaining available start points (filtered as others take them). */
    private List<Position> availablePoints = null;

    /** Current position of our robot. */
    private Position currentPosition;

    /** Flag: has a start point already been chosen? */
    private boolean hasChosenStartpoint = false;

    /** Flag: have cards already been selected for this round? */
    private boolean hasSelectedCards = false;


    /**
     * Create a new {@code AIClient}.
     *
     * @param serverAddress address of the game server
     * @param port          port of the game server
     * @param groupName     group name for registration
     * @param playerName    displayed player name
     * @param robotId       ID of the robot to control
     */
    public AIClient(String serverAddress, int port, String groupName, String playerName, int robotId) {
        this.serverAddress = serverAddress;
        this.port = port;
        this.groupName = groupName;
        this.playerName = playerName;
        this.robotId = robotId;
    }

    /**
     * Opens the connection to the server and starts the listener and alive threads.
     * Should be called exactly once per client instance.
     */
    public void start() {
        try {
            socket = new Socket(serverAddress, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            sendHello();
            sendPlayerValues();
            startAliveSender();

            new Thread(this::listenToServer).start();
            logger.info("Verbindung erfolgreich aufgebaut. ");

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Verbindung zum Server fehlgeschlagen: " + e.getMessage(), e);
        }
    }

    /**
     * Sends the initial Hello message. The protocol requires this to be the first message.
     */
    private void sendHello() {
        String json = "{\"messageType\":\"HelloServer\",\"messageBody\":{" +
                "\"group\":\"" + groupName + "\"," +
                "\"isAI\":true," +
                "\"protocol\":\"Version 2.0\"" +
                "}}";
        out.println(json);
    }

    /**
     * Sends player specific values such as name and robot ID.
     */
    private void sendPlayerValues() {
        try {
            PlayerValuesBody body = new PlayerValuesBody(playerName, robotId);
            PlayerValuesMessage msg = new PlayerValuesMessage(body);
            String json = NetworkManager.serialize(msg);
            out.println(json);
            logger.info("PlayerValues gesendet.");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Fehler beim Senden von PlayerValues: " + e.getMessage(), e);
        }
    }

    /**
     * Starts a timer that sends an Alive message every 5 seconds to keep the connection alive.
     */
    private void startAliveSender() {
        aliveTimer = new Timer();
        aliveTimer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                try {
                    sendAliveMessage();
                } catch (IOException e) {
                    logger.log(Level.WARNING, "Alive konnte nicht gesendet werden: " + e.getMessage(), e);
                    aliveTimer.cancel();
                    closeConnection();
                }
            }
        }, 0, 5000); // alle 5 Sekunden
    }

    /**
     * Sends an Alive message. Triggered periodically by the alive timer.
     *
     * @throws IOException if IO errors occur
     */
    private void sendAliveMessage() throws IOException {
        String aliveJson = "{\"messageType\":\"Alive\",\"messageBody\":{}}";
        out.println(aliveJson);
        logger.fine("Alive gesendet. ");
    }

    /**
     * Sets our status to "ready" after joining the game. The message is delayed to avoid race conditions.
     *
     * @throws IOException not thrown directly because the code runs inside a TimerTask
     */
    private void sendStatus() throws IOException {

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    String json = "{\"messageType\":\"SetStatus\",\"messageBody\":{\"ready\":true}}";
                    out.println(json);
                    logger.fine("[KI] SetStatus auf ready gesendet. ");
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "[KI] Fehler beim Senden von SetStatus", e);
                }
            }
        }, 4000); // 4 seconds delay

        logger.info("SetStatus auf ready gesendet. ");
    }

    /**
     * Main listener loop. Reads messages from the server line by line, parses the message type
     * and dispatches to the respective handlers.
     */
    private void listenToServer() {
        try {
            String msg;
            while ((msg = in.readLine()) != null) {
                logger.fine("SERVER -> KI: " + msg);
                String type = NetworkManager.getMessageType(msg);
                logger.fine("SERVER -> KI RAW: " + msg);
                logger.fine("SERVER -> KI Type: " + type);
                switch (type) {

                    case "Welcome":
                        logger.info("Welcome erhalten, jetzt SetStatus senden. ");
                        JsonNode body = NetworkManager.getObjectMapper().readTree(msg).get("messageBody");
                        thisClientId = body.get("clientID").asInt();
                        sendStatus();
                        startAliveSender();
                        break;

                    case "GameStarted":
                        handleGameStarted(msg);
                        break;

                    case "ActivePhase":
                        handleActivePhase(msg);
                        break;

                    case "SelectMapMessage":
                        handleSelectMap(msg);
                        break;

                    case "CurrentPlayer":
                        handleCurrentPlayer(msg);
                        break;

                    case "YourCardsMessage":
                        handleYourCards(msg);
                        break;

                    case "CurrentCards":
                        handleCurrentCards(msg);
                        break;

                    case "StartingPointTaken":                      // New
                        handleStartingPointTaken(msg);

                    default:
                        logger.finer("Unbekannter Nachrichtentyp emfangen: " + type);
                        break;
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Fehler beim Empfangen vom Server: " + e.getMessage(), e);
            closeConnection();
        }
    }

    /**
     * Tries again to set a starting point after a delay. Handy if a point got taken meanwhile.
     *
     * @param delayMs milliseconds to wait before retrying
     */
    private void retrySetStartingPoint(int delayMs) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendSetStartingPointLogic();
            }
        }, delayMs);
    }

    /**
     * Triggered when another player has taken a starting point. Removes the point from our local list
     * of available start points.
     *
     * @param json original server message
     * @throws JsonProcessingException if JSON parsing fails
     */
    private void handleStartingPointTaken (String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);
        JsonNode body = root.get("messageBody");
        int x = body.get("x").asInt();
        int y = body.get("y").asInt();
        Position usedPosition = new Position(x,y);
        if (availablePoints == null) {
            availablePoints = new ArrayList<>(startPoints);
        }
        availablePoints.removeIf(p -> p.equals(usedPosition));
    }

    /**
     * Processes the map information when the game starts: collects start points and checkpoints.
     *
     * @param json GameStarted message
     */
    private void handleGameStarted(String json) {
        try {
            JsonNode root = NetworkManager.getObjectMapper().readTree(json);
            JsonNode map = root.get("messageBody").get("gameMap");

            startPoints.clear();
            checkpoints.clear();                    ///MST 6
            for (int x = 0; x < map.size(); x++) {
                JsonNode column = map.get(x);
                for (int y = 0; y < column.size(); y++) {
                    JsonNode tiles = column.get(y);
                    for (JsonNode tile : tiles) {
                        String type = tile.get("type").asText();
                        if ("StartPoint".equals(type)) {
                            startPoints.add(new Position(x, y));
                            logger.info("Gefundener Startpunkt bei (" + x + "," + y + ")");
                        }
                        ///MST 6
                        else if ("Checkpoint".equals(type)){
                            checkpoints.add(new Position(x, y));
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Fehler beim Parsen der Map: " + e.getMessage(), e);
        }
    }

    /**
     * Placeholder for additional logic when the active phase begins.
     *
     * @param json ActivePhase message
     */
    private void handleActivePhase(String json) {
    }

    /**
     * Chooses one of the available maps if this client is allowed to decide.
     *
     * @param json SelectMapMessage from the server
     */
    private void handleSelectMap(String json) {
        try {
            JsonNode root = NetworkManager.getObjectMapper().readTree(json);
            JsonNode body = root.get("messageBody");
            JsonNode mapsNode = body.get("availableMaps");
            int chooserId = body.get("allowedClientId").asInt();

            if (chooserId != thisClientId) {
                return; // Nicht unsere Aufgabe
            }

            String chosenMap = mapsNode.get(0).asText(); // Einfach erste Map
            String mapJson = String.format(
                    "{\"messageType\":\"MapSelected\",\"messageBody\":{\"mapName\":\"%s\"}}",
                    chosenMap
            );
            out.println(mapJson);
            logger.info("Wählt Map: " + chosenMap);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Fehler bei handleSelectMap: " + e.getMessage(), e);
        }
    }

    /**
     * Called when the server announces whose turn it is. If it's our turn, we may try to set a starting point.
     *
     * @param json CurrentPlayer message
     */
    private void handleCurrentPlayer(String json) {
        try {
            JsonNode root = NetworkManager.getObjectMapper().readTree(json);
            int current = root.get("messageBody").get("clientID").asInt();

            if (current == thisClientId) {
                logger.info("Ich bin dran! ");

                if (hasChosenStartpoint) {
                    logger.fine("Startpunkt bereits gewählt, tue nichts mehr. ");
                    return;
                }

                if (startPoints.isEmpty()) {
                    logger.warning("Startpunkte leer, versuche es später erneut...");
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            sendSetStartingPointLogic();
                        }
                    }, 200);
                } else {
                    sendSetStartingPointLogic();
                }
            } else {
                logger.fine("Nicht dran, warte...");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Fehler in handleCurrentPlayer: " + e.getMessage(), e);
        }
    }

    /**
     * Contains the actual logic for choosing a starting point. Currently: random choice from the list.
     */
    private void sendSetStartingPointLogic() {
        if (startPoints.isEmpty()) {
            logger.warning("Keine bekannten Startpunkte verfügbar. ");
            return;
        }
        Position selected = startPoints.get(new Random().nextInt(startPoints.size()));
        logger.info("Wählt Startpunkt bei " + selected);
        sendSetStartingPoint(selected.getX(), selected.getY());
        hasChosenStartpoint = true;
    }

    /**
     * Sends the chosen starting point to the server.
     *
     * @param x x-coordinate
     * @param y y-coordinate
     */
    private void sendSetStartingPoint(int x, int y) {
        String json = String.format(
                "{\"messageType\":\"SetStartingPoint\",\"messageBody\":{\"x\":%d,\"y\":%d}}", x, y);
        out.println(json);
        logger.info("SetStartingPoint gesendet: (" + x + "," + y +")");
    }

    /*
    private void handleYourCards(String json){
        if (hasSelectedCards) {
            //System.out.println("[KI] Karten bereits ausgewählt, überspringe.");
           logger.fine("Karten bereits ausgewählt, überspringe. ");
            return;
        }

        try{
            //System.out.println("[KI] handleYourCards empfangen");
            logger.info("handleYourCards empfangen");

            JsonNode root = NetworkManager.getObjectMapper().readTree(json);
            JsonNode cards = root.get("messageBody").get("cardsInHand");

            List<String> cardList = new ArrayList<>();
            for(JsonNode card : cards){
                cardList.add(card.asText());
            }

            Collections.shuffle(cardList);

            for(int i = 0; i < Math.min(5, cardList.size()); i++){
                sendSelectedCard(cardList.get(i), i);
            }

            hasSelectedCards = true; //damit es nicht nochmal ausgeführt wird
            //System.out.println("[KI] Zufällige Karten ausgewählt und gesendet.");
            logger.info("Zufällige Karten ausgewählt und gesendet. ");
        } catch(IOException e){
           //System.err.println("[KI] Fehler bei handleYourCards: " + e.getMessage());
            logger.log(Level.SEVERE, "Fehler bei handleYourCards: " + e.getMessage(), e);
        }
    }
     */

    /**
     * Processes our hand of cards (MST6 version). If cards haven't been selected yet and all required data is
     * present, a path to the next checkpoint is computed and suitable cards are chosen.
     *
     * @param json "YourCardsMessage" from the server
     */
    private void handleYourCards(String json) {
        if (hasSelectedCards || currentPosition == null || checkpoints.isEmpty()) return;

        try {
            JsonNode root = NetworkManager.getObjectMapper().readTree(json);
            JsonNode cards = root.get("messageBody").get("cardsInHand");

            Position target = checkpoints.get(0);
            List<Position> path = findShortestPath(currentPosition, target);
            List<String> selected = chooseCardsToFollowPath(cards, path);

            for (int i = 0; i < selected.size(); i++) {
                sendSelectedCard(selected.get(i), i);
            }
            hasSelectedCards = true;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Fehler bei handleYourCards", e);
        }
    }

    /**
     * Breadth-first search to find a (shortest) path on the grid from {@code start} to {@code goal}.
     *
     * @param start start position
     * @param goal  goal position
     * @return list of positions (excluding start) that form the path to the goal
     */
    private List<Position> findShortestPath(Position start, Position goal){
        Queue<Position> queue = new LinkedList<>();
        Map<Position, Position> cameFrom = new HashMap<>();
        Set<Position> visited = new HashSet<>();

        queue.add(start);
        visited.add(start);

        while(!queue.isEmpty()){
            Position current = queue.poll();
            if(current.equals(goal)) break;

            for(Position neighbor : getNeighbors(current)){
                if(!visited.contains(neighbor)){
                    queue.add(neighbor);
                    visited.add(neighbor);
                    cameFrom.put(neighbor, current);
                }
            }
        }

        List<Position> path = new ArrayList<>();
        Position current = goal;
        while(cameFrom.containsKey(current)){
            path.add(0, current);
            current = cameFrom.get(current);
        }
        return path;
    }

    /**
     * Returns the four orthogonal neighbors (up, down, left, right) of a position.
     *
     * @param pos origin position
     * @return list of neighboring positions
     */
    private List<Position> getNeighbors(Position pos){
        List<Position> neighbors = new ArrayList<>();
        int x = pos.getX();
        int y = pos.getY();

        neighbors.add(new Position(x + 1, y));
        neighbors.add(new Position(x - 1, y));
        neighbors.add(new Position(x, y +1 ));
        neighbors.add(new Position(x,y - 1));
        return neighbors;
    }

    /**
     * Very simple heuristic: prefer "Move1" cards until 5 cards are chosen, otherwise pick random ones.
     *
     * @param cards cards in hand as JsonNode
     * @param path  (currently unused) planned path
     * @return list of chosen cards (max 5)
     */
    private List<String> chooseCardsToFollowPath(JsonNode cards, List<Position> path){
        List<String> selected = new ArrayList<>();
        for(JsonNode cardNode : cards){
            String card = cardNode.asText();
            if(card.contains("Move1") && selected.size() < 5){
                selected.add(card);
            }
        }
        while(selected.size() < 5 && cards.size() > selected.size()){
            String card = cards.get(random.nextInt(cards.size())).asText();
            if(!selected.contains(card)) selected.add(card);
        }
        return selected;
    }

    /**
     * Sends a chosen card for a specific register to the server.
     *
     * @param cardName card identifier
     * @param register register index (0-based)
     */
    private void sendSelectedCard(String cardName, int register){
        String json = String.format(
                "{\"messageType\":\"SelectedCard\",\"messageBody\":{\"card\":\"%s\",\"register\":%d}}",
                cardName, register
        );
        out.println(json);
        logger.fine("SelectedCard gesendet: " + cardName + " -> Register " + register);
    }

    /**
     * Reacts to the "CurrentCards" message and actively plays our card.
     *
     * @param json CurrentCards message
     */
    private void handleCurrentCards(String json){
        hasSelectedCards = false;
        try {
            JsonNode root = NetworkManager.getObjectMapper().readTree(json);
            JsonNode activeCards = root.get("messageBody").get("activeCards");

            for (JsonNode cardInfo : activeCards) {
                int clientId = cardInfo.get("clientID").asInt();
                String cardName = cardInfo.get("card").asText();

                if (clientId == thisClientId) {
                    sendPlayCard(cardName);
                    logger.info("Spiele Karte: " + cardName);
                }
            }
        } catch (IOException e){
            logger.log(Level.SEVERE, "Fehler bei handleCurrentCards: " + e.getMessage(), e);
        }
    }

    /**
     * Sends the command to play a card.
     *
     * @param cardName card name
     */
    private void sendPlayCard(String cardName){
        String json = String.format(
                "{\"messageType\":\"PlayCard\",\"messageBody\":{\"card\":\"%s\"}}",
                cardName
        );
        out.println(json);
        logger.fine("PlayCard gesendet: " + cardName);
    }

    /**
     * Closes the network connection and stops the alive timer.
     */
    private void closeConnection() {
        try {
            if (aliveTimer != null) aliveTimer.cancel();
            if (socket != null) socket.close();
            logger.info("Verbindung geschlossen. ");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Fehler beim Schließen der Verbindung " + e.getMessage(), e);
        }
    }

    /**
     * Sets the current robot position. Must be called externally (e.g. via board updates).
     *
     * @param position new robot position
     */
    public void setCurrentPosition(Position position){
        this.currentPosition = position;
    }
}