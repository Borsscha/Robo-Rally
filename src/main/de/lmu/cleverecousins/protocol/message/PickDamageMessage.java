package de.lmu.cleverecousins.protocol.message;
import com.fasterxml.jackson.annotation.JsonTypeName;
import de.lmu.cleverecousins.protocol.BaseMessage;
import de.lmu.cleverecousins.protocol.messageBody.PickDamageBody;

@JsonTypeName("PickDamage")
public class PickDamageMessage extends BaseMessage<PickDamageBody> {

    public PickDamageMessage(){}

    public PickDamageMessage(PickDamageBody body){
        super(body);
    }
}

