package de.lmu.cleverecousins.protocol.messageBody;

import java.util.List;

public class CardsYouGotNowBody {

    private List<String> cards;

    public CardsYouGotNowBody(){
    }

    public CardsYouGotNowBody(List<String> cards){
        this.cards = cards;
    }

    public List<String> getCards(){
        return cards;
    }

    public void setCards(List<String> cards){
        this.cards = cards;
    }
}

