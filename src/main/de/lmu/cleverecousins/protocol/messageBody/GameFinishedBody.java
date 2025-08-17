package de.lmu.cleverecousins.protocol.messageBody;

public class GameFinishedBody {
    private int clientID;

    public GameFinishedBody() {}

    public GameFinishedBody(int clientID) {
        this.clientID = clientID;
    }

    public int getClientID() {
        return clientID;
    }

    public void setClientID(int clientID) {
        this.clientID = clientID;
    }
}


