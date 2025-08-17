package de.lmu.cleverecousins.protocol.message;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.lmu.cleverecousins.protocol.BaseMessage;
import de.lmu.cleverecousins.protocol.messageBody.TimerStartedBody;

@JsonTypeName("TimerStarted")
public class TimerStartedMessage extends BaseMessage<TimerStartedBody>{

    public TimerStartedMessage(){
        super(new TimerStartedBody());
    }
}

