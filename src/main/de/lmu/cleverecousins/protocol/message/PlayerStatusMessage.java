package de.lmu.cleverecousins.protocol.message;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.lmu.cleverecousins.protocol.BaseMessage;
import de.lmu.cleverecousins.protocol.messageBody.PlayerStatusBody;

@JsonTypeName("PlayerStatus")
public class PlayerStatusMessage extends BaseMessage<PlayerStatusBody> {

    public PlayerStatusMessage(){};
    public PlayerStatusMessage(PlayerStatusBody body) {super(body);}
}

