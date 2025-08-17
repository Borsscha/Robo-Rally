package de.lmu.cleverecousins.protocol.messageBody;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ActivePhaseBody {
    @JsonProperty("phase")
    private int phase;

    public ActivePhaseBody(){
    }

    public ActivePhaseBody(int phase){
        this.phase = phase;
    }

    public int getPhase(){
        return phase;
    }

    public void setPhase(int phase) {
        this.phase = phase;
    }
}
