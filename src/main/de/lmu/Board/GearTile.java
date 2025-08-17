package de.lmu.Board;

import de.lmu.cleverecousins.Position;
import de.lmu.cleverecousins.Robot;

/**
 * Represents a gear tile on the game board.
 * <p>
 * When activated, this tile rotates the robot either clockwise or counterclockwise
 * depending on its configuration.
 */
public class GearTile extends BoardTile {

    /** Indicates the rotation direction: true = clockwise, false = counterclockwise */
    private final boolean clockwise;

    /**
     * Constructs a new GearTile at the specified position.
     *
     * @param position    the position of the tile on the game board
     * @param isOnBoard   flag indicating whether this tile is shown on the board
     * @param clockwise   true if the gear rotates clockwise, false for counterclockwise
     */
    public GearTile(Position position, String isOnBoard, boolean clockwise) {
        super(position, isOnBoard);
        this.clockwise = clockwise;
    }

    /**
     * Returns whether the gear rotates clockwise.
     *
     * @return true if the gear is clockwise, false if counterclockwise
     */
    public boolean isClockwise() {
        return clockwise;
    }

    /**
     * Rotates the robot standing on this tile according to the gear's direction.
     *
     * @param robot the robot currently on this tile
     */
    @Override
    public void activate(Robot robot) {
        if (clockwise) {
            robot.rotateClockwise();
        } else {
            robot.rotateCounterclockwise();
        }
    }

    /**
     * Returns the type of this tile.
     *
     * @return the string "Gear"
     */
    @Override
    public String getType() {
        return "Gear";
    }
}

