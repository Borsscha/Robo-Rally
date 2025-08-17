package de.lmu.cleverecousins.protocol.messageBody;

public class CardSelectedBody {

    private int clientID;
    private int register;
    private boolean filled;

    public CardSelectedBody(){
    }

    public CardSelectedBody(int clientID, int register, boolean filled){
        this.clientID = clientID;
        this.register = register;
        this.filled = filled;
    }

    public int getClientID() {
        return clientID;
    }

    public void setClientID(int clientID) {
        this.clientID = clientID;
    }

    public int getRegister() {
        return register;
    }

    public void setRegister(int register) {
        this.register = register;
    }

    public boolean isFilled() {
        return filled;
    }

    public void setFilled(boolean filled) {
        this.filled = filled;
    }
}

