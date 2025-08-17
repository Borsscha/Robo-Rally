package de.lmu.protocol.messageBody;

public class PlayerTurningBody {
    private int clientID;
    private String rotation;

    public PlayerTurningBody() {
        // Default constructor for Jackson
    }

    public PlayerTurningBody(int clientID, String rotation) {
        this.clientID = clientID;
        this.rotation = rotation;
    }

    public int getClientID() {
        return clientID;
    }

    public void setClientID(int clientID) {
        this.clientID = clientID;
    }

    public String getRotation() {
        return rotation;
    }

    public void setRotation(String rotation) {
        this.rotation = rotation;
    }
}

