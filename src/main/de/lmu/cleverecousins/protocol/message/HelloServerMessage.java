package de.lmu.cleverecousins.protocol.message;

import de.lmu.cleverecousins.protocol.BaseMessage;
import de.lmu.cleverecousins.protocol.messageBody.HelloServerBody;
public class HelloServerMessage extends BaseMessage<HelloServerBody>{
    public HelloServerMessage() {}
    public HelloServerMessage(HelloServerBody body) {
        super(body);
    }

}

