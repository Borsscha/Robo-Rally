package de.lmu.Board;

import de.lmu.cleverecousins.Position;
import de.lmu.cleverecousins.Robot;
import de.lmu.util.LogConfigurator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * In-memory representation of the game board.
 * <p>
 * The board stores a 2D grid of {@link BoardTile} lists (multiple tiles can stack on one field),
 * tracks robot locations, and provides helpers such as restart-point lookup and maximum checkpoint count.
 */
public class Board {

    private static final Logger logger = Logger.getLogger(Board.class.getName());

    static {
        LogConfigurator.configureRootLogger(Level.FINE);
    }

    /** Board width in tiles. */
    private final int width;

    /** Board height in tiles. */
    private final int height;

    /** Grid of tile lists, indexed by [x][y]. */
    private final List<BoardTile>[][] tiles;

    /** Mapping from positions to robots currently occupying them. */
    private final Map<Position, Robot> robotsOnBoard = new HashMap<>();

    /** Cached restart point (first {@link RestartPointTile} found). */
    private Position restartPoint;

    /**
     * Creates a new empty board with the given dimensions.
     *
     * @param width  number of columns
     * @param height number of rows
     */
    @SuppressWarnings("unchecked")
    public Board(int width, int height) {
        this.width = width;
        this.height = height;
        this.tiles = new
                ArrayList[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                tiles[x][y] = new ArrayList<>();
            }
        }
    }

    /**
     * Adds a tile at the given coordinates (if within bounds).
     *
     * @param x    column index
     * @param y    row index
     * @param tile tile instance to add
     */
    public void addTile(int x, int y, BoardTile tile) {
        logger.fine("[Board] Added tile: " + tile.getType() + " at (" + x + "," + y + ")");
        if (isInBounds(x, y)) {
            tiles[x][y].add(tile);
        }
    }

    /**
     * Returns all tiles present at the given coordinates.
     *
     * @param x column
     * @param y row
     * @return list of tiles (empty if out of bounds)
     */
    public List<BoardTile> getTilesAt(int x, int y) {
        if (isInBounds(x, y)) {
            return tiles[x][y];
        }
        return List.of();
    }

    /**
     * Convenience overload for {@link #getTilesAt(int, int)}.
     *
     * @param pos position object
     * @return list of tiles at that position
     */
    public List<BoardTile> getTilesAt(Position pos) {
        return getTilesAt(pos.getX(), pos.getY());
    }

    /**
     * @return {@code true} if (x,y) is inside the board boundaries
     */
    public boolean isInBounds(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    /** @return board width in tiles */
    public int getWidth() {
        return width;
    }

    /** @return board height in tiles */
    public int getHeight() {
        return height;
    }

    /**
     * Places a robot on the board at the given position and updates the robot's own position.
     *
     * @param robot    robot to place
     * @param position target coordinates
     */
    public void placeRobot(Robot robot, Position position) {
        robotsOnBoard.put(position, robot);
        robot.setPosition(position);
    }

    /**
     * Moves a robot from its current position to a new one, updating the map.
     *
     * @param robot       robot to move
     * @param newPosition new coordinates
     */
    public void moveRobot(Robot robot, Position newPosition) {
        robotsOnBoard.remove(robot.getPosition());
        robotsOnBoard.put(newPosition, robot);
        robot.setPosition(newPosition);
    }

    /**
     * Returns the robot at the given position, or {@code null} if none.
     *
     * @param pos coordinates to check
     * @return robot or {@code null}
     */
    public Robot getRobotAt(Position pos) {
        return robotsOnBoard.get(pos);
    }

    /**
     * Removes a robot from the board mapping.
     *
     * @param robot robot to remove
     */
    public void removeRobot(Robot robot) {
        robotsOnBoard.remove(robot.getPosition());
    }

    /**
     * Searches the board for a {@link RestartPointTile} and caches its position.
     *
     * @throws IllegalStateException if no restart point tile exists
     */
    public void findRestartPoint() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (BoardTile tile : tiles[x][y]) {
                    if (tile instanceof RestartPointTile) {
                        this.restartPoint = new Position(x, y);
                        logger.fine("[DEBUG] Found RestartPoint at " + this.restartPoint);
                        return;
                    }
                }
            }
        }
        throw new IllegalStateException("No RestartPointTile on map!");
    }

    /**
     * @return cached restart point position (call {@link #findRestartPoint()} first)
     */
    public Position getRestartPoint() {
        return restartPoint;
    }

    /**
     * Computes the highest checkpoint index present on the board.
     *
     * @return max checkpoint number encountered (0 if none)
     */
    public int getMaxCheckpointCount() {
        int max = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (BoardTile tile : tiles[x][y]) {
                    if (tile instanceof CheckpointTile cp) {
                        max = Math.max(max, cp.getCount());
                    }
                }
            }
        }
        return max;
    }
}

