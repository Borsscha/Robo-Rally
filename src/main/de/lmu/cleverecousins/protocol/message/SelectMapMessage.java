package de.lmu.cleverecousins.protocol.message;

import de.lmu.cleverecousins.protocol.BaseMessage;
import de.lmu.cleverecousins.protocol.messageBody.SelectMapBody;

public class SelectMapMessage extends BaseMessage<SelectMapBody> {

    public SelectMapMessage() {
        super(new SelectMapBody());
    }

    public SelectMapMessage(SelectMapBody body) {
        super(body);
    }

    // Falls du einen Konstruktor mit Liste und ClientID direkt möchtest,
    // kannst du diesen hinzufügen:

    public SelectMapMessage(java.util.List<String> maps, int allowedClientId) {
        super(new SelectMapBody(maps, allowedClientId));
    }
}





