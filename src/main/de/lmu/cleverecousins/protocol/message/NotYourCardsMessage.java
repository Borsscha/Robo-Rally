package de.lmu.cleverecousins.protocol.message;

import de.lmu.cleverecousins.protocol.BaseMessage;
import de.lmu.cleverecousins.protocol.messageBody.NotYourCardsBody;

public class NotYourCardsMessage extends BaseMessage<NotYourCardsBody>{

    public NotYourCardsMessage(){
        super();
    }

    public NotYourCardsMessage(NotYourCardsBody body){
        super(body);
    }

    public NotYourCardsMessage(int clientID, int cardsInHand){
        super(new NotYourCardsBody(clientID, cardsInHand));
    }
}

