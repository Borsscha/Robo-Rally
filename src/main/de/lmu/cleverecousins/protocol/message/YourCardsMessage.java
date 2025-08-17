package de.lmu.cleverecousins.protocol.message;

import de.lmu.cleverecousins.protocol.BaseMessage;
import de.lmu.cleverecousins.protocol.messageBody.YourCardsBody;

import java.util.List;

public class YourCardsMessage extends BaseMessage<YourCardsBody>{

    public YourCardsMessage(){
        super();
    }

    public YourCardsMessage(YourCardsBody body){
        super(body);
    }

    public YourCardsMessage(List<String> cardsInHand){
        super(new YourCardsBody(cardsInHand));
    }
}

