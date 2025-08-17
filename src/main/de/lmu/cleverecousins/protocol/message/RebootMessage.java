package de.lmu.cleverecousins.protocol.message;

import de.lmu.cleverecousins.protocol.BaseMessage;
import de.lmu.cleverecousins.protocol.messageBody.RebootBody;

public class RebootMessage extends BaseMessage<RebootBody> {
    public RebootMessage() {}
    public RebootMessage(RebootBody body) {
        super(body);
    }
}



