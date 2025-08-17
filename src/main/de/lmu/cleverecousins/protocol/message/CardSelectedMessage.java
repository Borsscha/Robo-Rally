package de.lmu.cleverecousins.protocol.message;

import de.lmu.cleverecousins.protocol.BaseMessage;
import de.lmu.cleverecousins.protocol.messageBody.CardSelectedBody;

public class CardSelectedMessage extends BaseMessage<CardSelectedBody>{

    public CardSelectedMessage(){
        super();
    }

    public CardSelectedMessage(CardSelectedBody body){
        super(body);
    }

    public CardSelectedMessage(int clientID, int register, boolean filled){
        super(new CardSelectedBody(clientID, register, filled));
    }
}

