package de.lmu.cleverecousins.cards;
import java.util.Collections;
import java.util.LinkedList;

/**
 * A generic deck class representing a collection of cards of any type.
 *
 * Provides basic operations such as drawing cards, shuffling, adding cards, and querying the deck size.
 *
 * @param <T> the type of card stored in this deck
 */
public class Deck<T> {

    protected LinkedList<T> cards;

    /**
     * Constructs a new deck initialized with the given list of cards.
     *
     * @param cards the initial cards to include in the deck
     */
    public Deck(LinkedList<T> cards) {
        this.cards = new LinkedList<>(cards);
    }

    /**
     * Constructs an empty deck.
     */
    public Deck() {
        this.cards = new LinkedList<>();
    }

    /**
     * Draws and removes up to {@code amount} cards from the top of the deck.
     *
     * @param amount the number of cards to draw
     * @return a list of drawn cards (may contain fewer than {@code amount} if the deck is smaller)
     */
    public LinkedList<T> draw(int amount){
        LinkedList<T> drawn = new LinkedList<>();
        for (int i = 0; i < amount && !cards.isEmpty(); i++) {
            drawn.add(cards.removeFirst());
        }
        return drawn;
    }

    /**
     * Randomly shuffles the cards in the deck.
     */
    public void shuffle() {
        Collections.shuffle(cards);
    }

    /**
     * Adds a new card to the bottom of the deck.
     *
     * @param newCard the card to add
     */
    public void addCard(T newCard) {
        cards.add(newCard);

    }

    /**
     * Returns the number of cards currently in the deck.
     *
     * @return the deck size
     */
    public int size() {
        return cards.size();
    }

}
