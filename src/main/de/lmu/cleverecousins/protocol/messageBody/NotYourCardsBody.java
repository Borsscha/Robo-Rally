package de.lmu.cleverecousins.protocol.messageBody;

public class NotYourCardsBody {

    private int clientID;
    private int cardsInHand;

    public NotYourCardsBody(){
    }

    public NotYourCardsBody(int clientID, int cardsInHand){
        this.clientID = clientID;
        this.cardsInHand = cardsInHand;
    }

    public int getClientID() {
        return clientID;
    }

    public void setClientID(int clientID) {
        this.clientID = clientID;
    }

    public int getCardsInHand() {
        return cardsInHand;
    }

    public void setCardsInHand(int cardsInHand) {
        this.cardsInHand = cardsInHand;
    }
}
