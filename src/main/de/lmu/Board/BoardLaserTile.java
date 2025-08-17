package de.lmu.Board;

import de.lmu.cleverecousins.Direction;
import de.lmu.cleverecousins.Position;
import de.lmu.cleverecousins.Robot;

import java.util.List;

/**
 * Tile that represents a board-mounted laser. It stores one or more firing orientations
 * and how many beams (strength) it emits in that direction.
 * <p>
 * The actual damage/animation logic should be implemented in {@link #activate(Robot)} or
 * elsewhere in the board processing pipeline.
 */
public class BoardLaserTile extends BoardTile {

    /** Directions in which this laser fires. */
    private final List<Direction> orientations;

    /** Number of laser beams (damage strength). */
    private final int count;

    /**
     * Creates a laser tile.
     *
     * @param position   board position of the tile
     * @param isOnBoard  protocol/serialization flag passed to the super constructor
     * @param orientations list of directions the laser fires towards
     * @param count      number of beams fired (damage per hit)
     */
    public BoardLaserTile(Position position, String isOnBoard , List<Direction> orientations, int count) {
        super(position, isOnBoard);
        this.orientations = orientations;
        this.count = count;
    }

    /**
     * @return orientations the laser fires in
     */
    public List<Direction> getOrientations() {
        return orientations;
    }

    /**
     * @return number of beams (damage)
     */
    public int getCount() {
        return count;
    }

    /**
     * Triggered when the tile activates for a robot. Implement laser hit logic here
     * if the activation phase processes tiles individually.
     *
     * @param robot robot currently being processed
     */
    @Override
    public void activate(Robot robot) {
    }

    /**
     * @return the tile type identifier: {@code "Laser"}
     */
    @Override
    public String getType() {
        return "Laser";
    }
}




