package de.lmu.cleverecousins.protocol.message;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.lmu.cleverecousins.protocol.BaseMessage;
import de.lmu.cleverecousins.protocol.messageBody.ActivePhaseBody;

@JsonTypeName("ActivePhase")
public class ActivePhaseMessage extends BaseMessage<ActivePhaseBody>{

    public ActivePhaseMessage() {
        super();
    }

    public ActivePhaseMessage(ActivePhaseBody body) {
        super(body);
    }

    public ActivePhaseMessage(int phase) {
        super(new ActivePhaseBody(phase));
    }
}

