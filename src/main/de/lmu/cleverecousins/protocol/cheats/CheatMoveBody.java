package de.lmu.cleverecousins.protocol.cheats;

public class CheatMoveBody {
    private int steps;

    public CheatMoveBody() {
        // Default-Konstruktor für Jackson
    }

    public CheatMoveBody(int steps) {
        this.steps = steps;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }
}
