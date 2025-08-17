package de.lmu.cleverecousins.protocol.messageBody;

import java.util.List;

public class UsedRobotsBody {
    private List<Integer> usedRobots;

    // Leerer Konstruktor für Jackson
    public UsedRobotsBody() {}

    public UsedRobotsBody(List<Integer> usedRobots) {
        this.usedRobots = usedRobots;
    }

    public List<Integer> getUsedRobots() {
        return usedRobots;
    }

    public void setUsedRobots(List<Integer> usedRobots) {
        this.usedRobots = usedRobots;
    }
}

