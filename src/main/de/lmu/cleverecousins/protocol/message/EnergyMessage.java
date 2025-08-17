package de.lmu.cleverecousins.protocol.message;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.lmu.cleverecousins.protocol.BaseMessage;
import de.lmu.cleverecousins.protocol.messageBody.EnergyBody;

@JsonTypeName("Energy")
public class EnergyMessage extends BaseMessage<EnergyBody> {
    public EnergyMessage() {}
    public EnergyMessage(EnergyBody body) {
        super(body);
    }
}



