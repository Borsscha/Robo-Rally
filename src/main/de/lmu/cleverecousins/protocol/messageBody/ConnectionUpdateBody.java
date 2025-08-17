package de.lmu.cleverecousins.protocol.messageBody;

public class ConnectionUpdateBody {

    private int clientID;
    private boolean isConnected;
    private String action;

    public ConnectionUpdateBody(){}

    public ConnectionUpdateBody(int clientID, boolean isConnected, String action){
        this.clientID = clientID;
        this.isConnected = isConnected;
        this.action = action;
    }

    public int getClientID(){
        return clientID;
    }

    public void setClientID(int clientID){
        this.clientID = clientID;
    }

    public boolean isConnected(){
        return isConnected;
    }

    public void setConnected(boolean connected){
        isConnected = connected;
    }

    public String getAction(){
        return action;
    }

    public void setAction(String action){
        this.action = action;
    }
}

