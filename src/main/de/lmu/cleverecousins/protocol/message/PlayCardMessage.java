package de.lmu.cleverecousins.protocol.message;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.lmu.cleverecousins.protocol.BaseMessage;
import de.lmu.cleverecousins.protocol.messageBody.PlayCardBody;

@JsonTypeName("PlayCard")
public class PlayCardMessage extends BaseMessage<PlayCardBody> {
    public PlayCardMessage() {};
    public PlayCardMessage(PlayCardBody body) { super(body);}
}



