package de.lmu.cleverecousins.protocol.message;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.lmu.cleverecousins.protocol.BaseMessage;
import de.lmu.cleverecousins.protocol.messageBody.CardsYouGotNowBody;

import java.util.List;

@JsonTypeName("CardsYouGotNow")
public class CardsYouGotNowMessage extends BaseMessage<CardsYouGotNowBody> {

    public CardsYouGotNowMessage(){
        super();
    }

    public CardsYouGotNowMessage(CardsYouGotNowBody body){
        super(body);
    }

    public CardsYouGotNowMessage(List<String> cards){
        super(new CardsYouGotNowBody(cards));
    }
}

