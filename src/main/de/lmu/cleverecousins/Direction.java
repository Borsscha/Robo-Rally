package de.lmu.cleverecousins;

/**
 * Cardinal directions used for robot orientation and movement on the board.
 */
public enum Direction {
    TOP, RIGHT, BOTTOM, LEFT;;

    /**
     * Returns the direction 90° to the left (counter‑clockwise) of this one.
     *
     * @return direction after a left turn
     */
    public Direction turnLeft() {
        return values()[(this.ordinal() + 3) % 4];
    }

    /**
     * Returns the direction 90° to the right (clockwise) of this one.
     *
     * @return direction after a right turn
     */
    public Direction turnRight() {
        return values()[(this.ordinal() + 1) % 4];
    }

    /**
     * Returns the direction 180° opposite to this one (U‑turn).
     *
     * @return direction after a U‑turn
     */
    public Direction uturn() {
        return values()[(this.ordinal() + 2) % 4];
    }

    /**
     * Semantic alias for a 180° turn; identical to {@link #uturn()} but more readable in some contexts.
     *
     * @return the opposite direction
     */
    public Direction opposite() {
        return switch (this) {
            case TOP -> BOTTOM;
            case BOTTOM -> TOP;
            case LEFT -> RIGHT;
            case RIGHT -> LEFT;
        };
    }

}

