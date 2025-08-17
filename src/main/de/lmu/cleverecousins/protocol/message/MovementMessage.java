package de.lmu.cleverecousins.protocol.message;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.lmu.cleverecousins.protocol.BaseMessage;
import de.lmu.cleverecousins.protocol.messageBody.MovementBody;

@JsonTypeName("Movement")
public class MovementMessage extends BaseMessage<MovementBody> {
    public MovementMessage(MovementBody body) {
        super(body);
    }
}




