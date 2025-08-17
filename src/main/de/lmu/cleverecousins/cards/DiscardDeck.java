package de.lmu.cleverecousins.cards;

import de.lmu.cleverecousins.cards.damageCards.DamageCard;
import de.lmu.cleverecousins.cards.programmingCards.ProgrammingCard;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a discard pile for programming and damage cards.
 *
 * Supports adding cards, clearing the discard pile, shuffling cards back into the deck,
 * and drawing from the discard pile.
 */
public class DiscardDeck {
    private List<ProgrammingCard> programmingCards;
    private List<DamageCard> damageCards;

    /**
     * Creates an empty discard deck for both programming and damage cards.
     */
    public DiscardDeck() {
        programmingCards = new ArrayList<>();
        damageCards = new ArrayList<>();
    }

    /**
     * Adds a programming card to the discard pile.
     *
     * @param card the programming card to add
     */
    public void add(ProgrammingCard card) {
        programmingCards.add(card);
    }

    /**
     * Adds a damage card to the discard pile.
     *
     * @param card the damage card to add
     */
    public void add(DamageCard card) {
        damageCards.add(card);
    }

    /**
     * Adds a list of programming cards to the discard pile.
     *
     * @param cardsToAdd the list of programming cards to add
     */
    public void addAll(List<ProgrammingCard> cardsToAdd) {
        programmingCards.addAll(cardsToAdd);
    }

    /**
     * Returns the number of programming cards in the discard pile.
     *
     * Note: Damage cards are not included in this count.
     *
     * @return the number of programming cards in the discard pile
     */
    public int size() {
        return programmingCards.size();
    }

    /**
     * Removes all programming cards from the discard pile.
     *
     * Note: Damage cards are not affected by this method.
     */
    public void clear() {
        programmingCards.clear();
    }

    /**
     * Shuffles all programming cards from the discard pile back into the given programming deck.
     *
     * After shuffling, the programming part of the discard pile is cleared.
     *
     * @param deck the programming deck to shuffle the discarded cards into
     */
    public void shuffleInto(ProgrammingDeck deck) {
        deck.addAll(new ArrayList<>(programmingCards));
        //deck.addAll(programmingCards);
        //deck.addAll(damageCards);
        clear();
    }

    /**
     * Draws and removes the most recently added programming card from the discard pile.
     *
     * @return the last programming card added, or {@code null} if the discard pile is empty
     */
    public ProgrammingCard draw() {
        if (programmingCards.isEmpty()) return null;
        return programmingCards.remove(programmingCards.size() - 1);
    }
}
