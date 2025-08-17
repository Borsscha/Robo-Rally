package de.lmu.cleverecousins.cards.damageCards;

import de.lmu.cleverecousins.Game;
import de.lmu.cleverecousins.Player;
import de.lmu.cleverecousins.Position;
import de.lmu.cleverecousins.Robot;
import de.lmu.util.LogConfigurator;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Concrete damage card "VIRUS".
 *
 * When applied, all other robots within a 6-tile radius receive a VIRUS card
 * in their discard pile.
 */
public class Virus extends DamageCard {

    private static final Logger logger = Logger.getLogger(Virus.class.getName());

    static {
        LogConfigurator.configureRootLogger(Level.FINE);
    }

    public Virus() {
        super("VIRUS", "Every Robot within a 6-space radius must take a VIRUS card");
    }

    /**
     * Returns the path to the image representing the VIRUS card.
     *
     * @return the full resource path to the card image
     */
    @Override
    public String getImagePath() {
        // Pfad zu src/main/resources/cards/images/virus.png
        return getClass().getResource("/cards/images/virus.png").toExternalForm();
    }

    /**
     * Applies the effect of the VIRUS card to the given robot.
     *
     * All other robots within a 6-tile radius from the source robot's position
     * receive one VIRUS card in their discard pile.
     *
     * @param robot the robot affected by the VIRUS card (source of infection)
     * @param game the current game context used to locate other players and robots
     */
    @Override
    public void applyEffect(Robot robot, Game game) {
        logger.fine("→ Applied VIRUS to robot " + robot);
        Position sourcePos = robot.getPosition();

        for (Player p : game.getAllPlayers()) {
            Robot target = p.getRobot();
            if (target == robot) continue;

            double dist = sourcePos.euclideanDistanceTo(target.getPosition());
            if (dist <= 6.0) {
                p.getDiscardDeck().add(new Virus());
            }
        }
    }
}

