package de.lmu.protocol;

import de.lmu.Board.*;
import de.lmu.cleverecousins.Direction;
import de.lmu.cleverecousins.Position;
import de.lmu.util.LogConfigurator;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class TileFactory {

    private static final Logger logger = Logger.getLogger(TileFactory.class.getName());

    static {
        LogConfigurator.configureRootLogger(Level.FINE);
    }

    public static BoardTile createTile(MapTileDefinition def, Position position, Board board) {
        String isOnBoard = def.getOnBoard(); // 从 JSON 拿

        //System.out.println("[TileFactory] Switching on type: " + def.getType());
        logger.fine("[TileFactory] Switching on type: " + def.getType());
        return switch (def.getType()) {
            case "PushPanel" -> {
                List<Direction> directions = parseDirections(def);
                if (directions.size() != 1) {
                    throw new IllegalArgumentException("PushPanel must have exactly one orientation");
                }
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

                yield new ConveyorBeltTile(position, isOnBoard, directions, def.getSpeed(), board);
            }


            case "StartPoint" -> new StartPointTile(position, isOnBoard);

            case "Pit" -> new PitTile(position, isOnBoard);

            case "Antenna" -> new AntennaTile(position, isOnBoard);

            case "RestartPoint" -> {
                //System.out.println("TileFactory: creating RestartPoint at " + position);
                logger.fine("[TileFactory] creating RestartPoint at " + position);
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

