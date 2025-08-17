package de.lmu.cleverecousins.protocol.message;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.lmu.cleverecousins.protocol.BaseMessage;
import de.lmu.cleverecousins.protocol.messageBody.GameFinishedBody;

@JsonTypeName("GameFinished")
public class GameFinishedMessage extends BaseMessage<GameFinishedBody> {
    public GameFinishedMessage() {}
    public GameFinishedMessage(GameFinishedBody body) {
        super(body);
    }
}


