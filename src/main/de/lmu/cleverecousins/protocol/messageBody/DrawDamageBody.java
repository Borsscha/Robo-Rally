package de.lmu.cleverecousins.protocol.messageBody;

import java.util.List;

public class DrawDamageBody {

    private int clientID;
    private List<String> cards;


    public DrawDamageBody(){}

    public DrawDamageBody(int clientID, List<String> cards){
        this.clientID = clientID;
        this.cards = cards;
    }


    public int getClientID(){
        return clientID;
    }

    public void setClientID(int clientID){
        this.clientID = clientID;
    }

    public List<String> getCards(){
        return cards;
    }

    public void setCards(List<String> cards){
        this.cards = cards;
    }
}

