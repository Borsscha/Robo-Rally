package de.lmu.cleverecousins.cards.programmingCards;

import de.lmu.cleverecousins.GamePhaseController;
import de.lmu.cleverecousins.Robot;
import de.lmu.cleverecousins.cards.Card;

/**
 * Abstrakte Basis für alle Programmierkarten.
 * Erweitert Card, damit die GUI und das ViewModel sie anzeigen können.
 */
public abstract class ProgrammingCard extends Card {

    private final String name;
    private final String description;

    public ProgrammingCard(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return name + " – " + description;
    }

    /**
     * Liefert den Pfad zum Bild der Karte als URL.
     * Muss in jeder konkreten Karte implementiert werden.
     */
    @Override
    public abstract String getImagePath();

    /**
     * Führt die Aktion dieser Programmierkarte auf dem Roboter im Spiel aus.
     */
    public abstract void execute(Robot robot, GamePhaseController controller); //Robot robot
}


