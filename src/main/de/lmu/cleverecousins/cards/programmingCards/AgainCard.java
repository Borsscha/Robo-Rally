package de.lmu.cleverecousins.cards.programmingCards;

import de.lmu.cleverecousins.GamePhaseController;
import de.lmu.cleverecousins.Robot;
import de.lmu.cleverecousins.cards.Card;
import de.lmu.util.LogConfigurator;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Concrete programming card "Again".
 *
 * When executed, this card repeats the effect of the previously played programming card
 * in the robot's register.
 *
 * Inherits from {@link ProgrammingCard}, which itself extends {@link Card}.
 */
public class AgainCard extends ProgrammingCard {

    private static final Logger logger = Logger.getLogger(AgainCard.class.getName());

    static {
        LogConfigurator.configureRootLogger(Level.FINE);
    }

    public AgainCard() {
        super("Again", "Repeat last register action");
    }

    /**
     * Returns the path to the image representing the Again card.
     *
     * @return the full resource path to the card image
     */
    @Override
    public String getImagePath() {
        // Pfad zu src/main/resources/cards/images/again.png
        return getClass().getResource("/cards/images/again.png").toExternalForm();
    }

    /**
     * Executes the effect of the Again card for the given robot.
     *
     * This card repeats the last executed programming card in the robot's register,
     * if such a card exists and is valid.
     *
     * @param robot the robot executing the card
     * @param controller the game phase controller that manages register execution
     */
    @Override
    public void execute(Robot robot, GamePhaseController controller) {
        int lastIndex = controller.getGame().getCurrentRegister() - 1;
        if (lastIndex < 0) {
            logger.fine("[DEBUG] AGAIN: Kein vorheriges Register.");
            return;
        }

        ProgrammingCard lastCard = robot.getRegister(lastIndex);
        if (lastCard == null || lastCard == this) {
            logger.fine("[DEBUG] AGAIN: Keine gültige Karte im vorherigen Register.");
            return;
        }
        logger.fine("[DEBUG]AGAIN: Wiederhole Karte: " + lastCard.getName());
        lastCard.execute(robot, controller);
    }
}


