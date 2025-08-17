package de.lmu.cleverecousins.protocol.message;

import de.lmu.cleverecousins.protocol.BaseMessage;
import de.lmu.cleverecousins.protocol.messageBody.UsedRobotsBody;

public class UsedRobotsMessage extends BaseMessage<UsedRobotsBody> {
    public UsedRobotsMessage() {
        super();
    }

    public UsedRobotsMessage(UsedRobotsBody body) {
        super(body);
    }
}
