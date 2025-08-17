package de.lmu.cleverecousins.protocol.message;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.lmu.cleverecousins.protocol.BaseMessage;
import de.lmu.cleverecousins.protocol.messageBody.TimerEndedBody;

import java.util.List;

@JsonTypeName("TimerEnded")
public class TimerEndedMessage extends BaseMessage<TimerEndedBody>{

    public TimerEndedMessage(){
        super();
    }

    public TimerEndedMessage(TimerEndedBody body){
        super(body);
    }

    public TimerEndedMessage(List<Integer> clientIDs) {
        super(new TimerEndedBody(clientIDs));
    }
}

