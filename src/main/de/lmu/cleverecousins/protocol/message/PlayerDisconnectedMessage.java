package de.lmu.cleverecousins.protocol.message;

import de.lmu.cleverecousins.protocol.BaseMessage;
import de.lmu.cleverecousins.protocol.messageBody.PlayerDisconnectedBody;

public class PlayerDisconnectedMessage extends BaseMessage<PlayerDisconnectedBody> {
    public PlayerDisconnectedMessage(PlayerDisconnectedBody body) {
        super(body);
    }
}
