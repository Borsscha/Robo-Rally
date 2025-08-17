package de.lmu.cleverecousins.protocol.message;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.lmu.cleverecousins.protocol.BaseMessage;
import de.lmu.cleverecousins.protocol.messageBody.SelectedDamageBody;

@JsonTypeName("SelectedDamage")

public class SelectedDamageMessage extends BaseMessage<SelectedDamageBody> {

    public SelectedDamageMessage(){}

    public SelectedDamageMessage(SelectedDamageBody body){
        super(body);
    }
}

