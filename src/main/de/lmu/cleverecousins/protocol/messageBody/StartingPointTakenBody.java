package de.lmu.cleverecousins.protocol.messageBody;

public class StartingPointTakenBody {

    private int x;
    private int y;
    private  String direction;
    private int clientID;

    public StartingPointTakenBody(){
    }

    public StartingPointTakenBody(int x, int y, String direction, int clientID){
        this.x = x;
        this.y = y;
        this.direction = direction;
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

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public int getClientID() {
        return clientID;
    }

    public void setClientID(int clientID) {
        this.clientID = clientID;
    }
}
