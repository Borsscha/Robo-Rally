package de.lmu.cleverecousins.protocol.messageBody;

public class SelectionFinishedBody {

    int clientID;

    public SelectionFinishedBody(){
    }

    public SelectionFinishedBody(int clientID){
        this.clientID = clientID;
    }

    public int getClientID(){
        return clientID;
    }

    public void setClientID(int clientID) {
        this.clientID = clientID;
    }
}

