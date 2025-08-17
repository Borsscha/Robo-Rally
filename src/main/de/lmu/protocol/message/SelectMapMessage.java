package de.lmu.protocol.message;

import de.lmu.protocol.message.BaseMessage;
import de.lmu.protocol.messageBody.SelectMapBody;

import java.util.List;

public class SelectMapMessage extends BaseMessage<SelectMapBody> {

    public SelectMapMessage() {
        super();
    }

    public SelectMapMessage(SelectMapBody body) {
        super(body);
    }

    public SelectMapMessage(List<String> availableMaps) {
        super(new SelectMapBody(availableMaps));
    }
}
