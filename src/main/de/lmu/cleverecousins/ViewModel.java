package de.lmu.cleverecousins;

/**
 * Abstraction for the UI/view layer. Implementations (e.g. LobbyViewModel, GameViewModel)
 * receive processed protocol events and update the presentation accordingly.
 */
public interface ViewModel {

    /**
     * Handles an incoming chat message.
     *
     * @param from    sender name
     * @param message message text
     */
    void receiveChatMessage(String from, String message);

    /**
     * Handles an incoming chat message with additional metadata.
     * Default implementation falls back to {@link #receiveChatMessage(String, String)}.
     *
     * @param from      sender name
     * @param message   message text
     * @param fromId    sender client ID
     * @param isPrivate whether the message is private
     */
    default void receiveChatMessage(String from, String message, int fromId, boolean isPrivate) {
        receiveChatMessage(from, message);
    }

    /**
     * Adds a player to the UI list.
     *
     * @param name player name
     * @deprecated Use {@link #updatePlayerList(String, int)} to include the client ID.
     */
    @Deprecated
    default void updatePlayerList(String name) {
        // Leere Standardimplementierung – nicht mehr aktiv verwendet
    }

    /**
     * Adds a player (with ID) to the UI list. Useful for private messaging, etc.
     *
     * @param name     player name
     * @param clientId player client ID
     */
    default void updatePlayerList(String name, int clientId) {
    }

    /**
     * Shows a system/status message to the user.
     *
     * @param message text to display
     */
    void showSystemPrompt(String message);

    /**
     * Supplies the VM with the local client's ID (for self-identification).
     *
     * @param clientID our own client ID
     */
    void setClientID(int clientID);

    /**
     * @return our own client ID
     */
    int getClientID();

    /**
     * Rotates a robot on the map (optional override).
     *
     * @param clientID client ID of the robot owner
     * @param rotation rotation keyword (e.g. \"clockwise\", \"counterclockwise\", \"uturn\")
     */
    default void rotateRobotOnMap(int clientID, String rotation) {
        // Kann leer bleiben – wird von GameViewModel oder LobbyViewModel überschrieben
    }
}



