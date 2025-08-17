package de.lmu.cleverecousins.protocol.message;

import de.lmu.cleverecousins.protocol.BaseMessage;
import de.lmu.cleverecousins.protocol.messageBody.CurrentCardsBody;

import java.util.List;

public class CurrentCardsMessage extends BaseMessage<CurrentCardsBody>{

    public CurrentCardsMessage(){
        super();
    }

    public CurrentCardsMessage(CurrentCardsBody body){
        super(body);
    }

    public CurrentCardsMessage(List<CurrentCardsBody.ActiveCard> activeCardList){
        super(new CurrentCardsBody(activeCardList));
    }
}

