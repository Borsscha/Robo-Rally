package de.lmu.protocol.message;

import de.lmu.protocol.messageBody.PlayerTurningBody;

public class PlayerTurningMessage extends BaseMessage<PlayerTurningBody> {

    public PlayerTurningMessage(int clientID, String rotation) {
        //super("PlayerTurning", new PlayerTurningBody(clientID, rotation));
    }
}
