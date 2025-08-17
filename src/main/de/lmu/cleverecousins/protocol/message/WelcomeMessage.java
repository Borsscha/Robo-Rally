package de.lmu.cleverecousins.protocol.message;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.lmu.cleverecousins.protocol.BaseMessage;
import de.lmu.cleverecousins.protocol.messageBody.WelcomeBody;

@JsonTypeName("Welcome")
public class WelcomeMessage extends BaseMessage<WelcomeBody> {
    public WelcomeMessage() {}
    public WelcomeMessage(WelcomeBody body){
        super(body);
    }
}

