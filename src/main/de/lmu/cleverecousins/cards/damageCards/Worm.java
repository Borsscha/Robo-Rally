package de.lmu.cleverecousins.cards.damageCards;

import de.lmu.cleverecousins.Game;
import de.lmu.cleverecousins.Robot;
import de.lmu.util.LogConfigurator;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Concrete damage card "WORM".
 *
 * When applied, the affected robot is rebooted.
 *
 * Inherits from {@link DamageCard} for GUI and ViewModel integration.
 */
public class Worm extends DamageCard {

    private static final Logger logger = Logger.getLogger(Worm.class.getName());

    static {
        LogConfigurator.configureRootLogger(Level.FINE);
    }

    public Worm() {
        super("WORM", "Reboot your Robot");
    }

    /**
     * Returns the path to the image representing the WORM card.
     *
     * @return the full resource path to the card image
     */
    @Override
    public String getImagePath() {
        // Pfad zu src/main/resources/cards/images/worm.png
        return getClass().getResource("/cards/images/worm.png").toExternalForm();
    }

    /**
     * Applies the effect of the WORM card to the given robot.
     *
     * This causes the robot to reboot (usually teleporting it back to its start position).
     *
     * @param robot the robot affected by the WORM card
     * @param game the current game context (not directly used here)
     */
    @Override
    public void applyEffect(Robot robot, Game game) {
        logger.fine("WORM card applied to robot: " + robot);
        robot.reboot();
    }
}

