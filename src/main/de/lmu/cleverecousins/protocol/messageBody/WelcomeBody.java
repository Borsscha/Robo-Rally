package de.lmu.cleverecousins.protocol.messageBody;

public class WelcomeBody {

    private int clientID;

    public WelcomeBody() {};
    public WelcomeBody (int clientID) {
        this.clientID = clientID;
    }

    public int getClientID() {
        return this.clientID;
    }

    public void setClientID(int id) {
        this.clientID = id;
    }
}

