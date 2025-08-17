package de.lmu.protocol.messageBody;

public class EnergyBody {
    private int clientID;
    private int count;
    private String source;

    public EnergyBody() {}

    public EnergyBody(int clientID, int count, String source) {
        this.clientID = clientID;
        this.count = count;
        this.source = source;
    }

    public int getClientID() {
        return clientID;
    }

    public void setClientID(int clientID) {
        this.clientID = clientID;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
