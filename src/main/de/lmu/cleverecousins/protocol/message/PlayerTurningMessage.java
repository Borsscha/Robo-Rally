package de.lmu.cleverecousins.protocol.message;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.lmu.cleverecousins.protocol.BaseMessage;
import de.lmu.cleverecousins.protocol.messageBody.PlayerTurningBody;

@JsonTypeName("PlayerTurning")
public class PlayerTurningMessage extends BaseMessage<PlayerTurningBody> {
    public PlayerTurningMessage() {}
    public PlayerTurningMessage(PlayerTurningBody body) {
        super(body);
    }
}



