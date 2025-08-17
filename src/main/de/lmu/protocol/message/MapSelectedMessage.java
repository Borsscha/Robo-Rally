package de.lmu.protocol.message;

import de.lmu.protocol.message.BaseMessage;
import de.lmu.protocol.messageBody.MapSelectedBody;

public class MapSelectedMessage extends BaseMessage<MapSelectedBody> {
    public MapSelectedMessage() {}
    public MapSelectedMessage(MapSelectedBody body) {
        super(body);
    }
}


