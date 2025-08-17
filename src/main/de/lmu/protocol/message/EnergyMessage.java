package de.lmu.protocol.message;

import de.lmu.protocol.messageBody.EnergyBody;

public class EnergyMessage extends BaseMessage<EnergyBody> {

    public EnergyMessage(int clientID, int count, String source) {
        //super("Energy", new EnergyBody(clientID, count, source));
    }
}
