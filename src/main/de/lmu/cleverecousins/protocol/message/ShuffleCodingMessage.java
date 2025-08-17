package de.lmu.cleverecousins.protocol.message;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.lmu.cleverecousins.protocol.BaseMessage;
import de.lmu.cleverecousins.protocol.messageBody.ShuffleCodingBody;

@JsonTypeName("ShuffleCoding")
public class ShuffleCodingMessage extends BaseMessage<ShuffleCodingBody>{

    public ShuffleCodingMessage(){
        super();
    }

    public ShuffleCodingMessage(ShuffleCodingBody body){
        super(body);
    }

    public ShuffleCodingMessage(int clientID) {
        super(new ShuffleCodingBody(clientID));
    }
}

