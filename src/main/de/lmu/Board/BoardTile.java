package de.lmu.Board;

import de.lmu.cleverecousins.Position;
import de.lmu.cleverecousins.Robot;

/**
 * Base class for all board tiles.
 * <p>
 * Each tile knows its fixed {@link Position} on the map and whether it is currently
 * considered to be “on board” (protocol/serialization flag). Subclasses must implement
 * their specific activation behavior and return a type identifier.
 */
public abstract class BoardTile {

    /** Grid position of this tile. */
    protected Position position;

    /** Protocol flag/string indicating on-board state (as received/sent). */
    protected String isOnBoard;

    /**
     * Creates a tile at a given position.
     *
     * @param position  board coordinates of the tile
     * @param isOnBoard protocol flag describing if the tile is on the board
     */
    public BoardTile(Position position, String isOnBoard) {
        this.position = position;
        this.isOnBoard = isOnBoard;
    }

    /**
     * @return this tile's board position
     */
    public Position getPosition() {
        return position;
    }

    /**
     * @return protocol/serialization flag for whether this tile is on the board
     */
    public String getIsOnBoard() {
        return isOnBoard;
    }

    /**
     * Executes this tile's effect on the given robot during the board activation phase.
     *
     * @param robot robot currently being processed
     */
    public abstract void activate(Robot robot);

    /**
     * @return short type identifier (e.g. \"Laser\", \"Antenna\", ...)
     */
    public abstract String getType();
}

