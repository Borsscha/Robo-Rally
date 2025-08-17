package de.lmu.Board;

import de.lmu.cleverecousins.Position;
import de.lmu.cleverecousins.Robot;
import de.lmu.util.LogConfigurator;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tile that dispenses a limited amount of energy. Each activation gives the robot
 * one energy (until the internal counter reaches zero) and decrements the stock.
 */
public class EnergyTile extends BoardTile {

    private static final Logger logger = Logger.getLogger(EnergyTile.class.getName());

    static{
        LogConfigurator.configureRootLogger(Level.FINE);
    }

    /** Remaining energy units available on this tile. */
    private int energyCount;

    /**
     * Creates an energy tile.
     *
     * @param position     board coordinates
     * @param isOnBoard    protocol/serialization flag passed to the super class
     * @param initialCount initial number of energy units stored on the tile
     */
    public EnergyTile(Position position, String isOnBoard, int initialCount) {
        super(position, isOnBoard);
        this.energyCount = initialCount;
    }

    /**
     * Gives one energy to the robot if any is left, then decrements the counter.
     *
     * @param robot robot currently being processed
     */
    @Override
    public void activate(Robot robot) {
        logger.fine("EnergyTile: Aktueller Energie-Zähler = " + energyCount);
        if(energyCount > 0){
            energyCount--;
            logger.fine("EnergyTile: Robot hat Energie erhalten, verbleibend: " + energyCount);
        }
        logger.fine("EnergyTile: Keine Energie mehr verfügbar. ");
    }

    /**
     * @return literal type identifier {@code "Energy"}
     */
    @Override
    public String getType() {
        return "Energy";
    }

    /**
     * @return remaining energy units on this tile
     */
    public int getCount() {
        return energyCount;
    }
}
