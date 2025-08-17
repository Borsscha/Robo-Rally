package de.lmu.cleverecousins.cards.damageCards;

import de.lmu.cleverecousins.Game;
import de.lmu.cleverecousins.Robot;
import de.lmu.util.LogConfigurator;

import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Concrete damage card "SPAM".
 *
 * Represents a damage card that blocks a programming register slot.
 * Does not directly alter the game state but serves as a placeholder effect.
 */
public class Spam extends DamageCard{

    private static final Logger logger = Logger.getLogger(Spam.class.getName());

    static {
        LogConfigurator.configureRootLogger(Level.FINE);
    }

    /**
     * Constructs a SPAM damage card with a predefined name and description.
     */
    public Spam() {
        super("SPAM", "Blocks a programming register");
    }

    /**
     * Returns the path to the image representing the SPAM card.
     *
     * @return the full resource path to the card image
     */
    @Override
    public String getImagePath() {
        // Pfad zu src/main/resources/cards/images/spam.png
        return getClass().getResource("/cards/images/spam.png").toExternalForm();
    }

    /**
     * Applies the effect of the SPAM card to the given robot.
     *
     * This implementation logs the effect but does not modify the robot's state.
     *
     * @param robot the robot affected by the SPAM card
     * @param game the current game context
     */
    @Override
    public void applyEffect(Robot robot, Game game) {
        logger.info("SPAM card applied to robot " + robot);
    }
}

