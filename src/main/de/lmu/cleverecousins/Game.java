package de.lmu.cleverecousins;

import java.util.*;

/**
 * Enthält zentrale Spiel-Logik wie Spielerreihenfolge,
 * Phasensteuerung und Hilfsmethoden für die Setup- und Programmierphase.
 */
public class Game {

    /** Map of client IDs to {@link Player} instances. */
    private final Map<Integer, Player> players = new HashMap<>();

    /** List defining the fixed order in which players take turns. */
    private final List<Integer> playerOrder = new ArrayList<>();

    /**
     * Current game phase:
     * <ol>
     *   <li>0 = Setup</li>
     *   <li>1 = Card Selection</li>
     *   <li>2 = Programming</li>
     *   <li>3 = Execution</li>
     * </ol>
     */
    private int currentPhase = 0;

    /** Index within {@link #playerOrder} for tracking whose turn it is. */
    private int currentPlayerIndex = 0;

    /** Flag indicating whether the phase timer has been started. */
    private boolean timerStarted = false;

    /** Register pointer used during the programming phase. */
    private int currentRegister = 0;


    // --------------------------------------
    // Phase and Timer Accessors
    // --------------------------------------


    /**
     * Returns the index of the active register during programming.
     *
     * @return current register index (0-based)
     */
    public int getCurrentRegister() {
        return currentRegister;
    }

    /**
     * Sets the pointer for the active register during programming.
     *
     * @param index register index (0-based)
     */
    public void setCurrentRegister(int index) {
        this.currentRegister = index;
    }

    /**
     * Indicates if the phase timer has been initiated.
     *
     * @return {@code true} if timer started, {@code false} otherwise
     */
    public boolean hasTimerStarted() {
        return timerStarted;
    }

    /**
     * Updates the timer started flag for phase transitions.
     *
     * @param started {@code true} to mark timer as started
     */
    public void setTimerStarted(boolean started) {
        this.timerStarted = started;
    }

    /**
     * Retrieves the current game phase.
     *
     * @return integer code of current phase
     */
    public int getCurrentPhase() {
        return currentPhase;
    }

    /**
     * Sets the active game phase.
     *
     * @param phase numeric code representing the phase
     */
    public void setCurrentPhase(int phase) {
        this.currentPhase = phase;
    }


    // --------------------------------------
    // Player Management
    // --------------------------------------


    /**
     * Registers a new player in the game.
     *
     * @param player the {@link Player} to add
     */
    public void addPlayer(Player player) {
        players.put(player.getClientID(), player);
    }

    /**
     * Retrieves a player by their client ID.
     *
     * @param clientID unique identifier of the player
     * @return corresponding {@link Player}, or {@code null} if not found
     */
    public Player getPlayer(int clientID) {
        return players.get(clientID);
    }

    /**
     * Returns all registered players.
     *
     * @return collection of all {@link Player} instances
     */
    public Collection<Player> getAllPlayers() {
        return players.values();
    }

    /**
     * Finds the player controlling the specified robot.
     *
     * @param robot the {@link Robot} instance
     * @return {@link Player} controlling the robot, or {@code null} if none
     */
    public Player getPlayerByRobot(Robot robot) {
        for (Player p : getAllPlayers()) {
            if (p.getRobot() == robot) {
                return p;
            }
        }
        return null;
    }


    // --------------------------------------
    // Turn Order Handling
    // --------------------------------------


    /**
     * Adds a client ID to the fixed turn order.
     *
     * @param clientID client identifier to append
     */
    public void addToPlayerOrder(int clientID) {
        playerOrder.add(clientID);
    }

    /**
     * Provides an unmodifiable view of the turn order list.
     *
     * @return list of client IDs in turn order
     */
    public List<Integer> getPlayerOrder() {
        return Collections.unmodifiableList(playerOrder);
    }

    /**
     * Sets the current player pointer to the given client ID.
     *
     * @param clientId client ID to set as current
     * @throws IllegalArgumentException if the ID is not in turn order
     */
    public void setCurrentPlayer(int clientId) {
        int idx = playerOrder.indexOf(clientId);
        if (idx < 0) {
            throw new IllegalArgumentException("Client-ID nicht in playerOrder: " + clientId);
        }
        this.currentPlayerIndex = idx;
    }

    /**
     * Gets the client ID of the active player.
     *
     * @return client ID at {@code currentPlayerIndex}
     */
    public int getCurrentPlayerClientID() {
        return playerOrder.get(currentPlayerIndex);
    }

    /**
     * Retrieves the index of the active player in the turn order.
     *
     * @return index (0-based) within {@link #playerOrder}
     */
    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    /**
     * Calculates the index of the next player without changing state.
     *
     * @return next player index (current + 1)
     */
    public int getNextPlayerIndex() {
        return currentPlayerIndex + 1;
    }

    /**
     * Advances the pointer to the next player in turn order.
     */
    public void nextPlayer() {
        currentPlayerIndex++;
    }


    // --------------------------------------
    // Setup Phase Utilities
    // --------------------------------------


    /**
     * Moves to the next player for starting position selection.
     *
     * @throws IllegalStateException if all players have already chosen
     */
    public void advanceStartingPlayer() {
        if (currentPlayerIndex < playerOrder.size() - 1) {
            currentPlayerIndex++;
        } else {
            throw new IllegalStateException("Alle Spieler haben bereits gewählt!");
        }
    }

    /**
     * Retrieves the client ID of the next player selecting a start point,
     * without advancing the pointer.
     *
     * @return client ID of next starter
     * @throws IllegalStateException if there is no next player
     */
    public int getNextStartingPlayerClientID() {
        int next = currentPlayerIndex + 1;
        if (next < playerOrder.size()) {
            return playerOrder.get(next);
        }
        throw new IllegalStateException("Kein weiterer Spieler übrig!");
    }

    /**
     * Checks whether all players have chosen their starting positions.
     *
     * @return {@code true} if current pointer is at last player, {@code false} otherwise
     */
    public boolean allPlayersChoseStart() {
        return currentPlayerIndex >= playerOrder.size() - 1;
    }

    /**
     * Checks if all players have completed their programming registers,
     * indicating readiness for execution.
     *
     * @return {@code true} if every player's robot has all registers filled
     */
    public boolean allPlayersReadyForExecution() {
        for (Player p : getAllPlayers()) {
            if (!p.getRobot().allRegistersFilled()) {
                return false;
            }
        }
        return true;
    }
}
