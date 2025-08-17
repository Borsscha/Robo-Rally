package de.lmu.Board;

import de.lmu.cleverecousins.Direction;
import de.lmu.cleverecousins.Position;
import de.lmu.cleverecousins.Robot;
import de.lmu.util.LogConfigurator;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A tile representing a conveyor belt on the board.
 *
 * Conveyor belts move robots automatically in a specified direction, possibly across multiple tiles,
 * depending on their speed. Some conveyor belts support multiple entry directions for chaining.
 *
 * Extends {@link BoardTile} and interacts with {@link Robot} and {@link Board}.
 */
public class ConveyorBeltTile extends BoardTile {

    private static final Logger logger = Logger.getLogger(ConveyorBeltTile.class.getName());

    static {
        LogConfigurator.configureRootLogger(Level.FINE);
    }

    private final Direction exitDir;
    private final List<Direction> entryDirs;
    private final int speed;
    private final Board board;

    /**
     * Constructs a {@code ConveyorBeltTile} with the given position, directions, speed and board reference.
     *
     * @param position the tile's location on the board
     * @param isOnBoard indicator if the tile is part of the game board
     * @param directions a list where the first is the exit direction, and the rest (optional) are entry directions
     * @param speed how many steps the belt moves a robot during activation
     * @param board the game board containing all tiles
     * @throws IllegalArgumentException if {@code directions} is null or empty
     */
    public ConveyorBeltTile(Position position, String isOnBoard, List<Direction> directions, int speed, Board board) {
        super(position, isOnBoard);

        if (directions == null || directions.isEmpty()) {
            throw new IllegalArgumentException("ConveyorBelt must have at least one exit direction!");
        }

        this.exitDir = directions.get(0);

        if (directions.size() > 1) {
            this.entryDirs = directions.subList(1, directions.size());
        } else {
            this.entryDirs = List.of();
        }

        this.speed = speed;
        this.board = board;
    }

    /**
     * Activates the conveyor belt, moving the given robot forward automatically.
     *
     * If the belt supports chaining (has entry directions), the robot continues
     * to move along connected conveyor tiles for up to {@code speed} steps.
     *
     * @param robot the robot to move
     */
    @Override
    public void activate(Robot robot) {
        if (entryDirs.isEmpty()) {
            logger.fine("ConveyorBelt: Simple move " + speed + " step(s) towards " + exitDir);
            robot.moveInDirection(exitDir, speed);
        } else {
            logger.fine("ConveyorBelt: Start activate with entryDirs, speed = " + speed);

            BoardTile currentTile = this;
            Direction prevExitDir = this.exitDir;

            for (int step = 0; step < speed; step++) {
                ConveyorBeltTile belt = (ConveyorBeltTile) currentTile;
                logger.fine("ConveyorBelt: Step " + (step + 1));
                belt.stepOnAndMove(robot, prevExitDir);

                prevExitDir = belt.getExitDir();
                List<BoardTile> tilesAt = board.getTilesAt(robot.getPosition());
                ConveyorBeltTile nextBelt = null;
                for (BoardTile t : tilesAt) {
                    if (t instanceof ConveyorBeltTile) {
                        nextBelt = (ConveyorBeltTile) t;
                        break;
                    }
                }
                if (nextBelt == null) {
                    break;
                }

                currentTile = nextBelt;
            }
        }
    }

    /**
     * Moves a robot one step forward along the conveyor belt.
     *
     * Used internally for step-by-step chained movement.
     *
     * @param robot the robot to move
     * @param prevExitDir the direction from which the robot arrived (used to compute entry logic)
     */
    public void stepOnAndMove(Robot robot, Direction prevExitDir) {
        Direction entryDir = prevExitDir.opposite();
        logger.fine("ConveyorBelt: Calculated entryDir = " + entryDir);

        if (!entryDir.equals(exitDir)) {
            if (entryDirs.contains(entryDir)) {
                logger.fine("ConveyorBelt: Valid entry direction " + entryDir + " → follow exit " + exitDir + " (NO turning)");
            } else {
                logger.fine("ConveyorBelt: Entry direction " + entryDir + " not listed → still push straight towards " + exitDir);
            }
        }
        logger.fine("ConveyorBelt: Moving robot 1 step towards " + exitDir);
        robot.moveInDirection(exitDir, 1);
    }

    /**
     * Returns the type of the tile as a string.
     *
     * @return the string "ConveyorBelt"
     */
    @Override
    public String getType() {
        return "ConveyorBelt";
    }

    /**
     * Returns the direction in which the belt pushes the robot.
     *
     * @return the exit direction
     */
    public Direction getExitDir() {
        return exitDir;
    }

    /**
     * Returns a list of valid entry directions for chaining.
     *
     * @return a list of entry directions (possibly empty)
     */
    public List<Direction> getEntryDirs() {
        return entryDirs;
    }

    /**
     * Returns the speed (number of steps) the conveyor moves the robot.
     *
     * @return speed of the belt
     */
    public int getSpeed() {
        return speed;
    }
}
