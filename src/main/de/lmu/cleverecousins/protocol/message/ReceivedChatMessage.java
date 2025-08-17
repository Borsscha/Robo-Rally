package de.lmu.cleverecousins.protocol.message;

import de.lmu.cleverecousins.protocol.BaseMessage;
import de.lmu.cleverecousins.protocol.messageBody.ReceivedChatBody;

public class ReceivedChatMessage extends BaseMessage<ReceivedChatBody> {

    public ReceivedChatMessage() {};
    public ReceivedChatMessage(ReceivedChatBody body) {super(body);}
}

