package de.lmu.Board;


import de.lmu.cleverecousins.Position;
import de.lmu.cleverecousins.Robot;

/**
 * Represents a pit tile on the game board.
 * <p>
 * A pit tile causes the robot to be destroyed when it enters this tile.
 * This is typically treated as the robot falling off the board.
 */
public class PitTile extends BoardTile {

    /**
     * Constructs a new PitTile at the given position.
     *
     * @param position   the coordinates of the tile on the game board
     * @param isOnBoard  flag indicating whether this tile is shown on the board
     */
    public PitTile(Position position, String isOnBoard) {

        super(position, isOnBoard);
    }

    /**
     * Activates the pit tile's effect on the robot.
     * The robot is immediately marked as destroyed.
     *
     * @param robot the robot currently on this tile
     */
    @Override
    public void activate(Robot robot) {
        robot.setDestroyed(true);
    }

    /**
     * Returns the type of this board tile.
     *
     * @return the string "Pit"
     */
    @Override
    public String getType() {
        return "Pit";
    }
}

