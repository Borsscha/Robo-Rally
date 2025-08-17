package de.lmu.protocol.message;

import de.lmu.protocol.messageBody.MovementBody;

public class MovementMessage extends BaseMessage<MovementBody> {

    public MovementMessage(int clientID, int x, int y) {
        //super("Movement", new MovementBody(clientID, x, y));
    }
}

