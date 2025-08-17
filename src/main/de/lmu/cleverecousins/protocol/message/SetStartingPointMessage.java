package de.lmu.cleverecousins.protocol.message;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.lmu.cleverecousins.protocol.BaseMessage;
import de.lmu.cleverecousins.protocol.messageBody.SetStartingPointBody;

@JsonTypeName("SetStartingPoint")  // ✏️ sorgt dafür, dass messageType="SetStartingPoint" im JSON steht
public class SetStartingPointMessage extends BaseMessage<SetStartingPointBody> {

    public SetStartingPointMessage() {
        super();
    }

    public SetStartingPointMessage(SetStartingPointBody body) {
        super(body);
    }

    public SetStartingPointMessage(int x, int y) {
        super(new SetStartingPointBody(x, y));
    }
}
