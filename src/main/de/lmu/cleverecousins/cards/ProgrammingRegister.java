package de.lmu.cleverecousins.cards;

import de.lmu.cleverecousins.cards.programmingCards.ProgrammingCard;
import de.lmu.util.LogConfigurator;

import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages a list of up to five programming cards
 * that are played sequentially during a game round.
 *
 * Provides methods to add, play, reset, and print the cards in the register.
 */
public class ProgrammingRegister {

    private static final Logger logger = Logger.getLogger(ProgrammingRegister.class.getName());

    static {
        LogConfigurator.configureRootLogger(Level.FINE);
    }

    private LinkedList<ProgrammingCard> register;


    public ProgrammingRegister() {
        this.register = new LinkedList<>();
    }

    /**
     * Adds a programming card to the register if there is available space.
     *
     * @param card the programming card to add
     */
    public void addCard(ProgrammingCard card) {
        if (register.size() < 5){
            register.add(card);
        } else {
            logger.warning("Register is already full (5 cards)");
        }
    }

    /**
     * Returns and removes the next programming card (the leftmost one) from the register.
     *
     * @return the next card, or {@code null} if the register is empty
     */
    public ProgrammingCard playNextCard() {
        if (!register.isEmpty()) {
            return register.removeFirst(); // "linkeste Karte"
        }
        return null;
    }

    /**
     * Checks whether the register is empty.
     *
     * @return {@code true} if the register contains no cards, otherwise {@code false}
     */
    public boolean isEmpty() {
        return register.isEmpty();
    }

    /**
     * Removes all cards from the register to prepare for a new game round.
     */
    public void reset() {
        register.clear();
    }

    /**
     * Logs the current state of the register.
     *
     * Each card is printed with its position in the register (starting at 1).
     */
    public void printRegister() {
        logger.info("Current Register:");
        for (int i = 0; i < register.size(); i++) {
            logger.fine("[" + (i + 1) + "] " + register.get(i));
        }
    }

}
