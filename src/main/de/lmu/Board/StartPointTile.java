package de.lmu.Board;

import de.lmu.cleverecousins.Position;
import de.lmu.cleverecousins.Robot;

/**
 * Represents a starting point tile on the game board.
 * A StartPointTile marks a location where a robot begins the game.
 */
public class StartPointTile extends BoardTile {

    /**
     * Constructs a StartPointTile at the specified position.
     *
     * @param position   the position of the tile on the board
     * @param isOnBoard  whether the tile is placed on the board (e.g., "true"/"false" as String)
     */
    public StartPointTile(Position position, String isOnBoard)
    {
        super(position, isOnBoard);
    }

    /**
     * Defines the behavior when a robot is on this tile.
     * For a StartPointTile, no active effect is triggered.
     *
     * @param robot the robot currently on the tile
     */
    @Override
    public void activate(Robot robot) {
        // No effect for start point tiles.
    }

    /**
     * Returns the type of this tile.
     *
     * @return the string "StartPoint"
     */
    @Override
    public String getType() {
        return "StartPoint";
    }
}
