package de.lmu.cleverecousins;

/**
 * Immutable snapshot of a robot's position and facing on the board.
 *
 * @param clientID  owner/player client ID
 * @param x         x-coordinate on the map (column)
 * @param y         y-coordinate on the map (row)
 * @param direction facing direction (e.g. "TOP", "RIGHT", "BOTTOM", "LEFT")
 */
public record RobotPosition(int clientID, int x, int y, String direction) {
}
