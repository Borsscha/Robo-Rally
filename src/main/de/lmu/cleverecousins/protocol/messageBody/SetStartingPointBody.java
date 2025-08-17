package de.lmu.cleverecousins.protocol.messageBody;

public class SetStartingPointBody {

    private int x;
    private  int y;

    public SetStartingPointBody(){
    }

    public SetStartingPointBody(int x, int y){
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY(){
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}

