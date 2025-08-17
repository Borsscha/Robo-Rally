package de.lmu.cleverecousins.cards.programmingCards;

import de.lmu.cleverecousins.GamePhaseController;
import de.lmu.cleverecousins.Robot;

/**
 * When executed, the robot gains 1 energy.
 */
public class PowerUpCard extends ProgrammingCard {


    public PowerUpCard() {
        super("PowerUp", "Gain 1 energy");
    }

    /**
     * Returns the path to the image representing the Power Up card.
     *
     * @return the full resource path to the card image
     */
    @Override
    public String getImagePath() {
        // Pfad zu src/main/resources/cards/images/powerup.png
        return getClass().getResource("/cards/images/powerup.png").toExternalForm();
    }

    /**
     * Executes the effect of the Power Up card for the given robot.
     *
     * This card increases the robot's energy by 1 unit.
     *
     * @param robot the robot executing the card
     * @param controller the game phase controller (not used directly in this effect)
     */
    @Override
    public void execute(Robot robot, GamePhaseController controller) {
        robot.gainEnergy();
    }
}


