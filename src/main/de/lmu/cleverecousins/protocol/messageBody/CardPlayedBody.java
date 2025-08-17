package de.lmu.cleverecousins.protocol.messageBody;

public class CardPlayedBody {
    private int clientId;
    private String card;

    public CardPlayedBody() {}
    public CardPlayedBody(int clientId, String card) {
        this.clientId = clientId;
        this.card = card;
    }

    public int getClientId() {
        return clientId;
    }
    public void setClientId(int clientId) {
        this.clientId = clientId;
    }
    public String getCard() {
        return card;
    }
    public void setCard(String card) {
        this.card = card;
    }

}

