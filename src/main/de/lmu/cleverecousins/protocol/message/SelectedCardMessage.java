package de.lmu.cleverecousins.protocol.message;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.lmu.cleverecousins.protocol.BaseMessage;
import de.lmu.cleverecousins.protocol.messageBody.SelectedCardBody;

@JsonTypeName("SelectedCard")

public class SelectedCardMessage extends BaseMessage<SelectedCardBody>{

    public SelectedCardMessage(){
        super();
    }

    public SelectedCardMessage(SelectedCardBody body){
        super(body);
    }

    public SelectedCardMessage(String card, int register){
        super(new SelectedCardBody(card, register));
    }
}

