package de.lmu.cleverecousins.protocol.messageBody;

import java.util.List;

public class TimerEndedBody {

    private List<Integer> clientIDs;

    public TimerEndedBody(){
    }

    public TimerEndedBody(List<Integer> clientIDs){
        this.clientIDs = clientIDs;
    }

    public List<Integer> getClientIDs() {
        return clientIDs;
    }

    public void setClientIDs(List<Integer> clientIDs) {
        this.clientIDs = clientIDs;
    }
}

