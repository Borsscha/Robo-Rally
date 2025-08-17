package de.lmu.cleverecousins.protocol.messageBody;
import java.util.List;

public class CurrentCardsBody {
    private List<ActiveCard> activeCards;

    public CurrentCardsBody(){
    }

    public CurrentCardsBody(List<ActiveCard> activeCards){
        this.activeCards = activeCards;
    }

    public List<ActiveCard> getActiveCards(){
        return activeCards;
    }

    public void setActiveCards(List<ActiveCard> activeCards){
        this.activeCards = activeCards;
    }


    //Inner class ActiveCard
    public static class ActiveCard{
        private int clientID;
        private String card;

        public ActiveCard(){
        }

        public ActiveCard(int clientID, String card){
            this.clientID = clientID;
            this.card = card;
        }

        public int getClientID() {
            return clientID;
        }

        public void setClientID(int clientID) {
            this.clientID = clientID;
        }

        public String getCard() {
            return card;
        }

        public void setCard(String card) {
            this.card = card;
        }
    }
}
