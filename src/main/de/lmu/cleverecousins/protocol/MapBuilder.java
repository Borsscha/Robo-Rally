package de.lmu.cleverecousins.protocol;

import de.lmu.Board.Board;
import de.lmu.Board.BoardTile;
import de.lmu.cleverecousins.Position;
import de.lmu.cleverecousins.protocol.messageBody.GameStartedBody;
import de.lmu.util.LogConfigurator;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * Utility class for constructing a {@link Board} object from a {@link GameStartedBody} map definition.
 * <p>
 * The class reads a nested list of {@link MapTileDefinition}s representing the game map layout,
 * converts each definition into a concrete {@link BoardTile}, and assembles them into the board grid.
 */
public class MapBuilder {

    private static final Logger logger = Logger.getLogger(MapBuilder.class.getName());

    static {
        LogConfigurator.configureRootLogger(Level.FINE);
    }

    /**
     * Builds a {@link Board} instance from the given {@link GameStartedBody}, which contains
     * a 3D list of map tile definitions (x, y, tile stack).
     * <p>
     * Each tile definition is converted into a specific {@link BoardTile} using {@link TileFactory},
     * and placed at the corresponding (x, y) location on the board. After construction,
     * the board's restart point is also initialized.
     *
     * @param body the {@link GameStartedBody} object containing the full map structure
     * @return a fully constructed {@link Board} populated with tiles
     */
    @SuppressWarnings("unchecked")
    public static Board buildBoard(GameStartedBody body) {
        List<List<List<MapTileDefinition>>> defs = body.getGameMap();

        int width = defs.size();         // X方向格数
        int height = defs.get(0).size(); // Y方向格数

        Board board = new Board(width, height);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                List<MapTileDefinition> tileDefs = defs.get(x).get(y);

                logger.info("[MapBuilder] Building board of size " + width + "x" + height);

                if (tileDefs != null) {
                    for (MapTileDefinition def : tileDefs) {
                        if (def != null) {
                            logger.fine("[MapBuilder] Creating tile type: " + def.getType() + " at (" + x + "," + y + ")");
                            def.setX(x);
                            def.setY(y);
                            Position pos = new Position(x, y);
                            BoardTile tile = TileFactory.createTile(def, pos, board);
                            board.addTile(x, y, tile);
                        }
                    }
                }
            }
        }
        board.findRestartPoint();
        return board;
    }
}

