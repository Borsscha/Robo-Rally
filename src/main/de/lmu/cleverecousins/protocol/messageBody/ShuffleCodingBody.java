package de.lmu.cleverecousins.protocol.messageBody;

public class ShuffleCodingBody {

    private int clientID;

    public ShuffleCodingBody(int clientID){
        this.clientID = clientID;
    }

    public int getClientID(){
        return clientID;
    }

    public void setClientID(int clientID){
        this.clientID = clientID;
    }
}
