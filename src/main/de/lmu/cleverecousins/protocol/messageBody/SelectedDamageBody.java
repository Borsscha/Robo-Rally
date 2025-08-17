package de.lmu.cleverecousins.protocol.messageBody;

import java.util.List;

public class SelectedDamageBody {

    private List<String> cards;

    public SelectedDamageBody(){}

    public SelectedDamageBody(List<String> cards){
        this.cards = cards;
    }

    public List<String> getCards(){
        return cards;
    }

    public void setCards(List<String>cards){
        this.cards = cards;
    }

}

