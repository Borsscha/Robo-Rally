package de.lmu.cleverecousins.protocol.messageBody;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SetStatusBody {

    @JsonProperty("ready")
    private boolean ready;

    public SetStatusBody() {};
    public SetStatusBody(boolean ready) {
        this.ready = ready;
    }

    public boolean getReady() {
        return this.ready;
    }
    public void setReady(boolean ready) {
        this.ready = ready;
    }
}

