package de.lmu.protocol.message;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.lmu.protocol.message.BaseMessage;
import de.lmu.protocol.messageBody.GameStartedBody;

@JsonTypeName("GameStarted")


public class GameStartedMessage extends BaseMessage<GameStartedBody> {
   /* public GameStartedMessage() {
        super("GameStarted");
    }

    public GameStartedMessage(GameStartedBody body) {
        super("GameStarted", body);
    }*/
}

