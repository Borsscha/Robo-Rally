package de.lmu.cleverecousins.protocol.message;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.lmu.cleverecousins.protocol.BaseMessage;
import de.lmu.cleverecousins.protocol.messageBody.SelectionFinishedBody;

@JsonTypeName("SelectionFinished")
public class SelectionFinishedMessage extends BaseMessage<SelectionFinishedBody> {

    public SelectionFinishedMessage(){
        super();
    }

    public SelectionFinishedMessage(SelectionFinishedBody body){
        super(body);
    }

    public SelectionFinishedMessage(int clientID){
        super(new SelectionFinishedBody(clientID));
    }
}
