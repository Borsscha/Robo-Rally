package de.lmu.cleverecousins.protocol.messageBody;

import java.util.List;

public class YourCardsBody {

    private List<String> cardsInHand;

    public YourCardsBody(){
    }

    public YourCardsBody(List<String> cardsInHand){
        this.cardsInHand = cardsInHand;
    }

    public List<String> getCardsInHand() {
        return cardsInHand;
    }

    public void setCardsInHand(List<String> cardsInHand) {
        this.cardsInHand = cardsInHand;
    }
}
