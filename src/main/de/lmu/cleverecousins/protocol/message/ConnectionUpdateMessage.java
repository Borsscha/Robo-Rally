package de.lmu.cleverecousins.protocol.message;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.lmu.cleverecousins.protocol.BaseMessage;
import de.lmu.cleverecousins.protocol.messageBody.ConnectionUpdateBody;

@JsonTypeName("ConnectionUpdate")

public class ConnectionUpdateMessage extends BaseMessage<ConnectionUpdateBody> {

    public ConnectionUpdateMessage(){}

    public ConnectionUpdateMessage(ConnectionUpdateBody body){
        super(body);
    }
}

