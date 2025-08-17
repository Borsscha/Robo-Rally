package de.lmu.cleverecousins;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.lmu.cleverecousins.protocol.BaseMessage;
import de.lmu.util.LogConfigurator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Lightweight TCP client for communicating with the RoboRally game server.
 * <p>
 * The {@code Client} establishes a socket connection, spawns a listener thread to
 * read server messages line by line, filters heartbeats, validates JSON, and
 * forwards messages to a configurable {@link #messageConsumer}. It also provides
 * multiple convenience {@code send(...)} overloads for different payload types.
 * </p>
 *
 * <h3>Responsibilities</h3>
 * <ul>
 *   <li>Opening/closing the socket connection.</li>
 *   <li>Reading incoming messages asynchronously.</li>
 *   <li>Basic heartbeat handling (auto-acknowledging "Alive").</li>
 *   <li>Serialization of {@link BaseMessage} objects via {@link NetworkManager}.</li>
 * </ul>
 *
 * <strong>Threading:</strong> Incoming messages are processed in a dedicated daemon
 * listener thread. UI code should be dispatched onto the UI thread (e.g. JavaFX) by
 * the provided consumer if necessary.
 */
public class Client {

    /** Logger for client I/O and diagnostics. */
    private static final Logger logger = Logger.getLogger(Client.class.getName());

    static {
        LogConfigurator.configureRootLogger(Level.FINE);
    }

    /** Active socket connection. */
    private Socket socket;

    /** Writer to the server. Auto-flushing is enabled. */
    private PrintWriter out;

    /** Reader for lines from the server. */
    private BufferedReader in;

    /** Background thread that listens for incoming messages. */
    private Thread listenerThread;

    /** Parses user input commands (slash-commands etc.) to JSON. */
    private final UserInputHandler userInputHandler = new UserInputHandler();

    /** Callback invoked for every non-heartbeat, valid JSON message. */
    private Consumer<String> messageConsumer;

    /** Optional player display name. */
    private String playerName;

    /** Local mapper used for quick JSON sanity checks. */
    private final ObjectMapper mapper = new ObjectMapper();


    // ---------------------------------------------------------------------
    // Connection lifecycle
    // ---------------------------------------------------------------------


    /**
     * Opens the TCP connection to the server and starts the listener thread.
     *
     * @param host server host name or IP
     * @param port server port
     * @throws IOException if the socket cannot be opened
     */
    public void connect(String host, int port) throws IOException {
        socket = new Socket(host, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        listenerThread = new Thread(() -> {
            try {
                String msg;
                while ((msg = in.readLine()) != null) {
                    logger.fine("[Client] Eingehend: " + msg);

                    try {
                        if (msg.isBlank()) {
                            continue;
                        }

                        // Auto-acknowledge heartbeats, but do not forward them
                        if (msg.contains("\"messageType\":\"Alive\"")) {
                            out.println("{\"messageType\":\"Alive\",\"messageBody\":{}}");
                            continue; // Heartbeats nicht an den UI-Consumer weiterreichen
                        }

                        // Quick JSON sanity check
                        try {
                            mapper.readTree(msg);
                        } catch (Exception parseEx) {
                            logger.warning("[Client] Ungültiges JSON verworfen: " + parseEx.getMessage());
                            continue;
                        }

                        // Forward to consumer for further handling
                        if (messageConsumer != null) {
                            messageConsumer.accept(msg);
                        }

                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "Fehler beim Verarbeiten der Nachricht: " + e.getMessage(), e);
                    }
                }
                logger.info("[Client] Server hat die Verbindung geschlossen. ");
                disconnect();

            } catch (IOException e) {
                logger.log(Level.WARNING, "Verbindung getrennt oder unterbrochen: " + e.getMessage(), e);
                disconnect();
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    // ---------------------------------------------------------------------
    // Accessors
    // ---------------------------------------------------------------------

    /**
     * Sets the player's display name (purely client-side field).
     *
     * @param playerName name to display/use locally
     */
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    /**
     * @return current player name or {@code null} if not set
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * Sets a consumer that will be invoked for each non-heartbeat, valid JSON message.
     *
     * @param consumer callback to consume server messages
     */
    public void setMessageConsumer(Consumer<String> consumer) {
        this.messageConsumer = consumer;
    }


    // ---------------------------------------------------------------------
    // Message sending
    // ---------------------------------------------------------------------


    /**
     * Sends a raw user command string through the {@link UserInputHandler} for conversion
     * to a JSON protocol message, then writes it to the socket.
     *
     * @param message user input command (e.g. "/helloServer group true")
     */
    public void send(String message) {
        if (out != null && !socket.isClosed()) {
            try {
                String jsonMessage = userInputHandler.processInput(message);
                out.println(jsonMessage);
            } catch (IllegalArgumentException e) {
                logger.warning("[Client] Ungültiger Eingabebefehl: " + e.getMessage());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        } else {
            logger.warning("[Client] Senden fehlgeschlagen: Verbindung nicht aktiv. ");
        }
    }

    /**
     * Serializes and sends a {@link BaseMessage} instance.
     *
     * @param message message object to serialize and send
     */
    public void send(BaseMessage<?> message) {
        if (out != null && !socket.isClosed()) {
            try {
                String json = NetworkManager.serialize(message);
                out.println(json);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "[Client] Fehler beim Senden: " + e.getMessage(), e);
            }
        } else {
            logger.warning("[Client] Senden fehlgeschlagen: Verbindung nicht aktiv. ");
        }
    }

    /**
     * Sends a pre-built JSON string directly to the server without validation.
     *
     * @param json raw JSON to send
     */
    public void sendRaw(String json){
        if (out != null && !socket.isClosed()){
            out.println(json);
        } else {
            logger.warning("[Client] Senden fehlgeschlagen: Verbindung nicht aktiv");
        }
    }


    // ---------------------------------------------------------------------
    // Disconnect
    // ---------------------------------------------------------------------


    /**
     * Closes streams and socket, and logs closure. Safe to call multiple times.
     */
    public void disconnect() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) {
                socket.close();
                logger.info("[Client] Verbindung sauber getrennt. ");
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Fehler beim Schließen der Verbindung: " + e.getMessage(), e);
        }
    }
}
