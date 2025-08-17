package de.lmu.cleverecousins.protocol.message;


import com.fasterxml.jackson.annotation.JsonTypeName;
import de.lmu.cleverecousins.protocol.BaseMessage;
import de.lmu.cleverecousins.protocol.messageBody.MapSelectedBody;
@JsonTypeName("MapSelected")
public class MapSelectedMessage extends BaseMessage<MapSelectedBody> {

    public MapSelectedMessage() {}
    public MapSelectedMessage(MapSelectedBody body) {
        super(body);
    }
}

















