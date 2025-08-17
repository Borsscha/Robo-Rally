package de.lmu.protocol.messageBody;

public class MovementBody {
    private int clientID;
    private int x;
    private int y;

    public MovementBody() {
        // Default constructor for JSON deserialization
    }

    public MovementBody(int clientID, int x, int y) {
        this.clientID = clientID;
        this.x = x;
        this.y = y;
    }

    public int getClientID() {
        return clientID;
    }

    public void setClientID(int clientID) {
        this.clientID = clientID;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}
