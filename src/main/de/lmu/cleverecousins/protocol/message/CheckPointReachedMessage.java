package de.lmu.cleverecousins.protocol.message;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.lmu.cleverecousins.protocol.BaseMessage;
import de.lmu.cleverecousins.protocol.messageBody.CheckPointReachedBody;

@JsonTypeName("CheckPointReached")
public class CheckPointReachedMessage extends BaseMessage<CheckPointReachedBody> {
    public CheckPointReachedMessage() {}
    public CheckPointReachedMessage(CheckPointReachedBody body) {
        super(body);
    }
}


