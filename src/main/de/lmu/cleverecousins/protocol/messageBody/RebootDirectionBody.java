package de.lmu.cleverecousins.protocol.messageBody;

public class RebootDirectionBody {
    private String direction;

    public RebootDirectionBody() {}
    public RebootDirectionBody(String direction) {
        this.direction = direction;
    }

    public String getDirection() {
        return direction;
    }
    public void setDirection(String direction) {
        this.direction = direction;
    }
}

