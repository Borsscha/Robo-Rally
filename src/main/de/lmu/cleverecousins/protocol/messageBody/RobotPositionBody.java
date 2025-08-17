package de.lmu.cleverecousins.protocol.messageBody;

public class RobotPositionBody {
    private int clientID;
    private int x;
    private int y;
    private String direction;

    public RobotPositionBody() {}

    public RobotPositionBody(int clientID, int x, int y, String direction) {
        this.clientID = clientID;
        this.x = x;
        this.y = y;
        this.direction = direction;
    }

    public int getClientID() { return clientID; }
    public int getX() { return x; }
    public int getY() { return y; }
    public String getDirection() { return direction; }

    public void setClientID(int clientID) { this.clientID = clientID; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public void setDirection(String direction) { this.direction = direction; }
}
