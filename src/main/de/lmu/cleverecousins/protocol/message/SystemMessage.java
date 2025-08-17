package de.lmu.cleverecousins.protocol.message;

import de.lmu.cleverecousins.protocol.BaseMessage;
import de.lmu.cleverecousins.protocol.messageBody.SystemBody;

public class SystemMessage extends BaseMessage<SystemBody> {

    public SystemMessage() {
        super(new SystemBody());
    }

    public SystemMessage(SystemBody body) {
        super(body);
    }

    public SystemMessage(String message) {
        super(new SystemBody(message));
    }
}
