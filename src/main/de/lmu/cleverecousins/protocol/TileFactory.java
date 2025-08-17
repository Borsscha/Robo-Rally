package de.lmu.cleverecousins.protocol;

import de.lmu.Board.*;
import de.lmu.cleverecousins.Direction;
import de.lmu.cleverecousins.Position;
import de.lmu.util.LogConfigurator;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * The TileFactory class is responsible for creating instances of various {@link BoardTile}
 * subclasses based on the provided {@link MapTileDefinition}.
 * <p>
 * It acts as a central factory for interpreting map data and constructing corresponding
 * tile objects at specific positions on the game board. The supported tile types include
 * walls, conveyor belts, lasers, checkpoints, gears, pits, and more.
 */
public class TileFactory {

    private static final Logger logger = Logger.getLogger(TileFactory.class.getName());

    static {
        LogConfigurator.configureRootLogger(Level.FINE);
    }

    /**
     * Creates a specific type of {@link BoardTile} based on the information given in
     * the {@link MapTileDefinition}.
     *
     * @param def      the definition of the map tile, containing attributes such as type,
     *                 orientation, speed, and count.
     * @param position the position of the tile on the board.
     * @param board    the game board, passed for context if the tile requires board-level access
     *                 (e.g., for conveyor belts).
     * @return a concrete instance of a subclass of {@link BoardTile} representing the tile.
     * @throws IllegalArgumentException if the tile type is unknown or required data is missing.
     */
    public static BoardTile createTile(MapTileDefinition def, Position position, Board board) {
        String isOnBoard = def.getOnBoard();

        logger.fine("[TileFactory] Switching on type: " + def.getType());

        return switch (def.getType()) {
            case "PushPanel" -> {
                List<Direction> directions = parseDirections(def);
                yield new PushPanelTile(position, isOnBoard, directions, def.getRegisters());
            }

            case "Wall" -> {
                List<Direction> directions = parseDirections(def);
                yield new WallTile(position, isOnBoard, directions);
            }

            case "Laser" -> {
                List<Direction> directions = parseDirections(def);
                yield new BoardLaserTile(position, isOnBoard, directions, def.getCount());
            }

            case "Gear" -> {
                boolean clockwise = def.getOrientations() != null
                        && !def.getOrientations().isEmpty()
                        && "clockwise".equalsIgnoreCase(def.getOrientations().get(0));
                yield new GearTile(position, isOnBoard, clockwise);
            }

            case "Empty" -> new EmptyTile(position, isOnBoard);

            case "Checkpoint" -> new CheckpointTile(position, isOnBoard, def.getCount());

            case "Energy" -> new EnergyTile(position, isOnBoard, def.getCount());

            case "ConveyorBelt" -> {
                if (def.getOrientations() == null || def.getSpeed() == null) {
                    throw new IllegalArgumentException("Missing conveyor belt data");
                }
                List<Direction> directions = parseDirections(def);

                String boardId = def.getOnBoard();

                yield new ConveyorBeltTile(position, boardId, directions, def.getSpeed(), board);
            }

            case "StartPoint" -> new StartPointTile(position, isOnBoard);

            case "Pit" -> new PitTile(position, isOnBoard);

            case "Antenna" -> new AntennaTile(position, isOnBoard);

            case "RestartPoint" -> {
                logger.fine("TileFactory: creating RestartPoint at " + position);
                yield new RestartPointTile(position, isOnBoard);
            }
            default -> throw new IllegalArgumentException("Unknown tile type: " + def.getType());
        };
    }

    private static List<Direction> parseDirections(MapTileDefinition def) {
        return def.getOrientations().stream()
                .map(String::toUpperCase)
                .map(Direction::valueOf)
                .collect(Collectors.toList());
    }
}

