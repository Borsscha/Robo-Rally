package de.lmu.Board;

import de.lmu.cleverecousins.Position;
import de.lmu.cleverecousins.Robot;

/**
 * Tile that has no effect when activated. Serves as a placeholder for empty board fields.
 */
public class EmptyTile extends BoardTile {

    /**
     * Creates an empty tile at the given position.
     *
     * @param pos       board coordinates of this tile
     * @param isOnBoard protocol/serialization flag passed to the super class
     */
    public EmptyTile(Position pos, String isOnBoard) { super(pos, isOnBoard); }

    /**
     * No-op: empty tiles do nothing on activation.
     *
     * @param robot robot currently being processed
     */
    @Override
    public void activate(Robot robot) {
    }

    /**
     * @return the literal type string {@code "EMPTY"}
     */
    public String getType() { return "EMPTY"; }
}

