package de.lmu.protocol;

import de.lmu.Board.Board;
import de.lmu.Board.BoardTile;
import de.lmu.cleverecousins.Position;
import de.lmu.protocol.messageBody.GameStartedBody;

import java.util.List;

public class MapBuilder {

    @SuppressWarnings("unchecked")
    public static Board buildBoard(GameStartedBody body) {
        List<List<List<MapTileDefinition>>> defs = body.getGameMap();

        int width = defs.size();         // X方向格数
        int height = defs.get(0).size(); // Y方向格数

        Board board = new Board(width, height);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                List<MapTileDefinition> tileDefs = defs.get(x).get(y);

                if (tileDefs != null) {
                    for (MapTileDefinition def : tileDefs) {
                        if (def != null) {
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

