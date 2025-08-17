package de.lmu.protocol.message;

import de.lmu.protocol.messageBody.RebootBody;

public class RebootMessage extends BaseMessage<RebootBody> {

    public RebootMessage(int clientID) {
        //super("Reboot", new RebootBody(clientID));
    }
}
