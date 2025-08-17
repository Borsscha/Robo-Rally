package de.lmu.protocol.messageBody;

public class RebootBody {
    private int clientID;

    public RebootBody() {}

    public RebootBody(int clientID) {
        this.clientID = clientID;
    }

    public int getClientID() {
        return clientID;
    }

    public void setClientID(int clientID) {
        this.clientID = clientID;
    }
}
