package de.lmu.Board;

import de.lmu.cleverecousins.Position;
import de.lmu.cleverecousins.Robot;
import de.lmu.util.LogConfigurator;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * TileChecker is a utility class responsible for checking and activating tile effects
 * at the current position of a robot.
 * <p>
 * It separates the handling of conveyor belt movement from other tile-specific behaviors
 * such as gears, energy boosts, and restart points.
 */
public class TileChecker {

    private static final Logger logger = Logger.getLogger(TileChecker.class.getName());

    static{
    LogConfigurator.configureRootLogger(Level.FINE);
    }

    /**
     * Checks if the robot is standing on a {@code ConveyorBeltTile} and activates it once if present.
     *
     * @param board the game board containing all tiles
     * @param robot the robot whose current position is evaluated
     * @return {@code true} if a conveyor belt tile was found and activated, {@code false} otherwise
     */
    public static boolean handleConveyorBeltOnce(Board board, Robot robot) {
        Position pos = robot.getPosition();
        List<BoardTile> tiles = board.getTilesAt(pos);

        if (tiles == null || tiles.isEmpty()) {
            return false;
        }

        for (BoardTile tile : tiles) {
            if (tile instanceof ConveyorBeltTile cb) {
                logger.fine("TileChecker: ConveyorBelt gefunden → aktivieren");
                cb.activate(robot);
                return true;
            }
        }
        return false;
    }


    /**
     * Activates all tile effects at the robot's current position, excluding {@code ConveyorBeltTile}.
     * <p>
     * Handles effects such as gears, push panels, energy tiles, and restart/start points.
     *
     * @param board the game board containing all tiles
     * @param robot the robot whose current position is evaluated
     */
    public static void handleOtherTileEffects(Board board, Robot robot) {
        Position pos = robot.getPosition();
        List<BoardTile> tiles = board.getTilesAt(pos);

        if (tiles == null || tiles.isEmpty()) {
            logger.fine("TileChecker: Keine Tiles an aktueller Position. ");
            return;
        }

        for (BoardTile tile : tiles) {
            String type = tile.getType();
            logger.fine("TileChecker: Aktiviere Tile vom Typ: " + type);

            switch (type) {
                case "Gear" -> ((GearTile) tile).activate(robot);
                case "PushPanel" -> ((PushPanelTile) tile).activate(robot);
                case "Energy" -> {
                    ((EnergyTile) tile).activate(robot);
                    logger.fine("TileChecker: Energie erhalten → neuer Stand: " + robot.getEnergyReserve());
                }
                case "BoardLaser" -> ((BoardLaserTile) tile).activate(robot);

                case "StartPoint" -> {
                    logger.fine("TileChecker: StartPoint gespeichert als Spawnpunkt. ");
                    robot.setStartPoint(pos);
                }
                case "RestartPoint" -> {
                    logger.fine("TileChecker: RestartPoint gespeichert für Reboot. ");
                    robot.setRebootPosition(pos);
                }
                // Empty, Wall, Pit 等默认忽略
                default -> {}
            }
        }
    }
}


