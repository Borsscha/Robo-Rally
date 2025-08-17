package de.lmu.cleverecousins.protocol.message;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.lmu.cleverecousins.protocol.BaseMessage;
import de.lmu.cleverecousins.protocol.messageBody.CurrentPlayerBody;

@JsonTypeName("CurrentPlayer")

public class CurrentPlayerMessage extends BaseMessage<CurrentPlayerBody>{

    public CurrentPlayerMessage(){
        super();
    }

    public CurrentPlayerMessage(CurrentPlayerBody body){
        super(body);
    }

    public CurrentPlayerMessage(int clientID) {
        super(new CurrentPlayerBody(clientID));
    }
}

