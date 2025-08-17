package de.lmu.cleverecousins.viewmodel;

import de.lmu.cleverecousins.AIClient;
import de.lmu.cleverecousins.Client;
import de.lmu.cleverecousins.ViewModel;
import de.lmu.util.LogConfigurator;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ViewModel for the login screen. It holds all input fields required to join a game
 * (group name, player name, robot ID, human/AI toggle) and exposes validation/enablement
 * state for the login button. It can also start an {@link AIClient} instead of a human client.
 * <p>
 * The class implements {@link ViewModel} but most chat/game related callbacks are no-ops
 * because they are not needed on the login screen.
 */
public class LoginViewModel implements ViewModel {

    private static final Logger logger = Logger.getLogger(LoginViewModel.class.getName());

    static {
        LogConfigurator.configureRootLogger(Level.FINE);
    }

    // ---------------- Input fields ----------------
    /** Group name entered by the user. */
    private final StringProperty groupName = new SimpleStringProperty("");

    /** Flag indicating whether this client is human (true) or AI (false). */
    private final BooleanProperty isHuman = new SimpleBooleanProperty(true);

    /** Player display name. */
    private final StringProperty playerName = new SimpleStringProperty("");

    /** Selected robot ID (nullable until chosen). */
    private final ObjectProperty<Integer> robotId = new SimpleObjectProperty<>(null);

    /** Derived flag to disable/enable the login action. */
    private final BooleanProperty loginDisabled = new SimpleBooleanProperty(true);

    /** Network client used for sending initial handshake messages. */
    private final Client client;

    /** Optional callback executed after a successful login. */
    private Runnable onLoginSuccess;

    /** IDs of robots already taken by other players (to be disabled in the UI). */
    private final ObservableList<Integer> usedRobots = FXCollections.observableArrayList();


    // ---------------------------------------------------------------------
    // Server data helpers
    // ---------------------------------------------------------------------


    /**
     * @return observable list of robot IDs already in use
     */
    public ObservableList<Integer> getUsedRobots() {
        return usedRobots;
    }

    /**
     * Replaces the list of taken robot IDs (typically from a server push).
     *
     * @param newUsedRobots list of robot IDs currently used by others
     */
    public void updateUsedRobots(List<Integer> newUsedRobots) {
        usedRobots.setAll(newUsedRobots);
    }


    // ---------------------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------------------


    /**
     * Creates the LoginViewModel and wires validation so the login button becomes
     * enabled only when all required fields are filled.
     *
     * @param client network client used to communicate with the server
     */
    public LoginViewModel(Client client) {
        this.client = client;

        BooleanBinding allValid = groupName.isNotEmpty()
                .and(playerName.isNotEmpty())
                .and(robotId.isNotNull());

        loginDisabled.bind(allValid.not());
    }

    /** @return bindable group name property */
    public StringProperty groupNameProperty() {
        return groupName;
    }

    /** @return bindable flag whether this client is human */
    public BooleanProperty isHumanProperty() {
        return isHuman;
    }

    /** @return bindable player name property */
    public StringProperty playerNameProperty() {
        return playerName;
    }

    /** @return bindable robot ID property */
    public ObjectProperty<Integer> robotIdProperty() {
        return robotId;
    }

    /** @return bindable flag to disable/enable the login button */
    public BooleanProperty loginDisabledProperty() {
        return loginDisabled;
    }

    /**
     * Registers a callback to run after the login handshake has been sent/setup.
     *
     * @param onLoginSuccess runnable invoked on success
     */
    public void setOnLoginSuccess(Runnable onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
    }


    // ---------------------------------------------------------------------
    // Login action
    // ---------------------------------------------------------------------


    /**
     * Sends the appropriate login sequence depending on the human/AI toggle.
     * <ul>
     *   <li>If AI: starts a new {@link AIClient} and disconnects this {@link Client}.</li>
     *   <li>If human: sends /helloServer and /playerValues via the existing client.</li>
     * </ul>
     * Invokes {@code onLoginSuccess} afterwards if set.
     */
    public void sendLogin() {
        boolean isHumanChecked = isHuman.get();
        boolean isAI = !isHumanChecked;

        if (isAI) {
            try {
                logger.info("[Login] KI wird gestartet...");
                AIClient ai = new AIClient("localhost", 12345, groupName.get(), playerName.get(), robotId.get());
                ai.start();
                client.disconnect();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "[Login] Fehler beim Starten der KI", e);
            }

            if (onLoginSuccess != null) {
                onLoginSuccess.run();
            }

        } else {
            client.send("/helloServer " + groupName.get() + " false");
            client.send("/playerValues " + playerName.get() + " " + robotId.get());

            if (onLoginSuccess != null) {
                onLoginSuccess.run();
            }
        }
    }

    // ---------------------------------------------------------------------
    // ViewModel interface (unused here)
    // ---------------------------------------------------------------------

    /** {@inheritDoc} (unused in login view) */
    @Override
    public void receiveChatMessage(String from, String message) {
        // Wird in LoginView nicht benötigt
    }

    /** {@inheritDoc} (unused in login view) */
    @Override
    public void receiveChatMessage(String from, String message, int fromId, boolean isPrivate) {
        // Wird in LoginView nicht benötigt
    }

    /** {@inheritDoc} (deprecated/no-op here) */
    @Override
    public void updatePlayerList(String name) {
        // Wird in LoginView nicht benötigt
    }

    /** {@inheritDoc} (no-op here) */
    @Override
    public void updatePlayerList(String name, int clientId) {
        // Wird in LoginView nicht benötigt
    }

    /**
     * Shows a system prompt in the login context (logs to console).
     *
     * @param message system text
     */
    @Override
    public void showSystemPrompt(String message) {//System.out.println("[System] " + message); // Optional: Logging
        logger.info("[System] " + message);
    }

    /** {@inheritDoc} (not relevant during login) */
    @Override
    public void setClientID(int clientID) {
        // In der Login-Phase nicht relevant
    }

    /** {@inheritDoc} (always -1 for login) */
    @Override
    public int getClientID() {
        return -1; // Login hat keine gültige Client-ID
    }
}

