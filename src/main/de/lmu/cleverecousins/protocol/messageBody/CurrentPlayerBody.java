package de.lmu.cleverecousins.protocol.messageBody;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CurrentPlayerBody {
    @JsonProperty("clientID")
    private int clientID;

    // No-arg-Konstruktor für Jackson
    public CurrentPlayerBody() {}

    public CurrentPlayerBody(int clientID){
        this.clientID = clientID;
    }

    public int getClientID(){
        return clientID;
    }

    public void setClientID(int clientID){
        this.clientID = clientID;
    }
}
