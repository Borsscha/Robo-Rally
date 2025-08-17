package de.lmu.cleverecousins.protocol.message;

import de.lmu.cleverecousins.protocol.BaseMessage;
import de.lmu.cleverecousins.protocol.messageBody.RobotPositionBody;

public class RobotPositionMessage extends BaseMessage<RobotPositionBody> {
    public RobotPositionMessage(RobotPositionBody body) {
        super(body);
    }
}
