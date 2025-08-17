package de.lmu.cleverecousins.cards.programmingCards;

/**
 * Factory class for creating instances of {@link ProgrammingCard} based on card names.
 *
 * Supports multiple aliases for each card type and returns {@code null} if the name is unrecognized.
 */
public class CardFactory {

    /**
     * Creates a {@link ProgrammingCard} instance based on the provided card name.
     * @param cardName the name (or alias) of the card to create
     * @return a new {@link ProgrammingCard} instance, or {@code null} if the name is unrecognized
     */
    public static ProgrammingCard create(String cardName) {
        return switch (cardName) {
            case "Again" -> new AgainCard();
            case "BackUp", "Back Up" -> new BackUpCard();
            case "MoveI", "MoveOne" -> new MoveOneCard();
            case "MoveII", "MoveTwo" -> new MoveTwoCard();
            case "MoveIII", "MoveThree" -> new MoveThreeCard();
            case "TurnLeft" -> new TurnLeftCard();
            case "TurnRight" -> new TurnRightCard();
            case "UTurn" -> new UTurnCard();
            case "PowerUp" -> new PowerUpCard();
            default -> null;  // bei unbekanntem Namen null zurückgeben
        };
    }
}
