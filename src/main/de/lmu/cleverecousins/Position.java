package de.lmu.cleverecousins;

import java.util.Objects;

/**
 * Mutable 2D grid coordinate used for robot/map positions.
 * <p>
 * Provides convenience methods to move in a given {@link Direction}, compute distances,
 * and standard overrides for equality, hashing, and string representation.
 */
public class Position {

    /** X coordinate (column). */
    private int x;

    /** Y coordinate (row). */
    private int y;

    /**
     * Creates a new position.
     *
     * @param x x-coordinate
     * @param y y-coordinate
     */
    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // ------------------------
    // Getters
    // ------------------------

    /**
     * @return current x-coordinate
     */
    public int getX() {
        return x;
    }

    /**
     * @return current y-coordinate
     */
    public int getY() {
        return y;
    }


    // ------------------------
    // Setters
    // ------------------------


    /**
     * Sets the x-coordinate.
     *
     * @param x new x value
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * Sets the y-coordinate.
     *
     * @param y new y value
     */
    public void setY(int y) {
        this.y = y;
    }

     /**
     * Moves this position in-place according to the given direction.
     * The current object is modified.
     *
     * @param dir direction to move
     */
    public void moveInPlace(Direction dir) {
        switch (dir) {
            case TOP -> y--;
            case RIGHT -> x++;
            case BOTTOM -> y++;
            case LEFT -> x--;
        }
    }

    /**
     * Returns a new {@code Position} moved one step in the given direction,
     * leaving the current object unchanged.
     *
     * @param dir direction to move
     * @return new position after moving one tile
     */
    public Position moved(Direction dir) {
        return switch (dir) {
            case TOP    -> new Position(x, y - 1);
            case RIGHT -> new Position(x + 1, y);
            case BOTTOM  -> new Position(x, y + 1);
            case LEFT  -> new Position(x - 1, y);
        };
    }

    /**
     * Calculates the Euclidean distance to another position.
     * Used e.g. by the Virus card.
     *
     * @param other other position
     * @return Euclidean distance between this and {@code other}
     */
    public double euclideanDistanceTo(Position other) {
        int dx = this.x - other.x;
        int dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Two positions are equal if both their x and y coordinates match.
     *
     * @param o object to compare
     * @return {@code true} if positions are equal, else {@code false}
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Position other)) return false;
        return this.x == other.x && this.y == other.y;
    }

    /**
     * Hashes based on x and y coordinates.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    /**
     * @return string in the form {@code (x, y)}
     */
    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
