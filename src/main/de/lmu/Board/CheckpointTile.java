package de.lmu.Board;

import de.lmu.cleverecousins.Position;
import de.lmu.cleverecousins.Robot;
import de.lmu.util.LogConfigurator;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tile representing a checkpoint on the board.
 * When a robot whose next required checkpoint number matches this tile's {@code count}
 * steps on (or is activated on) it, the robot is marked as having reached that checkpoint.
 */
public class CheckpointTile extends BoardTile {

    private static final Logger logger = Logger.getLogger(CheckpointTile.class.getName());

    static {
        LogConfigurator.configureRootLogger(Level.FINE);
    }

    /** Sequence number of this checkpoint (e.g. 1, 2, 3...). */
    private final int count;

    /**
     * Creates a checkpoint tile.
     *
     * @param position  board coordinates of the tile
     * @param isOnBoard protocol/serialization flag passed to the super class
     * @param count     checkpoint index this tile represents
     */
    public CheckpointTile(Position position, String isOnBoard, int count) {
        super(position, isOnBoard);
        this.count = count;
    }

    /** @return this checkpoint's index/number */
    public int getCount() {
        return count;
    }

    /**
     * Activates the tile for the given robot. If the robot's next checkpoint equals this tile's
     * {@code count}, it is credited for reaching it.
     *
     * @param robot robot currently being processed
     */
    @Override
    public void activate(Robot robot) {
        if (robot.getNextCheckpoint() == this.count) {
            robot.reachedCheckpoint(this.count);
            logger.fine("[CheckpointTile] Robot hat Checkpoint " + this.count + " erreicht. ");
        }
    }

    /**
     * @return type identifier string {@code "CheckPoint"}
     */
    @Override
    public String getType() {
        return "CheckPoint";
    }
}
