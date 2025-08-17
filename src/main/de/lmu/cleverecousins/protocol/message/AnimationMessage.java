package de.lmu.cleverecousins.protocol.message;

import de.lmu.cleverecousins.protocol.BaseMessage;
import de.lmu.cleverecousins.protocol.messageBody.AnimationBody;

public class AnimationMessage extends BaseMessage<AnimationBody> {
    public AnimationMessage() {}
    public AnimationMessage(AnimationBody body) {
        super(body);
    }
}



