package de.lmu.test;

import de.lmu.Board.*;
import de.lmu.cleverecousins.Direction;
import de.lmu.cleverecousins.Position;
import de.lmu.cleverecousins.Robot;
import de.lmu.protocol.MapBuilder;
import de.lmu.protocol.MapLoader;
import de.lmu.protocol.messageBody.GameStartedBody;
import de.lmu.util.LogConfigurator;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class MapTestRunner {

    private static final Logger logger = Logger.getLogger(MapTestRunner.class.getName());

    static {
        LogConfigurator.configureRootLogger(Level.FINE); // optional, falls vorhanden
    }

    public void tryMove2(Robot robot, Board board, int steps) {
        for (int i = 0; i < steps; i++) {
            Position oldPos = robot.getPosition();
            Position nextPos = oldPos.moved(robot.getDirection());

            if (!board.isInBounds(nextPos.getX(), nextPos.getY())) {
                logger.warning("Hit map boundary! Cannot move.");
                break;
            }

            boolean blocked = false;

            List<BoardTile> tiles = board.getTilesAt(oldPos);
            for (BoardTile tile : tiles) {
                if (tile instanceof WallTile wall && wall.getBlockedSides().contains(robot.getDirection())) {
                    blocked = true;
                    logger.info("Blocked by wall on current tile.");
                    break;
                }
            }

            if (!blocked) {
                Direction reverseDir = robot.getDirection().uturn();
                List<BoardTile> targetTiles = board.getTilesAt(nextPos);
                for (BoardTile tile : targetTiles) {
                    if (tile instanceof WallTile wall && wall.getBlockedSides().contains(reverseDir)) {
                        blocked = true;
                        logger.info("Blocked by wall on target tile (reverse check).");
                        break;
                    }
                }
            }

            if (blocked) {
                logger.info("Hit wall! Cannot move.");
                break;
            }

            // 正式移动一步
            robot.setPosition(nextPos);
        }

        // ✅ 只检查当前位置的传送带，一次
        List<BoardTile> tilesAfterMove = board.getTilesAt(robot.getPosition());
        for (BoardTile tile : tilesAfterMove) {
            if (tile instanceof ConveyorBeltTile cb) {
                cb.activate(robot);
                break; // 只触发一次
            }
        }

        // ✅ 其他效果
        TileChecker.handleOtherTileEffects(board, robot);

        logger.info("Moved to " + robot.getPosition());
        String boardId = tilesAfterMove.isEmpty() ? "Unknown" : tilesAfterMove.get(0).getIsOnBoard();
        logger.info("Robot is on board: " + boardId);
    }

    public static void main(String[] args) {
        try {
            GameStartedBody body = MapLoader.loadMap("/map-dizzy-highway.json");
            Board board = MapBuilder.buildBoard(body);  // 直接获得完整的 Board 对象

            int width = board.getWidth();
            int height = board.getHeight();

// 可选：打印地图结构（比如 tile 类型）
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    List<BoardTile> tiles = board.getTilesAt(x, y);
                    if (!tiles.isEmpty()) {
                        logger.info("[" + x + "," + y + "]: " + tiles.stream()
                                .map(BoardTile::getType)
                                .collect(Collectors.joining(", ")));
                    }
                }
            }

            // 初始化机器人
            Robot robot = new Robot(new Position(0, 0), Direction.TOP);
            logger.info("Initial robot state: " + robot);

            Scanner scanner = new Scanner(System.in);

            while (true) {
                logger.fine("\nCommand:");
                String input = scanner.nextLine().trim();

                if (input.equals("/quit")) break;

                switch (input) {
                    case "/move1" -> {
                        tryMove(robot, board, 1);
                        TileChecker.handleOtherTileEffects(board, robot);
                    }
                    case "/move2" -> {
                        tryMove(robot, board, 2);
                        TileChecker.handleOtherTileEffects(board, robot);
                    }
                    case "/move3" -> {
                        tryMove(robot, board, 3);
                        TileChecker.handleOtherTileEffects(board, robot);
                    }
                    case "/turnleft" -> {
                        robot.rotateCounterclockwise();
                        logger.info("Turned left → Now facing " + robot.getDirection());

                        // 链式传送带
                        TileChecker.handleConveyorBeltOnce(board, robot);

                        // 其他效果
                        TileChecker.handleOtherTileEffects(board, robot);

                        logger.info("Robot at: " + robot.getPosition());
                    }

                    case "/turnright" -> {
                        robot.rotateClockwise();
                        logger.info("Turned right → Now facing " + robot.getDirection());

                        TileChecker.handleConveyorBeltOnce(board, robot);
                        TileChecker.handleOtherTileEffects(board, robot);

                        logger.info("Robot at: " + robot.getPosition());
                    }
                    case "/uturn" -> {
                        robot.uturn();
                        logger.info("U-Turned → Now facing " + robot.getDirection());

                        TileChecker.handleConveyorBeltOnce(board, robot);
                        TileChecker.handleOtherTileEffects(board, robot);

                        logger.info("Robot at: " + robot.getPosition());
                    }
                    case "/status" -> {
                        logger.info("Status: " + robot);
                        String boardId = board.getTilesAt(robot.getPosition())
                                .stream()
                                .findFirst()
                                .map(BoardTile::getIsOnBoard)
                                .orElse("Unknown");
                        logger.info("Robot is at board: " + boardId);
                    }
                    default -> logger.warning("Unknown command.");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void tryMove(Robot robot, Board board, int steps) {
        for (int i = 0; i < steps; i++) {
            Position oldPos = robot.getPosition();
            Position nextPos = oldPos.moved(robot.getDirection());

            if (!board.isInBounds(nextPos.getX(), nextPos.getY())) {
                logger.warning("Hit map boundary! Cannot move.");
                break;
            }

            boolean blocked = false;

            List<BoardTile> tiles = board.getTilesAt(oldPos);
            for (BoardTile tile : tiles) {
                if (tile instanceof WallTile wall && wall.getBlockedSides().contains(robot.getDirection())) {
                    blocked = true;
                    logger.info("Blocked by wall on current tile.");
                    break;
                }
            }

            if (!blocked) {
                Direction reverseDir = robot.getDirection().uturn();
                List<BoardTile> targetTiles = board.getTilesAt(nextPos);
                for (BoardTile tile : targetTiles) {
                    if (tile instanceof WallTile wall && wall.getBlockedSides().contains(reverseDir)) {
                        blocked = true;
                        logger.info("Blocked by wall on target tile (reverse check).");
                        break;
                    }
                }
            }

            if (blocked) {
                logger.info("Hit wall! Cannot move.");
                break;
            }

            // 正式移动一步
            robot.setPosition(nextPos);
        }

        //只检查当前位置的传送带，一次
        List<BoardTile> tilesAfterMove = board.getTilesAt(robot.getPosition());
        for (BoardTile tile : tilesAfterMove) {
            if (tile instanceof ConveyorBeltTile cb) {
                cb.activate(robot);
                break; // 只触发一次
            }
        }

        //其他效果
        TileChecker.handleOtherTileEffects(board, robot);

        logger.info("Moved to " + robot.getPosition());
        String boardId = tilesAfterMove.isEmpty() ? "Unknown" : tilesAfterMove.get(0).getIsOnBoard();
        logger.info("Robot is on board: " + boardId);
    }
}
