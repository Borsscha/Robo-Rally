package de.lmu.cleverecousins.cards.damageCards;

import de.lmu.cleverecousins.Game;
import de.lmu.cleverecousins.Player;
import de.lmu.cleverecousins.Robot;
import de.lmu.util.LogConfigurator;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Concrete damage card "TROJAN HORSE".
 *
 * When applied, adds two SPAM cards to the affected player's discard pile.
 */
public class TrojanHorse extends DamageCard{

    private static final Logger logger = Logger.getLogger(TrojanHorse.class.getName());

    static {
        LogConfigurator.configureRootLogger(Level.FINE);
    }

    public TrojanHorse() {
        super("TROJAN HORSE", "Take 2 SPAM cards");
    }

    /**
     * Returns the path to the image representing the TROJAN HORSE card.
     *
     * @return the full resource path to the card image
     */
    @Override
    public String getImagePath() {
        // Pfad zu src/main/resources/cards/images/trojanhorse.png
        return getClass().getResource("/cards/images/trojanhorse.png").toExternalForm();
    }

    /**
     * Applies the effect of the TROJAN HORSE card to the given robot.
     *
     * This effect adds two SPAM cards to the discard pile of the player who owns the robot.
     *
     * @param robot the robot affected by the TROJAN HORSE card
     * @param game the current game context used to resolve the player
     */
    @Override
    public void applyEffect(Robot robot, Game game) {
        logger.info("TROJAN HORSE card applied to robot " + robot);
        Player player = game.getPlayerByRobot(robot);
        if (player != null) {
            player.getDiscardDeck().add(new Spam());
            player.getDiscardDeck().add(new Spam());
        }
    }
}
