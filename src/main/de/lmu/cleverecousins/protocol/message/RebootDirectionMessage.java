package de.lmu.cleverecousins.protocol.message;

import de.lmu.cleverecousins.protocol.BaseMessage;
import de.lmu.cleverecousins.protocol.messageBody.RebootDirectionBody;

public class RebootDirectionMessage extends BaseMessage<RebootDirectionBody> {
    public RebootDirectionMessage() {}
    public RebootDirectionMessage(RebootDirectionBody body) {
        super(body);
    }

}

