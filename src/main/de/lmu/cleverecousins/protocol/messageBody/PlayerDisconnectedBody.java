package de.lmu.cleverecousins.protocol.messageBody;

public class PlayerDisconnectedBody {
    private int clientID;
    private String playerName;

    public PlayerDisconnectedBody() {}

    public PlayerDisconnectedBody(int clientID, String playerName) {
        this.clientID = clientID;
        this.playerName = playerName;
    }

    public int getClientID() {
        return clientID;
    }

    public String getPlayerName() {
        return playerName;
    }
}
