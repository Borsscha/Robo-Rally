package de.lmu.cleverecousins.protocol.messageBody;

public class PlayCardBody {
    private String card;

    public PlayCardBody() {}

    public PlayCardBody(String card) {
        this.card = card;
    }

    public String getCard() {
        return card;
    }
    public void setCard(String card) {
        this.card = card;
    }

}
