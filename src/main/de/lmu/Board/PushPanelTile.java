package de.lmu.Board;

import de.lmu.cleverecousins.Direction;
import de.lmu.cleverecousins.Position;
import de.lmu.cleverecousins.Robot;
import de.lmu.util.LogConfigurator;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a Push Panel tile on the game board.
 * <p>
 * A Push Panel is a tile that pushes robots in a specific direction during certain register phases.
 * The push direction is always opposite of the side the panel is displayed on.
 */
public class PushPanelTile extends BoardTile {

    private static final Logger logger = Logger.getLogger(PushPanelTile.class.getName());

    static {
        LogConfigurator.configureRootLogger(Level.FINE);
    }

    /**
     * The side of the board on which the push panel is located (visual orientation).
     */
    private final Direction boardSide;

    /**
     * The direction in which the push panel moves the robot.
     */
    private final Direction pushDir;

    /**
     * The list of register indices in which this push panel activates.
     */
    private final List<Integer> activeRegisters;

    /**
     * Constructs a PushPanelTile.
     *
     * @param position         the position of the tile on the board
     * @param isOnBoard        string indicating if tile is rendered on board
     * @param orientations     direction(s) from which the push panel appears (usually one element)
     * @param activeRegisters  the register numbers during which the push is triggered
     */
    public PushPanelTile(Position position, String isOnBoard, List<Direction> orientations, List<Integer> activeRegisters) {
        super(position, isOnBoard);

        this.boardSide = orientations.get(0);
        this.pushDir = getOpposite(boardSide);
        this.activeRegisters = activeRegisters;
    }

    /**
     * Activates the push panel tile.
     * If the robot's current register matches one of the tile's active registers,
     * the robot will be pushed in the push direction.
     *
     * @param robot the robot currently on this tile
     */
    @Override
    public void activate(Robot robot) {
        int currentRegister = robot.getCurrentRegisterIndex();
        if (activeRegisters.contains(currentRegister)) {
            logger.fine("PushPanel: Pushing robot 1 step towards " + pushDir);
            robot.moveInDirection(pushDir, 1);
        }
    }

    /**
     * Returns the type of this board tile.
     *
     * @return the string "PushPanel"
     */
    @Override
    public String getType() {
        return "PushPanel";
    }

    /**
     * Gets the visual orientation of the panel on the board.
     *
     * @return the board side this panel faces
     */
    public Direction getBoardSide() {
        return boardSide;
    }

    /**
     * Gets the direction in which the robot will be pushed.
     *
     * @return the push direction
     */
    public Direction getPushDir() {
        return pushDir;
    }

    /**
     * Returns the list of registers during which this push panel is active.
     *
     * @return list of active register numbers
     */
    public List<Integer> getActiveRegisters() {
        return activeRegisters;
    }

    /**
     * Returns the opposite direction of the given one.
     *
     * @param dir the input direction
     * @return the opposite direction
     */
    private Direction getOpposite(Direction dir) {
        return switch (dir) {
            case TOP -> Direction.BOTTOM;
            case BOTTOM -> Direction.TOP;
            case LEFT -> Direction.RIGHT;
            case RIGHT -> Direction.LEFT;
        };
    }
}

