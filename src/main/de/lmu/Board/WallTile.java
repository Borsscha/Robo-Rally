package de.lmu.Board;

import de.lmu.cleverecousins.Direction;
import de.lmu.cleverecousins.Position;
import de.lmu.cleverecousins.Robot;

import java.util.List;

/**
 * Represents a wall tile on the game board.
 * A WallTile blocks movement in one or more specified directions.
 */
public class WallTile extends BoardTile {

    /** List of directions in which the wall blocks movement. */
    private final List<Direction> blockedSides;

    /**
     * Constructs a new WallTile at the specified position.
     *
     * @param position      the position of the tile on the board
     * @param isOnBoard     whether the tile is actively placed on the board (e.g., "true"/"false" as String)
     * @param blockedSides  list of directions that are blocked by the wall
     */
    public WallTile(Position position, String isOnBoard, List<Direction> blockedSides) {
        super(position, isOnBoard);
        this.blockedSides = blockedSides;
    }

    /**
     * Returns the list of directions in which this wall blocks movement.
     *
     * @return a list of {@link Direction}s representing blocked sides
     */
    public List<Direction> getBlockedSides() {
        return blockedSides;
    }

    /**
     * Defines the wall's behavior when a robot is on this tile.
     * For walls, this method does not perform any action, since walls are passive blockers.
     *
     * @param robot the robot on the tile (ignored)
     */
    @Override
    public void activate(Robot robot) {
        // Walls are passive and do not trigger effects.
    }

    /**
     * Returns the type name of the tile.
     *
     * @return the string "Wall"
     */
    @Override
    public String getType() {
        return "Wall";
    }
}

