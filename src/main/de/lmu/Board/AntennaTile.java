package de.lmu.Board;

import de.lmu.cleverecousins.Player;
import de.lmu.cleverecousins.Position;
import de.lmu.cleverecousins.Robot;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Board tile representing the antenna used to determine player priority.
 * <p>
 * The antenna itself does nothing on activation, but it provides helper logic to sort players
 * by priority: first by Manhattan distance to the antenna, then (for ties) by the clockwise angle
 * from the antenna to the robot position.
 */
public class AntennaTile extends BoardTile {

    /**
     * Creates an antenna tile at the given board position.
     *
     * @param position  tile coordinates
     * @param isOnBoard protocol flag/string indicating on-board state (passed to super)
     */
    public AntennaTile(Position position, String isOnBoard) {
        super(position, isOnBoard);
    }

    /**
     * Antenna has no direct activation effect on a single robot.
     *
     * @param robot the robot stepping on/being processed by this tile (ignored)
     */
    @Override
    public void activate(Robot robot) {

    }

    /**
     * @return the string literal {@code "Antenna"}
     */
    @Override
    public String getType() {
        return "Antenna";
    }

    /**
     * Returns the players sorted by priority: nearest to the antenna first (Manhattan distance),
     * then by a clockwise angle tiebreaker.
     *
     * @param players list of players to sort
     * @return new list sorted by priority
     */
    public List<Player> getPriorityOrder(List<Player> players) {
        return players.stream()
                .sorted(Comparator.comparingInt((Player p) -> manhattanDistance(p.getRobot().getPosition()))
                        .thenComparing((Player p) -> angleTo(p.getRobot().getPosition())))
                .collect(Collectors.toList());
    }

    /**
     * Computes Manhattan distance from this antenna tile to another position.
     *
     * @param other position to compare
     * @return |dx| + |dy|
     */
    private int manhattanDistance(Position other) {
        return Math.abs(this.getPosition().getX() - other.getX()) +
                Math.abs(this.getPosition().getY() - other.getY());
    }

    /**
     * Tie-breaker: computes the clockwise angle from the antenna to the given position.
     * Smaller angles have higher priority.
     *
     * @param other robot position
     * @return angle in radians suitable for sorting
     */
    private double angleTo(Position other) {
        int dx = other.getX() - this.getPosition().getX();
        int dy = this.getPosition().getY() - other.getY();
        return Math.atan2(dx, dy);
    }
}
