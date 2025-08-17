package de.lmu.cleverecousins.protocol.message;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.lmu.cleverecousins.protocol.BaseMessage;
import de.lmu.cleverecousins.protocol.messageBody.CardPlayedBody;

@JsonTypeName("CardPlayed")
public class CardPlayedMessage extends BaseMessage<CardPlayedBody> {
    public  CardPlayedMessage() {}
    public CardPlayedMessage(CardPlayedBody body) { super(body);}
}

