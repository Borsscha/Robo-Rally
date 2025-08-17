package de.lmu.cleverecousins.protocol.messageBody;

public class SelectedCardBody {

    private String card;
    private int register;

    public SelectedCardBody(){
    }

    public SelectedCardBody(String card, int register){
        this.card = card;
        this.register = register;
    }

    public String getCard() {
        return card;
    }

    public void setCard(String card) {
        this.card = card;
    }

    public int getRegister() {
        return register;
    }

    public void setRegister(int register) {
        this.register = register;
    }
}
