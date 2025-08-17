package de.lmu.cleverecousins.protocol.message;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.lmu.cleverecousins.protocol.BaseMessage;
import de.lmu.cleverecousins.protocol.messageBody.ReplaceCardBody;

@JsonTypeName("ReplaceCard")
public class ReplaceCardMessage extends BaseMessage<ReplaceCardBody>{

    public ReplaceCardMessage(){
        super();
    }

    public ReplaceCardMessage(ReplaceCardBody body){
        super(body);
    }

    public ReplaceCardMessage(int register, String newCard, int clientID){
        super(new ReplaceCardBody(register, newCard, clientID));
    }
}

