package de.lmu.cleverecousins;

import de.lmu.cleverecousins.protocol.message.PlayerDisconnectedMessage;
import de.lmu.cleverecousins.protocol.message.SelectMapMessage;
import de.lmu.cleverecousins.protocol.messageBody.PlayerDisconnectedBody;
import de.lmu.cleverecousins.protocol.messageBody.SelectMapBody;
import de.lmu.util.LogConfigurator;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The {@code Server} class represents the main server for a multiplayer Robo Rally game.
 * It handles client connections, manages game state, broadcasts important messages,
 * and orchestrates readiness and map selection logic.
 *
 * <p>This server uses a fixed port (12345) and assigns unique client IDs to
 * connected players. It also manages a heartbeat system to ensure connectivity.</p>
 */

public class Server {

    /** Shared game instance across all clients. */
    public static final Game sharedGame = new Game();

    /** Executor to manage card actions during the game. */
    public static final CardExecutor cardExecutor = new CardExecutor();

    /** Game timer service controlling timed phases. */
    public static final GameTimerService timerService = new GameTimerService(30);
    private static final Logger logger = Logger.getLogger(Server.class.getName());
    private static final int PORT = 12345;
    private static final Set<Integer> assignedClientIDs = Collections.synchronizedSet(new HashSet<>());
    private static final Random random = new Random();

    /** Set of all connected client thread. */
    public static Set<ClientManager> clients = Collections.synchronizedSet(new HashSet<>());

    /** List of client IDs that are currently marked as ready. */
    public static List<Integer> readyPlayers = Collections.synchronizedList(new ArrayList<>());

    /** Game phase controller to manage game progression. */
    public static GamePhaseController phaseController;
    private static boolean mapSelectionSent = false;

    static {
        LogConfigurator.configureRootLogger(Level.FINE);
    }

    /**
     * Generates a unique client ID between 100 and 999.
     *
     * @return a unique integer client ID
     */
    public static synchronized int generateUniqueClientID() {
        int id;
        do {
            id = 100 + random.nextInt(900);
        } while (assignedClientIDs.contains(id));
        assignedClientIDs.add(id);
        return id;
    }

    /**
     * Releases a previously assigned client ID.
     *
     * @param id the ID to be released
     */
    public static synchronized void releaseClientID(int id) {
        assignedClientIDs.remove(id);
    }

    /**
     * Returns a list of currently ready player IDs.
     *
     * @return a list of ready player client IDs
     */
    public static List<Integer> getReadyPlayers() {
        return readyPlayers;
    }

    /**
     * Adds or removes a client from the ready list based on its status.
     * If at least 2 players become ready and map selection hasn't been sent,
     * the server sends a map selection message to all clients.
     *
     * @param c the client whose ready status changed
     */
    public static synchronized void changeReadyList(ClientManager c) {
        boolean wasEnoughBefore = readyPlayers.size() >= 2;

        if (c.getReady()) {
            if (!readyPlayers.contains(c.getClientID())) {
                readyPlayers.add(c.getClientID());
            }
        } else {
            readyPlayers.remove(Integer.valueOf(c.getClientID()));
        }

        boolean isEnoughNow = readyPlayers.size() >= 2;

        if (!wasEnoughBefore && isEnoughNow && !mapSelectionSent) {
            try {
                broadcastMapSelection();
                mapSelectionSent = true;
            } catch (IOException e) {
                logger.log(Level.SEVERE, "[ERROR] Map-Auswahl konnte nicht gesendet werden: " + e.getMessage(), e);
            }
        }

        if (readyPlayers.size() < 2) {
            mapSelectionSent = false;
        }
    }

    /**
     * Sends a map selection message to all clients, initiated by the first ready player.
     *
     * @throws IOException if broadcasting fails
     */
    public static void broadcastMapSelection() throws IOException {
        ClientManager firstReadyPlayer = findFirstReadyPlayer();
        if (firstReadyPlayer == null) {
            logger.warning("[WARN] Kein Spieler bereit für Map-Auswahl.");
            return;
        }

        List<String> availableMaps = getAvailableMaps();
        SelectMapBody body = new SelectMapBody(availableMaps, firstReadyPlayer.getClientID());
        String selectMapJson = NetworkManager.serialize(new SelectMapMessage(body));

        // Broadcast nur der SelectMapMessage an alle Clients
        synchronized (clients) {
            for (ClientManager cm : clients) {
                try {
                    cm.sendMessageToClient(selectMapJson);
                } catch (IOException e) {
                    logger.log(Level.WARNING, "[ERROR] SelectMapMessage konnte nicht an Client " + cm.getClientID() + " gesendet werden: " + e.getMessage(), e);
                }
            }
        }
        logger.fine("[DEBUG] Map-Auswahl-Nachricht an alle Clients gesendet: " + selectMapJson);
    }

    /**
     * Finds and returns the first player who is marked as ready.
     *
     * @return the first ready client, or {@code null} if none are ready
     */
    public static ClientManager findFirstReadyPlayer() {
        synchronized (clients) {
            for (ClientManager c : clients) {
                if (c.getReady()) {
                    return c;
                }
            }
        }
        return null;
    }

    /**
     * Returns a list of available map names for the game.
     *
     * @return a list of map names
     */
    public static List<String> getAvailableMaps() {
        return List.of("Dizzy Highway", "Extra Crispy", "Lost Bearings", "Death Trap");
    }

    /**
     * Notifies all clients that a player has disconnected from the server.
     *
     * @param clientID   the ID of the disconnected player
     * @param playerName the name of the disconnected player
     */
    public static void broadcastPlayerDisconnected(int clientID, String playerName) {
        try {
            var msg = new PlayerDisconnectedMessage(new PlayerDisconnectedBody(clientID, playerName));
            String json = NetworkManager.serialize(msg);

            synchronized (clients) {
                for (ClientManager cm : clients) {
                    cm.sendMessageToClient(json);
                }
            }
            logger.info("[INFO] PlayerDisconnected sauber an alle übertragen: " + playerName);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "[ERROR] broadcastPlayerDisconnected konnte nicht serialisiert werden: " + e.getMessage(), e);
        }
    }

    /**
     * Main server entry point. Listens for new client connections,
     * maintains a heartbeat to check client availability, and starts
     * {@code ClientManager} threads for each connection.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            logger.info("Robo Rally Game Server gestartet auf Port " + PORT);

            Timer heartbeatTimer = new Timer();
            heartbeatTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    synchronized (clients) {
                        long now = System.currentTimeMillis();
                        for (Iterator<ClientManager> it = clients.iterator(); it.hasNext(); ) {
                            ClientManager cm = it.next();
                            try {
                                long lastAlive = cm.getLastAliveTimestamp();
                                if (now - lastAlive > 10000) {
                                    logger.warning("[WARN] Verbindung zu " + cm.getPlayerName() + " verloren.");
                                    broadcastPlayerDisconnected(cm.getClientID(), cm.getPlayerName());
                                    cm.disconnect();
                                    it.remove();
                                    readyPlayers.remove(Integer.valueOf(cm.getClientID()));
                                    releaseClientID(cm.getClientID());
                                } else {
                                    try {
                                        cm.sendMessageToClient("{\"messageType\":\"Alive\",\"messageBody\":{}}");
                                    } catch (IOException ioex) {
                                        logger.log(Level.SEVERE, "[ERROR] Alive konnte nicht gesendet werden, Client wird entfernt: " + ioex.getMessage(), ioex);
                                        broadcastPlayerDisconnected(cm.getClientID(), cm.getPlayerName());
                                        cm.disconnect();
                                        it.remove();
                                        readyPlayers.remove(Integer.valueOf(cm.getClientID()));
                                        releaseClientID(cm.getClientID());
                                    }
                                }
                            } catch (Exception e) {
                                logger.log(Level.SEVERE, "[ERROR] Heartbeat-Problem: " + e.getMessage(), e);
                                cm.disconnect();
                                it.remove();
                                readyPlayers.remove(Integer.valueOf(cm.getClientID()));
                                releaseClientID(cm.getClientID());
                            }
                        }
                    }
                }
            }, 0, 5000);

            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    ClientManager cm = new ClientManager(socket, clients, phaseController);
                    synchronized (clients) {
                        clients.add(cm);
                    }
                    cm.start();
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "[ERROR] Fehler beim Annehmen neuer Verbindung: " + e.getMessage(), e);
                }
            }

        } catch (IOException e) {
            logger.log(Level.SEVERE, "[FATAL] Server konnte nicht gestartet werden: " + e.getMessage(), e);
        }
    }
}
