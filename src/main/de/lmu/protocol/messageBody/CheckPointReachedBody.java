package de.lmu.protocol.messageBody;

public class CheckPointReachedBody {
    private int clientID;
    private int number;

    public CheckPointReachedBody() {}

    public CheckPointReachedBody(int clientID, int number) {
        this.clientID = clientID;
        this.number = number;
    }

    public int getClientID() {
        return clientID;
    }

    public void setClientID(int clientID) {
        this.clientID = clientID;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
