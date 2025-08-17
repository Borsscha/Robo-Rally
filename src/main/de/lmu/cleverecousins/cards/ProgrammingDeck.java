package de.lmu.cleverecousins.cards;

import de.lmu.cleverecousins.cards.programmingCards.*;
import de.lmu.cleverecousins.cards.programmingCards.ProgrammingCard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a deck of programming cards used in the game.
 *
 * Initializes with a fixed set of 20 predefined cards, supports shuffling, drawing,
 * adding, and clearing cards.
 */
public class ProgrammingDeck {
    private List<ProgrammingCard> cards;
    /**
     * Constructs a new programming deck with a predefined set of 20 cards
     * The deck is shuffled upon creation.
     */
    public ProgrammingDeck() {
        cards = new ArrayList<>();
        for (int i = 0; i < 5; i++) cards.add(new MoveOneCard());
        for (int i = 0; i < 3; i++) cards.add(new MoveTwoCard());
        cards.add(new MoveThreeCard());
        for (int i = 0; i < 3; i++) cards.add(new TurnRightCard());
        for (int i = 0; i < 3; i++) cards.add(new TurnLeftCard());
        cards.add(new BackUpCard());
        cards.add(new PowerUpCard());
        for (int i = 0; i < 2; i++) cards.add(new AgainCard());
        cards.add(new UTurnCard());
        shuffle();
    }
    /**
     * Randomly shuffles the order of cards in the deck.
     */
    public void shuffle() {
        Collections.shuffle(cards);
    }
    /**
     * Draws up to {@code n} cards from the top of the deck.
     *
     * @param n the maximum number of cards to draw
     * @return a list of drawn cards (may be fewer than {@code n} if the deck is smaller)
     */
    public List<ProgrammingCard> draw(int n) {
        List<ProgrammingCard> drawn = new ArrayList<>();
        for (int i = 0; i < n && !cards.isEmpty(); i++) {
            drawn.add(cards.remove(0));
        }
        return drawn;
    }
    /**
     * Adds a single programming card to the bottom of the deck.
     *
     * @param card the card to add
     */
    public void addCard(ProgrammingCard card) {
        cards.add(card);
    }
    /**
     * Returns the current number of cards in the deck.
     *
     * @return the number of cards remaining in the deck
     */
    public int size() {
        return cards.size();
    }
    /**
     * Adds a list of programming cards to the bottom of the deck.
     *
     * @param additionalCards the list of cards to add
     */
    public void addAll(List<ProgrammingCard> additionalCards) {
        cards.addAll(new ArrayList<>(additionalCards));
    }
    /**
     * Removes all cards from the deck, leaving it empty.
     */
    public void clear() {
        cards.clear();
    }
}