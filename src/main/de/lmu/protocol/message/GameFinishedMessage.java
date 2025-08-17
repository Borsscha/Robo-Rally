package de.lmu.protocol.message;

import de.lmu.protocol.messageBody.GameFinishedBody;

public class GameFinishedMessage extends BaseMessage<GameFinishedBody> {

    public GameFinishedMessage(int clientID) {
        //super("GameFinished", new GameFinishedBody(clientID));
    }
}