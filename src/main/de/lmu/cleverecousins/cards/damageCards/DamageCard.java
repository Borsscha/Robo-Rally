// src/main/java/de/lmu/cleverecousins/cards/damageCards/DamageCard.java
package de.lmu.cleverecousins.cards.damageCards;

import de.lmu.cleverecousins.Game;
import de.lmu.cleverecousins.Robot;
import de.lmu.cleverecousins.cards.Card;

/**
 * Abstract base class for all damage cards.
 *
 * Extends {@link Card} to be compatible with the GUI and ViewModel.
 *
 * Each damage card has a name, a description, and a specific in-game effect
 * that must be implemented by subclasses.
 */
public abstract class DamageCard extends Card {

    protected String name;
    protected String description;

    /**
     * Constructs a damage card with a given name and description.
     *
     * @param name the name of the damage card
     * @param description a brief explanation of its effect
     */
    public DamageCard(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * Returns the path to the image file representing this card.
     *
     * This method must be implemented by each concrete damage card subclass.
     *
     * @return the image path as a {@code String}
     */
    @Override
    public abstract String getImagePath();

    /**
     * Returns the name of the damage card.
     *
     * @return the card's name
     */
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Returns a string representation of the damage card,
     * combining its name and description.
     *
     * @return a string in the format: "Name – Description"
     */
    @Override
    public String toString() {
        return name + " – " + description;
    }

    /**
     * Applies the effect of this damage card to a given robot within the current game context.
     *
     * @param robot the robot affected by the card
     * @param game the game context in which the card is applied
     */
    public abstract void applyEffect(Robot robot, Game game);
}