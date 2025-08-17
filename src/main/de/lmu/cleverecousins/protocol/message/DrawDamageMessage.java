package de.lmu.cleverecousins.protocol.message;
import de.lmu.cleverecousins.protocol.BaseMessage;
import de.lmu.cleverecousins.protocol.messageBody.DrawDamageBody;

public class DrawDamageMessage extends BaseMessage<DrawDamageBody> {

    public DrawDamageMessage(){}

    public DrawDamageMessage(DrawDamageBody body){
        super(body);
    }
}

