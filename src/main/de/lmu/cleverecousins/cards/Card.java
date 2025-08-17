package de.lmu.cleverecousins.cards;

/**
 * Abstract base class for all cards used in the game.
 *
 * Provides common methods that all specific card types must implement,
 * such as retrieving the card's image path and protocol name.
 */
public abstract class Card {

    /**
     * Returns the relative or absolute image path used to visually display the card.
     *
     * @return the image path as a {@code String}
     */
    public abstract String getImagePath();

    /**
     * Returns the unique name of the card (e.g., "MoveI"),
     * as expected by the communication protocol.
     *
     * @return the card's protocol name
     */
    public abstract String getName();
}