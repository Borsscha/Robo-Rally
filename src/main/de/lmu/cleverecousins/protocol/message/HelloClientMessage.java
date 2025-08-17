package de.lmu.cleverecousins.protocol.message;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.lmu.cleverecousins.protocol.BaseMessage;
import de.lmu.cleverecousins.protocol.messageBody.HelloClientBody;

@JsonTypeName("HelloClient")

public class HelloClientMessage extends BaseMessage<HelloClientBody> {
    public HelloClientMessage() {};

    public HelloClientMessage(HelloClientBody body) {
        super(body);
    }
}

