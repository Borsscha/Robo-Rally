package de.lmu.cleverecousins.protocol.message;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.lmu.cleverecousins.protocol.BaseMessage;
import de.lmu.cleverecousins.protocol.messageBody.GameStartedBody;

@JsonTypeName("GameStarted")


public class GameStartedMessage extends BaseMessage<GameStartedBody> {
    public GameStartedMessage() {}

    public GameStartedMessage(GameStartedBody body) {
        super(body);
    }
}







