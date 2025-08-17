package de.lmu.cleverecousins.protocol.messageBody;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PlayerStatusBody {
    private int clientID;

    @JsonProperty("ready")
    private boolean ready;

    public PlayerStatusBody() {};
    public PlayerStatusBody(int clientID, boolean ready) {
        this.clientID = clientID;
        this.ready = ready;
    }

    public int getClientID() {
        return this.clientID;
    }

    public void setClientID(int clientID) {
        this.clientID = clientID;
    }

    public boolean getReady() {
        return this.ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }
}
