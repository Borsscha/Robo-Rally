package de.lmu.cleverecousins.protocol.message;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.lmu.cleverecousins.protocol.BaseMessage;
import de.lmu.cleverecousins.protocol.messageBody.SetStatusBody;

@JsonTypeName("SetStatus")
public class SetStatusMessage extends BaseMessage<SetStatusBody>{
    public SetStatusMessage() {};
    public SetStatusMessage(SetStatusBody body) {super(body);}

}

