package de.lmu.protocol.message;

import de.lmu.protocol.messageBody.CheckPointReachedBody;

public class CheckPointReachedMessage extends BaseMessage<CheckPointReachedBody> {

    public CheckPointReachedMessage(int clientID, int number) {
        //super("CheckPointReached", new CheckPointReachedBody(clientID, number));
    }
}
