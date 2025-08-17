package de.lmu.cleverecousins;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.lmu.Board.*;
import de.lmu.cleverecousins.cards.DamageDeck;
import de.lmu.cleverecousins.cards.damageCards.DamageCard;
import de.lmu.cleverecousins.cards.damageCards.Spam;
import de.lmu.cleverecousins.cards.programmingCards.ProgrammingCard;
import de.lmu.cleverecousins.protocol.BaseMessage;
import de.lmu.cleverecousins.protocol.message.*;
import de.lmu.cleverecousins.protocol.messageBody.*;
import de.lmu.test.MapTestRunner;
import de.lmu.util.LogConfigurator;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Heart of the game
 *  <p>
 *  {@code GamePhaseController} manages the central game flow of Robo Rally
 *  It coordinates the different game phases.
 *  <ul>
 *      <li>Setup Phase (player selects starting position)</li>
 *      <li>Programming Phase (players select cards, timer management)</li>
 *      <li>Activation Phase (cards are executed one by one, including movement and interactions)</li>
 *  </ul>
 *  Additionally, this class handles game events such as robot movement, reboots,
 *  energy tile activation, damage card distribution, and checkpoint handling.
 *
 *  It works closely with the {@link Game}, {@link Board}, {@link DamageDeck},
 *  and all connected {@link ClientManager} instances to control player actions and
 *  broadcast the appropriate network messages.
 *  </p>
 *
 *  <p><b>Responsibilities:</b></p>
 *  <ul>
 *      <li>Managing card distribution and selection</li>
 *      <li>Handling robot movements and tile interactions</li>
 *      <li>Controlling the reboot logic, energy gain, and damage cards</li>
 *      <li>Advancing the game state (turn order, registers, phases)</li>
 *  </ul>
 *  </p>
 *  <p><b>Typical flow:</b></p>
 *  After the setup phase, the controller starts the programming phase via {@code startProgrammingPhase()},
 *  deals cards to players, starts a timer, and then transitions into the activation phase,
 *  where one card per register is executed per player.
 *
 *  @author Gabriel, Liz
 */
public class GamePhaseController implements TimerListener{

    /**
     * Logger used for debugging and tracking important events during game phases.
     * Configured to log detailed information (Level.FINE and above).
     */
    private static final Logger logger = Logger.getLogger(GamePhaseController.class.getName());

    // Configure the root logger to show fine-grained debug output
    static {
        LogConfigurator.configureRootLogger(Level.FINE);
    }
    /**
     * Holds the current state and configuration of the game, including players, phase, and registers.
     */
    private final Game game;

    /**
     * Executes programming cards' effects during the activation phase.
     * Used to apply the logic defined in each card to the game state.
     */
    private final CardExecutor cardExecutor;

    /**
     * Manages the game timer, including starting and expiring it during programming and activation phases.
     * Listeners can react to timer events such as when the time runs out.
     */
    private final GameTimerService timerService;

    /**
     * Set of all connected clients used primarily for broadcasting messages and identifying active participants.
     */
    private final Set<ClientManager> clients;   // zum Broadcasten

    /**
     * Tracks which players have already chosen a starting point during the setup phase.
     * Used to determine when all players are ready to proceed.
     */
    private final Set<Integer> startPointChosen = new HashSet<>();

    /**
     * Central instance of the DamageDeck used to draw damage cards (e.g. Spam, Trojan) during the game.
     */
    private final DamageDeck damageDeck = new DamageDeck();

    /**
     * Jackson ObjectMapper for parsing JSON messages from clients.
     */
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Optional test runner used for automated map testing and debugging.
     */
    private MapTestRunner test;

    /**
     * The game board instance, containing all tiles and providing access to their positions and behaviors.
     */
    private Board board;

    /**
     * Maximum number of checkpoints defined on the board. Used to determine game end conditions.
     */
    private int maxCheckpointCount;

    /**
     * Indicates whether the game has already ended, to prevent further actions once a winner is determined.
     */
    private boolean gameOver = false;

    /**
     * Constructs a new GamePhaseController.
     *
     * @param game the current game state and configuration
     * @param board the game board used for positioning and tile logic
     * @param cardExecutor responsible for executing card actions during the activation phase
     * @param timerService handles the timer and timeouts during timed phases
     * @param clients the set of connected clients to which game messages are broadcast
     */
    public GamePhaseController(Game game, Board board, CardExecutor cardExecutor, GameTimerService timerService, Set<ClientManager> clients) {
        this.game = game;
        this.cardExecutor = cardExecutor;
        this.timerService = timerService;
        this.clients = clients;
        this.timerService.getTimer().addListener(this);
        this.board = board;
        this.maxCheckpointCount = board.getMaxCheckpointCount();
    }

    /** Returns the current Game instance. */
    public Game getGame() {
        return game;
    }

    /** Returns true if Game is over. */
    public boolean isGameOver() {
        return gameOver;
    }

    /**
     * Initializes the setup phase of the game.
     * Resets all start point selections, sets the first player, and broadcasts the current phase and active player.
     */
    public void startSetupPhase() {
        logger.fine("[DEBUG] Starte Setup-Phase (0)");
        game.setCurrentPhase(0);

        // Reset aller Spieler-Flags für die Startpunkt-Wahl
        for (Player p : game.getAllPlayers()) {
            p.setHasChosenStartPoint(false);
        }

        // >>>>>> DEBUG Spielerliste ausgeben <<<<<<
        for (Player p : game.getAllPlayers()) {
            logger.fine("[DEBUG] Spieler im Setup: clientID=" + p.getClientID());
        }
        // >>>>>> ENDE DEBUG <<<<<<

        List<Integer> order = game.getPlayerOrder();
        if (order.isEmpty()) {
            logger.severe("[ERROR] Keine Spieler im Spiel – Setup-Phase abgebrochen.");
            return;
        }

        // Ersten Spieler festlegen
        int firstID = order.get(0);
        game.setCurrentPlayer(firstID);
        logger.fine("[DEBUG] Spielerreihenfolge: " + order);

        logger.fine("[DEBUG] Aktueller Spieler gesetzt auf Client-ID: " + firstID);


        // 1) ActivePhase=0 an alle
        ActivePhaseMessage phaseMsg = new ActivePhaseMessage(new ActivePhaseBody(0));
        broadcast(phaseMsg);

        // 2) CurrentPlayerMessage an alle, damit der erste Spieler sein Startpunkt-UI sieht
        CurrentPlayerMessage cpMsg = new CurrentPlayerMessage(new CurrentPlayerBody(firstID));
        broadcast(cpMsg);
        logger.fine("[DEBUG] Broadcasted CurrentPlayer for Setup: " + firstID);
    }

    /**
     * Starts the programming phase by distributing cards to each player,
     * broadcasting card information, and preparing the phase timer.
     */
    public void startProgrammingPhase() {
        logger.fine("[DEBUG] Starte Programmierphase (2)");

        game.setCurrentPhase(2);
        game.setTimerStarted(false);

        ActivePhaseMessage phaseMsg = new ActivePhaseMessage(new ActivePhaseBody(2));
        broadcast(phaseMsg);

        for (Player p : game.getAllPlayers()) {
            p.prepareNextRoundDeck();
            boolean shuffled = p.drawCards();

            List<String> cardNames = p.getHand().stream().map(ProgrammingCard::getName).toList();

            // 1. An Spieler selbst senden
            try {
                String yourCards = NetworkManager.serialize(new YourCardsMessage(new YourCardsBody(cardNames)));

                for (ClientManager c : clients) {
                    if (c.getClientID() == p.getClientID()) {
                        try {
                            c.sendMessageToClient(yourCards);
                        } catch (IOException e) {
                            logger.log(Level.SEVERE, "[ERROR] Fehler beim Senden von Karten an Client " + p.getClientID(), e);
                        }
                        break; // Spieler gefunden, abbrechen
                    }
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "[ERROR] Fehler beim Serialisieren der YourCards-Nachricht für Client " + p.getClientID(), e);
            }

            // 2. An andere Spieler senden
            try {
                String notYourCards = NetworkManager.serialize(new NotYourCardsMessage(new NotYourCardsBody(p.getClientID(), cardNames.size())));

                for (ClientManager c : clients) {
                    if (c.getClientID() != p.getClientID()) {
                        try {
                            c.sendMessageToClient(notYourCards);
                        } catch (IOException e) {
                            logger.log(Level.SEVERE, "[ERROR] Fehler bei NotYourCards an Client " + c.getClientID(), e);
                        }
                    }
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "[ERROR] Fehler beim Serialisieren der NotYourCards-Nachricht für Client " + p.getClientID(), e);
            }

            // 3. Mischen melden
            if (shuffled) {
                try {
                    String shuffleMsg = NetworkManager.serialize(new ShuffleCodingMessage(new ShuffleCodingBody(p.getClientID())));
                    broadcastRaw(shuffleMsg);
                    logger.fine("[DEBUG] Kartenstapel von Spieler " + p.getClientID() + " wurde neu gemischt.");
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "[ERROR] Fehler bei ShuffleCoding", e);
                }
            }
            logger.fine("[DEBUG] Karten an Spieler " + p.getClientID() + " verteilt.");
        }

        // 4. Aktuellen Spieler setzen
        int firstID = game.getPlayerOrder().get(0);
        game.setCurrentPlayer(firstID);

        try {
            String msg = NetworkManager.serialize(new CurrentPlayerMessage(new CurrentPlayerBody(firstID)));
            broadcastRaw(msg);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "[ERROR] Fehler bei CurrentPlayerMessage", e);
        }
    }

    /**
     * Called when a player finishes selecting cards for all registers.
     * Starts the countdown timer if it hasn’t been started yet.
     *
     * @param clientID The ID of the player who has completed selection.
     */
    public void selectionFinished(int clientID) {
        logger.fine("[DEBUG] Player " + clientID + " hat SelectionFinished gesendet.");

        // Wenn der erste fertig ist, den Timer starten
        if (!game.hasTimerStarted()) {
            game.setTimerStarted(true);
            broadcast(new TimerStartedMessage());
            logger.fine("[DEBUG] Timer gestartet (30 Sekunden)");
            timerService.getTimer().start();
        }
    }

    /**
     * Starts the activation phase by setting the register index to 0
     * and prompting the first player to play their card.
     */
    public void startActivationPhase() {
        logger.fine("[DEBUG] Starte Aktivierungsphase (3)");

        game.setCurrentPhase(3);
        game.setCurrentRegister(0);
        game.setCurrentPlayer(game.getPlayerOrder().get(0));
        promptCurrentPlayerToPlayCard();
    }

    /**
     * Handles the selection of a player's starting point during the setup phase (phase 0).
     * Parses the received JSON to extract coordinates and assigns them to the player's robot.
     * Once all players have selected a position, the game proceeds to the programming phase.
     *
     * @param clientID ID of the player sending the request
     * @param json the incoming message containing the desired x and y coordinates
     * @throws IOException if the JSON message cannot be parsed
     */
    public void setStartingPoint(int clientID, String json) throws IOException {
        logger.fine("[DEBUG] SetStartingPoint aufgerufen mit: " + json);

        // NEU: nur in Phase 0 erlauben
        if (game.getCurrentPhase() != 0) {
            logger.warning("[WARN] SetStartingPoint empfangen, aber nicht mehr in Setup-Phase — ignoriert.");
            return;
        }

        // 1) JSON parsen
        JsonNode root = objectMapper.readTree(json);
        JsonNode body = root.get("messageBody");
        int x = body.get("x").asInt();
        int y = body.get("y").asInt();

        // 2) Ist der richtige Spieler am Zug?
        int expectedID = game.getPlayerOrder().get(game.getCurrentPlayerIndex());
        if (clientID != expectedID) {
            logger.warning("[WARN] Spieler " + clientID + " ist nicht dran (erwartet: " + expectedID + ")");
            return;
        }

        // 3) Startpunkt setzen
        Player player = game.getPlayer(clientID);
        Position pos = new Position(x, y);
        player.setStartPoint(pos);
        player.getRobot().setPosition(pos);
        player.getRobot().setDirection(Direction.RIGHT);
        player.setHasChosenStartPoint(true);

        // 4) Broadcast StartingPointTakenMessage an alle
        StartingPointTakenBody takenBody = new StartingPointTakenBody(x, y, "right", clientID);
        broadcast(new StartingPointTakenMessage(takenBody));
        logger.fine("[DEBUG] Spieler " + clientID + " hat Startpunkt bei " + x + "," + y);
        sendRobotPosition(clientID, x, y, "right");

        // 5) Prüfen, ob noch jemand fehlt
        boolean allChosen = game.getAllPlayers().stream()
                .allMatch(Player::hasChosenStartPoint);

        if (!allChosen) {
            // Noch nicht alle gewählt: nächsten Spieler ankündigen
            game.advanceStartingPlayer();
            int nextID = game.getCurrentPlayerClientID();

            CurrentPlayerMessage cpMsg = new CurrentPlayerMessage(new CurrentPlayerBody(nextID));
            String cpJson = NetworkManager.serialize(cpMsg);
            for (ClientManager cm : clients) {
                cm.sendMessageToClient(cpJson);
            }
            logger.fine("[DEBUG] Broadcasted CurrentPlayer for Startpoint: " + cpJson);
            return;
        }

        // --- Alle haben gewählt: jetzt Phase 1 starten ---
        logger.fine("[DEBUG] Alle Spieler haben Startpunkte gesetzt...");

        // Phase 1 brauchen wir dann für "UpgradePhase"
        /*
        game.setCurrentPhase(1); // Phase umschalten
        broadcast(new ActivePhaseMessage(new ActivePhaseBody(1)));
        System.out.println("[DEBUG] Broadcasted ActivePhase 1");
         */

        // Karten verteilen
        startProgrammingPhase();
    }

    /**
     * Handles the assignment of a programming card to a specific register during the programming phase.
     * Also removes the selected card from the player's hand and broadcasts the update.
     * When all registers are filled, starts the programming timer if not already running.
     *
     * @param clientID ID of the player sending the card selection
     * @param json the incoming message specifying the card name and target register
     * @throws IOException if the JSON message cannot be parsed
     */
    public void selectCard(int clientID, String json) throws IOException {
        logger.fine("[DEBUG] selectCard aufgerufen");

        JsonNode root = objectMapper.readTree(json);
        JsonNode body = root.get("messageBody");

        String cardName = body.get("card").isNull() ? null : body.get("card").asText();
        int register = body.get("register").asInt();

        // Check nur außerhalb der Programmierphase
        if (game.getCurrentPhase() != 2) {
            int expectedID = game.getPlayerOrder().get(game.getCurrentPlayerIndex());
            if (clientID != expectedID) {
                logger.warning("[WARN] Spieler " + clientID + " ist nicht dran (erwartet: " + expectedID + ")");
                return;
            }
        }

        Player player = game.getPlayer(clientID);
        if (player == null) {
            logger.severe("[ERROR] Spieler nicht gefunden!");
            return;
        }
        logger.fine("[DEBUG] Handkarten von Spieler " + clientID + ":");

        for (ProgrammingCard card : player.getHand()) {
            logger.fine(" - " + card.getName());
        }

        boolean filled = cardName != null;
        boolean success = player.setRegisterCard(cardName, register);

        if (!success) {
            logger.warning("[WARN] Karte konnte nicht gesetzt werden: " + cardName);
            return;
        }

        if (filled) {
            Iterator<ProgrammingCard> it = player.getHand().iterator();
            while (it.hasNext()) {
                ProgrammingCard card = it.next();
                if (card.getName().equals(cardName)) {
                    it.remove();
                    break;
                }
            }
        }

        broadcast(new CardSelectedMessage(new CardSelectedBody(clientID, register, filled)));

        long filledNow = 0;
        for (int i = 0; i < 5; i++) {
            if (player.getRobot().getRegister(i) != null) {
                filledNow++;
            }
        }

        if (filledNow == 5) {
            logger.fine("[DEBUG] Spieler " + clientID + " hat 5 Karten gesetzt.");
            broadcast(new SelectionFinishedMessage(new SelectionFinishedBody(clientID)));

            // Timer nur starten, wenn er noch nicht läuft
            if (!game.hasTimerStarted()) {
                game.setTimerStarted(true);
                broadcast(new TimerStartedMessage());
                logger.fine("[DEBUG] Timer wird gestartet (30 Sekunden)");
                timerService.getTimer().start();
            }
        }
    }

    /**
     * Called when the programming timer expires.
     * Automatically fills any remaining empty registers of all players with random cards from their hand.
     * Sends updated card assignments to affected players and transitions to the activation phase.
     */
    public void onTimerExpired(){
        List<Integer> slowClients = new ArrayList<>();

        for(Player p: game.getAllPlayers()){
            long filled = 0;
            for(int i = 0; i < 5; i++){
                if(p.getRobot().getRegister(i) != null) filled++;
            }

            if(filled < 5){
                slowClients.add(p.getClientID());

                List<ProgrammingCard> hand = new ArrayList<>(p.getHand());
                Collections.shuffle(hand);
                for(int i = 0; i < 5 && !hand.isEmpty(); i++){
                    if(p.getRobot().getRegister(i) == null){
                        p.getRobot().setRegister(i, hand.remove(0));
                    }
                }

                List<String> drawn = new ArrayList<>();
                for(int i = 0; i < 5; i++){
                    ProgrammingCard card = p.getRobot().getRegister(i);
                    if(card != null) drawn.add(card.getName());
                }
                broadcastToClient(p.getClientID(), new CardsYouGotNowMessage(new CardsYouGotNowBody(drawn)));
            }
        }
        broadcast(new TimerEndedMessage(new TimerEndedBody(slowClients)));
        logger.fine("[DEBUG] Timer beendet. Nachzügler: " + slowClients);

        game.setTimerStarted(false);
        broadcast(new ActivePhaseMessage(new ActivePhaseBody(3)));
        startActivationPhase();
    }

    /**
     * Handles the logic when a player plays a card during the activation phase.
     * Verifies the played card, applies its effects, checks for game end conditions,
     * and moves to the next player or register.
     *
     * @param clientID The ID of the player playing the card.
     * @param json The JSON data containing the card information.
     * @throws IOException If message broadcasting fails.
     */
    public void playCard(int clientID, String json) throws IOException {
        JsonNode root = objectMapper.readTree(json);
        JsonNode body = root.get("messageBody");

        if (gameOver) {
            logger.fine("[DEBUG] Spiel ist bereits beendet, keine weiteren Aktionen.");
            return;
        }

        String cardName = body.get("card").asText();
        int currentRegister = game.getCurrentRegister();
        int expectedID = game.getPlayerOrder().get(game.getCurrentPlayerIndex());

        if (clientID != expectedID) {
            logger.warning("[WARN] Spieler " + clientID + " ist nicht dran!");
            return;
        }

        Player player = game.getPlayer(clientID);
        if (player == null) return;

        Robot robot = player.getRobot();
        ProgrammingCard card = robot.getRegister(currentRegister);

        if (card == null || !card.getName().equals(cardName)) {
            logger.severe("[ERROR] Gespielte Karte stimmt nicht mit Register überein!");
            return;
        }

        logger.fine("[DEBUG] Spieler " + clientID + " spielt Karte: " + card.getName());

        /// Movement and Turning
        Position before = robot.getPosition();
        Direction beforeDir = robot.getDirection();

        // NEW: central Logic for Movements
        card.execute(robot, this);

        Position after = robot.getPosition();
        Direction afterDir = robot.getDirection();

        // Movement check
        if (!before.equals(after)) {
            broadcast(new MovementMessage(new MovementBody(clientID, after.getX(), after.getY())));
            logger.fine("[Debug] Bewegung erkannt: " + after);
        }

        // Turning check
        if (!beforeDir.equals(afterDir)) {
            String rotation = beforeDir.turnRight() == afterDir ? "clockwise"
                    : beforeDir.turnLeft() == afterDir ? "counterclockwise"
                    : "uturn";

            broadcast(new PlayerTurningMessage(new PlayerTurningBody(clientID, rotation)));
            logger.fine("[DEBUG] Rotation erkannt: " + rotation);
        }

        // Energy check
        if(card.getName().equals("PowerUp")) {
            broadcast(new EnergyMessage(new EnergyBody(clientID, 1, "PowerUpCard")));
            logger.fine("[DEBUG] PowerUp erkannt: 1");
        }

        // Check if SpamCard
        if (card.getName().equals("Spam")) {
            logger.fine("[DEBUG] Spam-Karte gespielt auf Register " + currentRegister);

            // 1. Hole Spieler
            Player spamPlayer = robot.getOwner();

            // 2. Ziehe neue Karte aus seinem DiscardDeck (ggf. mischt er selbst)
            ProgrammingCard newCard = spamPlayer.getDiscardDeck().draw();

            // Falls leer
            if (newCard == null) {
                logger.warning("[WARN] Keine Ersatzkarte mehr verfügbar, Register bleibt leer");
                robot.setRegister(currentRegister, null);
            } else {
                robot.setRegister(currentRegister, newCard);
                sendReplaceCard(clientID, currentRegister, newCard.getName());
                logger.fine("[DEBUG] Spam-Karte ersetzt durch: " + newCard.getName());
            }
        }

        broadcast(new CardPlayedMessage(new CardPlayedBody(clientID, card.getName())));

        // Check if robot has all Checkpoints -> Game ends, robot wins
        if (robot.getNextCheckpoint() > maxCheckpointCount) {
            logger.info("[DEBUG] Spieler " + player.getClientID() + " hat alle Checkpoints erreicht und gewinnt!");

            sendGameFinished(player.getClientID());
            for (Player p : game.getAllPlayers()) {
                Robot r = p.getRobot();
                if (r != null) {
                    r.removeFromGame();
                    board.removeRobot(r);
                }
            }
            gameOver = true;
            return;
        }

        // Nächster Spieler oder nächstes Register
        if (game.getCurrentPlayerIndex() < game.getPlayerOrder().size() - 1) {
            game.nextPlayer();
        } else {
            if (currentRegister < 4) {
                game.setCurrentRegister(currentRegister + 1);
                game.setCurrentPlayer(game.getPlayerOrder().get(0));
            } else {
                logger.fine("[DEBUG] Alle 5 Register abgearbeitet.");

                // Nimmt alle Karten im Register und tut sie zurück in den Pile
                for (Player p : game.getAllPlayers()) {
                    p.discardHand();
                    p.discardUsedCards();
                }

                // Spiellogik zurücksetzen
                game.setCurrentRegister(0);
                game.setCurrentPlayer(game.getPlayerOrder().get(0));

                // Neue Runde: zurück zur Programmierphase
                startProgrammingPhase();
                return;
            }
        }

        // 🔫 Shooting check am Ende des Registers
        if (game.getCurrentRegister() == 4) {
            Robot shooter = robot;
            Position shooterPos = shooter.getPosition();
            Direction shooterDir = shooter.getDirection();

            for (Player other : game.getAllPlayers()) {
                if (other.getClientID() == clientID) continue;

                Robot target = other.getRobot();
                Position targetPos = target.getPosition();

                Position p = shooterPos.moved(shooterDir);
                boolean wallBetween = false;

                while (board.isInBounds(p.getX(), p.getY())) {
                    if (isBlockedByWall(p.moved(shooterDir.opposite()), shooterDir)) {
                        wallBetween = true;
                        break;
                    }
                    if (p.equals(targetPos)) {
                        if (!wallBetween) {
                            Spam spam = damageDeck.drawSpam();
                            if (spam != null) {
                                other.addDamageCard(spam);
                                logger.info("[SHOOT] Spieler " + clientID + " trifft Spieler " + other.getClientID() + " -> Spam-Karte");
                            } else {
                                logger.warning("[SHOOT] Keine Spam-Karten mehr verfügbar!");
                            }
                        }
                        break;
                    }
                    p = p.moved(shooterDir);
                }
            }
        }

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                // Aktuelle Karte des nächsten Spielers senden
                Player nextPlayer = game.getPlayer(game.getPlayerOrder().get(game.getCurrentPlayerIndex()));
                ProgrammingCard nextCard = nextPlayer.getRobot().getRegister(game.getCurrentRegister());

                if (nextCard == null) {
                    logger.warning("WARNUNG: nextCard ist null – vermutlich neue Runde noch nicht vollständig initialisiert.");
                    return;
                }

                List<CurrentCardsBody.ActiveCard> cards = List.of(new CurrentCardsBody.ActiveCard(nextPlayer.getClientID(), nextCard.getName()));
                broadcast(new CurrentCardsMessage(new CurrentCardsBody(cards)));

                logger.fine("[DEBUG] Nächster Spieler: " + nextPlayer.getClientID() + " mit Karte: " + nextCard.getName());
            }
        }, 500);
    }

    /**
     * Notifies the current player that it is their turn to play the next card.
     * Sends a message to the active client showing their card for the current register slot.
     * Called at the start of each player's turn in the activation phase.
     */
    private void promptCurrentPlayerToPlayCard() {
        int clientID = game.getPlayerOrder().get(game.getCurrentPlayerIndex());
        Player player = game.getPlayer(clientID);
        ProgrammingCard card = player.getRobot().getRegister(game.getCurrentRegister());

        if (card == null) {
            logger.severe("[ERROR] Keine Karte in Register " + game.getCurrentRegister() + " bei Spieler " + clientID);
            return;
        }

        CurrentCardsBody body = new CurrentCardsBody(List.of(new CurrentCardsBody.ActiveCard(clientID, card.getName())));

        try {
            String msg = NetworkManager.serialize(new CurrentCardsMessage(body));
            for (ClientManager c : clients) {
                if (c.getClientID() == clientID) {
                    c.sendMessageToClient(msg);
                }
            }
            logger.fine("[DEBUG] Spieler " + clientID + " ist dran mit Karte: " + card.getName());
        } catch (IOException e) {
            logger.log(Level.SEVERE, "[ERROR] Fehler bei CurrentCardsMessage: " + e.getMessage(), e);
        }
    }

    /**
     * Sends a serialized JSON message to a specific client based on their ID.
     *
     * @param clientID the ID of the target client
     * @param msg the BaseMessage to be sent
     */
    private void broadcastToClient(int clientID, BaseMessage<?> msg) {
        try {
            String json = NetworkManager.serialize(msg);
            for (ClientManager c : clients) {
                if (c.getClientID() == clientID) {
                    try {
                        c.sendMessageToClient(json);
                    } catch (IOException ignored) {}
                    break;
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "[ERROR] Nachricht an Client " + clientID + " fehlgeschlagen: " + e.getMessage(), e);
        }
    }

    /**
     * Broadcasts a raw (already serialized) JSON string to all connected clients.
     *
     * @param msg the message to send to every client
     */
    private void broadcastRaw(String msg) {
        for (ClientManager c : clients) {
            try {
                c.sendMessageToClient(msg);
            } catch (IOException ignored) {}
        }
    }

    /**
     * Broadcasts a BaseMessage object to all connected clients.
     * Automatically serializes the message into JSON before sending.
     *
     * @param msg the message to send
     */
    private void broadcast(BaseMessage<?> msg) {
        try {
            String json = NetworkManager.serialize(msg);
            for (ClientManager c : clients) {
                try {
                    c.sendMessageToClient(json);
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "[ERROR] Nachricht an Client " + c.getClientID() + " fehlgeschlagen: " + e.getMessage(), e);
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "[ERROR] Fehler beim Serialisieren der Nachricht: " + e.getMessage(), e);
        }
    }

    /**
     * Handles a player's selection of damage cards (e.g. Spam, Virus, Trojan).
     * Draws the selected damage cards from the DamageDeck and assigns them to the player.
     * Then confirms the selection via {@code sendSelectedDamage}.
     *
     * @param clientID the ID of the player making the selection
     * @param json the incoming message containing the list of selected damage cards
     */
    public void handlePickDamage(int clientID, String json){
        try{
            JsonNode root = objectMapper.readTree(json);
            JsonNode body = root.get("messageBody");

            int count = body.get("count").asInt();
            List<String> picked = new ArrayList<>();

            for(JsonNode node : body.get("availablePiles")){
                picked.add(node.asText());
            }

            logger.fine("[DEBUG] PickDamage erhalten von Client " + clientID + ": " + picked);

            //Hole Spieler
            Player player = game.getPlayer(clientID);
            if(player == null){
                logger.severe("[ERROR] Spieler nicht gefunden für PickDamage.");
                return;
            }

            //Schaden anwenden
            for(String damage : picked){
                DamageCard card = switch (damage){
                    case "Spam" -> damageDeck.drawSpam();
                    case "Trojan" -> damageDeck.drawTrojanHorse();
                    case "Virus" -> damageDeck.drawVirus();
                    case "Worm" -> damageDeck.drawWorm();
                    default -> null;
                };

                if(card != null){
                    player.addDamageCard(card);
                    logger.info("[INFO] Spieler " + clientID + " nimmt Schaden: " + card.getName());
                } else{
                    logger.warning("[WARNUNG] Keine Karte verfügbar oder unbekannter Typ: " + damage);
                }
            }

            //Methode sendSelectDamage wird aufgerufen (Nachricht zurück an den Client)
            sendSelectedDamage(clientID, picked);

        } catch (Exception e){
            logger.log(Level.SEVERE, "[ERROR] Fehler in handlePickDamage: " + e.getMessage(), e);
        }
    }

    /**
     * Sends a confirmation message to the client listing the damage cards they received.
     *
     * @param clientID the recipient client
     * @param cardNames list of damage card names the player picked
     */
    public void sendSelectedDamage(int clientID, List<String> cardNames){
        try{
            SelectedDamageBody body = new SelectedDamageBody(cardNames);
            SelectedDamageMessage message = new SelectedDamageMessage(body);
            broadcastToClient(clientID, message);
            logger.fine("[DEBUG] SelectedDamage an Client " + clientID + " gesendet: " + cardNames);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "[ERROR] Fehler beim Senden von SelectedDamage: " + e.getMessage());
        }
    }

    /**
     * Sends a message to a client instructing them to replace the card in a specific register slot.
     *
     * @param clientID the ID of the client
     * @param register the index of the register to be replaced
     * @param newCard the name of the new card that should replace the current one
     */
    public void sendReplaceCard(int clientID, int register, String newCard){
        try{
            ReplaceCardBody body = new ReplaceCardBody(clientID, newCard, register);
            ReplaceCardMessage message = new ReplaceCardMessage(body);
            broadcastToClient(clientID, message);

            logger.fine("[DEBUG] ReplaceCard an Spieler " + clientID + ": Register " + register + " -> " + newCard);
        } catch (Exception e){
            logger.log(Level.SEVERE, "[ERROR] Fehler beim Senden von ReplaceCard: " + e.getMessage(), e);
        }
    }

    /**
     * Sends a message to all clients that a player has reached a checkpoint.
     *
     * @param clientID the player who reached the checkpoint
     * @param checkpoint the number of the checkpoint reached
     */
    public void sendCheckPointReached(int clientID, int checkpoint){
        try{
            CheckPointReachedBody body = new CheckPointReachedBody(clientID, checkpoint);
            CheckPointReachedMessage message = new CheckPointReachedMessage(body);
            broadcastToClient(clientID, message);
            logger.fine("[DEBUG] CheckPointReached an Spieler " + clientID + ": Checkpoint " + checkpoint);

        } catch(Exception e){
            logger.log(Level.SEVERE, "[ERROR] Fehler beim Senden von CheckPointReached: " + e.getMessage(), e);
        }
    }

    /**
     * Broadcasts a message that the game has ended and announces the winner.
     *
     * @param clientID the client ID of the winning player
     */
    public void sendGameFinished(int clientID){
        try{
            GameFinishedBody body = new GameFinishedBody(clientID);
            GameFinishedMessage message = new GameFinishedMessage(body);
            broadcast(message);
            logger.log(Level.FINE, "[DEBUG] GameFinished gesendet: Gewinner ist Spieler {0}", clientID);
        } catch(Exception e){
            logger.log(Level.SEVERE, "[ERROR] Fehler beim Senden von GameFinished: {0}", e.getMessage());
        }
    }

    /**
     * Returns true if a wall blocks movement from the given position in the given direction.
     */
    public boolean isBlockedByWall(Position pos, Direction dir) {
        List<BoardTile> tiles = board.getTilesAt(pos);
        for (BoardTile tile : tiles) {
            if (tile instanceof WallTile wall) {
                if (wall.getBlockedSides().contains(dir)) {
                    return true;
                }
            }
        }
        // Prüfen ob von der anderen Seite geblockt
        Position nextPos = pos.moved(dir);
        List<BoardTile> nextTiles = board.getTilesAt(nextPos);
        for (BoardTile tile : nextTiles) {
            if (tile instanceof WallTile wall) {
                if (wall.getBlockedSides().contains(dir.opposite())) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean tryPushRobot(Position pos, Direction dir) {
        Robot blockingRobot = board.getRobotAt(pos.moved(dir));
        if (blockingRobot == null) {
            return true; // nichts im Weg
        }

        Position nextPos = pos.moved(dir);
        if (isBlockedByWall(pos, dir) || !board.isInBounds(nextPos.getX(), nextPos.getY())) {
            return false; // Wand oder Spielfeldgrenze
        }

        if (tryPushRobot(nextPos, dir)) {
            executeMoveForward(blockingRobot, 1); // verschiebe blockierenden Roboter
            return true;
        }

        return false; // kann nicht pushen
    }

    /**
     * Moves the given robot forward by a specified number of steps.
     * Handles wall collisions, falling off the board, field activations, and reboot logic.
     *
     * @param robot The robot to move.
     * @param steps The number of forward steps to execute.
     */
    public void executeMoveForward(Robot robot, int steps) {
        for (int i = 0; i < steps; i++) {
            Position currentPos = robot.getPosition();
            Direction dir = robot.getDirection();
            Position nextPos = currentPos.moved(dir);

            // Prüfen, ob Mauer blockiert
            if (isBlockedByWall(currentPos, dir)) {
                logger.fine(String.format("[DEBUG] %d at %s facing %s is blocked by wall.%n",
                        robot.getOwner().getClientID(), currentPos, dir));
                break;
            }

            // Prüfen, ob Spielfeldgrenze überschritten
            if (!board.isInBounds(nextPos.getX(), nextPos.getY())) {
                handleReboot(robot, currentPos);
                //break;
                return;
            }

            // Versuche den Blockierer zu pushen
            if (board.getRobotAt(nextPos) != null) {
                if (!tryPushRobot(currentPos, dir)) {
                    // Pushing failed
                    return;
                }
            }

            // Roboter auf neue Position bewegen
            board.moveRobot(robot, nextPos);
            logger.fine(String.format("[DEBUG] %d moved from %s to %s facing %s%n",
                    robot.getOwner().getClientID(), currentPos, nextPos, dir));

            // JSON-Nachricht an alle Clients senden
            RobotPositionBody posBody = new RobotPositionBody(
                    robot.getOwner().getClientID(),
                    nextPos.getX(),
                    nextPos.getY(),
                    dir.toString()
            );

            broadcast(new RobotPositionMessage(posBody));
            // Felder am neuen Standort aktivieren
            for (BoardTile tile : board.getTilesAt(nextPos)) {
                tile.activate(robot);

                // Checkpoints prüfen
                if (tile instanceof CheckpointTile checkpointTile) {
                    int reachedCheckpoint = checkpointTile.getCount();
                    if (robot.getNextCheckpoint() == reachedCheckpoint) {
                        //robot.addCheckpointCount();
                        sendCheckPointReached(robot.getOwner().getClientID(), reachedCheckpoint);
                    }
                }

                if(tile instanceof EnergyTile energyTile){
                    int gained = energyTile.getCount();
                    if(gained > 0) {
                        robot.gainEnergy(gained);
                        int clientID = robot.getOwner().getClientID();
                        broadcast(new EnergyMessage(new EnergyBody(clientID, gained, "EnergyTile")));
                        logger.fine("[DEBUG] PowerUp erkannt: " + gained);
                    }
                }
            }

            // Falls Reboot ausgelöst wurde
            if (robot.isRebooting()) {
                logger.fine(String.format("[DEBUG] %d triggered reboot on tile -> back to %s%n",
                        robot.getOwner().getClientID(), robot.getPosition()));
                board.moveRobot(robot, robot.getPosition());
                return;
            }
        }
    }

    /**
     * Moves the given robot backward by a specified number of steps.
     * Includes wall detection, boundary checks, and tile activations similar to forward movement.
     *
     * @param robot The robot to move backward.
     * @param steps The number of backward steps to execute.
     */
    public void executeMoveBackward(Robot robot, int steps) {
        for (int i = 0; i < steps; i++) {
            Position currentPos = robot.getPosition();
            Direction dir = robot.getDirection().opposite();
            Position nextPos = currentPos.moved(dir);

            if (isBlockedByWall(currentPos, dir)) {
                logger.fine(String.format("[DEBUG] %d at %s facing %s is blocked by wall (backward).%n",
                        robot.getOwner().getClientID(), currentPos, dir));
                break;
            }

            if (!board.isInBounds(nextPos.getX(), nextPos.getY())) {
                handleReboot(robot, currentPos);
                //break;
                return;
            }

            // Versuche den Blockierer zu pushen
            if (board.getRobotAt(nextPos) != null) {
                if (!tryPushRobot(currentPos, dir)) {
                    // Pushing failed
                    return;
                }
            }

            board.moveRobot(robot, nextPos);
            logger.fine(String.format("[DEBUG] %d moved backward from %s to %s%n",
                    robot.getOwner().getClientID(), currentPos, nextPos));

            for (BoardTile tile : board.getTilesAt(nextPos)) {
                tile.activate(robot);

                // Prüfen, ob es ein CheckpointTile ist
                if (tile instanceof CheckpointTile checkpointTile) {
                    int reachedCheckpoint = checkpointTile.getCount();
                    if (robot.getNextCheckpoint() == reachedCheckpoint) {
                        //robot.addCheckpointCount();
                        sendCheckPointReached(robot.getOwner().getClientID(), reachedCheckpoint);
                    }
                }

                if(tile instanceof EnergyTile energyTile){
                    int gained = energyTile.getCount();
                    if(gained > 0) {
                        robot.gainEnergy(gained);
                        int clientID = robot.getOwner().getClientID();
                        broadcast(new EnergyMessage(new EnergyBody(clientID, gained, "EnergyTile")));
                        logger.fine("[DEBUG] PowerUp erkannt: " + gained);
                    }
                }
            }

            if (robot.isRebooting()) {
                logger.fine(String.format("[DEBUG] %d triggered reboot on tile -> back to %s%n",
                        robot.getOwner().getClientID(), robot.getPosition()));
                board.moveRobot(robot, robot.getPosition());
                return;
            }
        }
    }

    /**
     * Handles the rebooting logic for a robot that has fallen off the board.
     * Moves the robot to a restart position and assigns a Spam damage card.
     *
     * @param robot The robot to reboot.
     * @param fromPos The position the robot fell from.
     */
    private void handleReboot(Robot robot, Position fromPos) {
        if (fromPos.getX() < 4) {
            logger.fine(String.format("[DEBUG] %d fell in start area -> rebooting to starting point %s",
                    robot.getOwner().getClientID(), robot.getStartingPoint()));
            robot.reboot();

        } else {
            Position restart = board.getRestartPoint();
            logger.fine(String.format("[DEBUG] %d fell beyond start area -> rebooting to restart point %s",
                    robot.getOwner().getClientID(), restart));
            robot.reboot();
            robot.setRebootPosition(restart);
        }
        board.moveRobot(robot, robot.getPosition());

        // NEU: Ziehe Spam-Karte und füge hinzu

        Player player = robot.getOwner();
        Spam spamCard = damageDeck.drawSpam();
        if (spamCard != null) {
            player.addDamageCard(spamCard);
            logger.info("[REBOOT] Spieler " + player.getClientID() + " erhält eine Spam-Karte.");
        } else {
            logger.info("[REBOOT] Keine Spam-Karten mehr im Deck!");
        }
    }

    /**
     * Returns the current game board instance.
     *
     * @return the Board object containing the map and tile layout
     */
    public Board getBoard() {
        return board;
    }

    /**
     * Checks whether all players have chosen their starting positions.
     *
     * @return true if all players have selected their starting point, false otherwise
     */
    public boolean allPlayersChoseStart() {
        // game.getPlayerOrder() liefert die Liste aller Client-IDs im Spiel
        return startPointChosen.size() == game.getPlayerOrder().size();
    }

    /**
     * Sends a message to all clients with the updated position and direction of a robot.
     * This is typically called after a robot moves or reboots.
     *
     * @param clientID the ID of the player whose robot moved
     * @param x the new x-coordinate of the robot
     * @param y the new y-coordinate of the robot
     * @param direction the direction the robot is now facing (e.g. "north")
     */
    public void sendRobotPosition(int clientID, int x, int y, String direction) {
        try {
            var body = new RobotPositionBody(clientID, x, y, direction);
            var message = new RobotPositionMessage(body);
            broadcast(message);
            logger.fine(String.format("[DEBUG] RobotPosition gesendet an alle: (%d,%d) Richtung %s für Spieler %d%n", x, y, direction, clientID));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "[ERROR] Fehler beim Senden von RobotPosition: {0}", e.getMessage());

        }
    }
}
