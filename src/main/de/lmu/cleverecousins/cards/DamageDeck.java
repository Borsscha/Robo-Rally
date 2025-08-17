package de.lmu.cleverecousins.cards;

import de.lmu.cleverecousins.cards.damageCards.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a deck of different types of damage cards used in the game.
 *
 * The deck consists of four separate piles: Spam, TrojanHorse, Virus, and Worm.
 * Each type is stored, drawn, and managed independently.
 */
public class DamageDeck {
    private final List<Spam> spamPile;
    private final List<TrojanHorse> trojanPile;
    private final List<Virus> virusPile;
    private final List<Worm> wormPile;

    /**
     * Creates and initializes the damage deck with a predefined number of cards
     * Each pile is shuffled individually.
     */
    public DamageDeck() {
        this.spamPile = new ArrayList<>();
        this.trojanPile = new ArrayList<>();
        this.virusPile = new ArrayList<>();
        this.wormPile = new ArrayList<>();

        // Initial deck sizes
        for (int i = 0; i < 10; i++) spamPile.add(new Spam());
        for (int i = 0; i < 5; i++) trojanPile.add(new TrojanHorse());
        for (int i = 0; i < 5; i++) virusPile.add(new Virus());
        for (int i = 0; i < 5; i++) wormPile.add(new Worm());

        // Shuffle decks
        Collections.shuffle(spamPile);
        Collections.shuffle(trojanPile);
        Collections.shuffle(virusPile);
        Collections.shuffle(wormPile);
    }

    /**
     * Draws and removes a Spam card from the pile.
     *
     * @return a Spam card, or {@code null} if the pile is empty
     */
    public Spam drawSpam() {
        return spamPile.isEmpty() ? null : spamPile.remove(0);
    }

    /**
     * Draws and removes a TrojanHorse card from the pile.
     *
     * @return a TrojanHorse card, or {@code null} if the pile is empty
     */
    public TrojanHorse drawTrojanHorse() {
        return trojanPile.isEmpty() ? null : trojanPile.remove(0);
    }

    /**
     * Draws and removes a Virus card from the pile.
     *
     * @return a Virus card, or {@code null} if the pile is empty
     */
    public Virus drawVirus() {
        return virusPile.isEmpty() ? null : virusPile.remove(0);
    }

    /**
     * Draws and removes a Worm card from the pile.
     *
     * @return a Worm card, or {@code null} if the pile is empty
     */
    public Worm drawWorm() {
        return wormPile.isEmpty() ? null : wormPile.remove(0);
    }


    public void addSpam(Spam spam) {
        spamPile.add(spam);
    }

    public void addTrojanHorse(TrojanHorse trojan) {
        trojanPile.add(trojan);
    }

    public void addVirus(Virus virus) {
        virusPile.add(virus);
    }

    public void addWorm(Worm worm) {
        wormPile.add(worm);
    }

    // For testing
    public int countSpam() {
        return spamPile.size();
    }

    public int countTrojanHorse() {
        return trojanPile.size();
    }

    public int countVirus() {
        return virusPile.size();
    }

    public int countWorm() {
        return wormPile.size();
    }

}

