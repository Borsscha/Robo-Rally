package de.lmu.cleverecousins.protocol.cheats;

public class CheatTurnBody {
    private String direction;

    public CheatTurnBody() {
        // Default-Konstruktor für Jackson
    }

    public CheatTurnBody(String direction) {
        this.direction = direction;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }
}
