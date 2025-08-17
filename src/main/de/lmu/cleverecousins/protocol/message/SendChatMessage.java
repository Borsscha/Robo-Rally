package de.lmu.cleverecousins.protocol.message;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.lmu.cleverecousins.protocol.BaseMessage;
import de.lmu.cleverecousins.protocol.messageBody.SendChatBody;

@JsonTypeName("SendChat")
public class SendChatMessage extends BaseMessage<SendChatBody> {
    public SendChatMessage() {};
    public SendChatMessage(SendChatBody body) {super(body);}
}

