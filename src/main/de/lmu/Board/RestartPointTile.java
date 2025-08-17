package de.lmu.Board;

import de.lmu.cleverecousins.Position;
import de.lmu.cleverecousins.Robot;

/**
 * Represents a restart point tile (Reboot Tile) on the game board.
 * When a robot falls off the board, it is rebooted and placed on this tile.
 */
public class RestartPointTile extends BoardTile {

    /**
     * Constructs a RestartPointTile at the given position.
     *
     * @param position   the tile's position on the board
     * @param isOnBoard  whether the tile is considered to be placed on the board
     */
    public RestartPointTile(Position position, String isOnBoard) {
        super(position, isOnBoard);
    }

    /**
     * Defines the effect when a robot stands on this tile.
     * A RestartPointTile has no active effect on robots during the activation phase.
     *
     * @param robot the robot currently on the tile
     */
    @Override
    public void activate(Robot robot) {
        // Reboot point has no activation effect on robot.
    }

    /**
     * Returns the type of this tile.
     *
     * @return the string "RestartPoint"
     */
    @Override
    public String getType() {
        return "RestartPoint";
    }
}

