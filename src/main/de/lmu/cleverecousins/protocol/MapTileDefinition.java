package de.lmu.cleverecousins.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Represents the definition of a single tile on the game board.
 * <p>
 * This class is typically used for parsing or generating tile information from or to JSON
 * as part of the game board layout. It contains metadata about tile type, position, orientation,
 * speed (e.g., for conveyor belts), register triggers, rotation, and more.
 */
public class MapTileDefinition {

    /** The type of the tile (e.g., "Wall", "Laser", "Pit", etc.). */
    private String type;

    /** The name of the board this tile belongs to (optional). */
    private String board;

    /** A list of orientations associated with the tile (e.g., "top", "right"). */
    private List<String> orientations;

    /** Speed value used for tiles like conveyor belts. */
    private Integer speed;

    /** The registers that activate this tile, if any (e.g., for pushers). */
    private List<Integer> registers;

    /** Number of elements (e.g., lasers) this tile contains. */
    private Integer count;

    /** The rotation direction for tiles like rotators (e.g., "clockwise", "counterclockwise"). */
    private String rotation;

    /** Indicates whether this tile is currently placed on the board. */
    @JsonProperty("isOnBoard")
    private String isOnBoard;

    /** The x-coordinate of the tile on the board grid. */
    private int x;

    /** The y-coordinate of the tile on the board grid. */
    private int y;

    /**
     * Default constructor required for JSON deserialization.
     */
    public MapTileDefinition() {}

    /**
     * Constructs a MapTileDefinition with a specified position and type.
     *
     * @param x    the x-coordinate of the tile on the map
     * @param y    the y-coordinate of the tile on the map
     * @param type the type of the tile (e.g., "Wall", "Laser", "Pit")
     */
    public MapTileDefinition(int x, int y, String type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    /**
     * Sets a single orientation value (used when only one orientation is specified).
     *
     * @param orientation a single orientation value (e.g., "top")
     */
    @JsonProperty("orientation")
    public void setOrientation(String orientation) {
        this.orientations = List.of(orientation);
    }

    /**
     * Gets the isOnBoard flag.
     *
     * @return a string indicating if the tile is on the board
     */
    public String getOnBoard() {
        return this.isOnBoard;
    }

    /**
     * Sets whether the tile is placed on the board.
     *
     * @param isOnBoard a string (e.g., "true" or "false")
     */
    public void setOnBoard(String isOnBoard) {
        this.isOnBoard = isOnBoard;
    }

    /**
     * Gets the tile type (e.g., "Wall", "Laser", "Pit").
     *
     * @return the type of the tile
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the tile type (e.g., "Wall", "Laser", "Pit").
     *
     * @param type the new type of the tile
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets the board name this tile belongs to.
     *
     * @return the board name, or null if not set
     */
    public String getBoard() {
        return board;
    }

    /**
     * Sets the name of the board this tile belongs to.
     *
     * @param board the board name
     */
    public void setBoard(String board) {
        this.board = board;
    }

    /**
     * Returns a list of all orientations for this tile.
     *
     * @return a non-null list of orientations (empty if none)
     */
    public List<String> getOrientations() {
        return orientations != null ? orientations : List.of();
    }

    /**
     * Sets the list of orientations. Must not be {@code null}.
     *
     * @param orientations a list of valid orientation strings
     */
    public void setOrientations(List<String> orientations) {
        if (orientations == null) {
            throw new IllegalArgumentException("orientations must not be null");
        }
        this.orientations = orientations;
    }

    /**
     * Gets the speed of the tile (used for conveyor belts).
     *
     * @return the speed, or null if not applicable
     */
    public Integer getSpeed() {
        return speed;
    }

    /**
     * Sets the speed of the tile.
     *
     * @param speed the speed value
     */
    public void setSpeed(Integer speed) {
        this.speed = speed;
    }

    /**
     * Gets the list of registers that activate this tile.
     *
     * @return the list of registers
     */
    public List<Integer> getRegisters() {
        return registers;
    }

    /**
     * Sets the list of registers that trigger this tile.
     *
     * @param registers the register numbers
     */
    public void setRegisters(List<Integer> registers) {
        this.registers = registers;
    }

    /**
     * Gets the number of elements associated with the tile (e.g., laser beams).
     *
     * @return the element count
     */
    public Integer getCount() {
        return count;
    }

    /**
     * Sets the count of elements (e.g., number of lasers).
     *
     * @param count the count
     */
    public void setCount(Integer count) {
        this.count = count;
    }

    /**
     * Gets the rotation type (e.g., "clockwise", "counterclockwise").
     *
     * @return the rotation direction
     */
    public String getRotation() {
        return rotation;
    }

    /**
     * Sets the rotation direction for this tile.
     *
     * @param rotation rotation direction string
     */
    public void setRotation(String rotation) {
        this.rotation = rotation;
    }

    /**
     * Gets the x-coordinate of the tile.
     *
     * @return the x-position
     */
    public int getX() {
        return x;
    }

    /**
     * Sets the x-coordinate of the tile.
     *
     * @param x the x-position
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * Gets the y-coordinate of the tile.
     *
     * @return the y-position
     */
    public int getY() {
        return y;
    }

    /**
     * Sets the y-coordinate of the tile.
     *
     * @param y the y-position
     */
    public void setY(int y) {
        this.y = y;
    }
}