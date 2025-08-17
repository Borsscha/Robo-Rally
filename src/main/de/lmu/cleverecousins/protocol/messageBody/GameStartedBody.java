package de.lmu.cleverecousins.protocol.messageBody;


import de.lmu.cleverecousins.protocol.MapTileDefinition;

import java.util.List;

public class GameStartedBody {
    private int energy;
    private List<List<List<MapTileDefinition>>> gameMap;

    public GameStartedBody() {}

    public int getEnergy() {

        return energy;
    }

    public void setEnergy(int energy) {

        this.energy = energy;
    }

    public List<List<List<MapTileDefinition>>> getGameMap() {

        return gameMap;
    }

    public void setGameMap(List<List<List<MapTileDefinition>>> gameMap) {

        this.gameMap = gameMap;
    }
}



