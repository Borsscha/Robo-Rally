package de.lmu.cleverecousins.cards.programmingCards;

import de.lmu.cleverecousins.GamePhaseController;
import de.lmu.cleverecousins.Robot;

/**
 * Konkrete Programmierkarte „U-Turn“.
 * Erbt von ProgrammingCard (und damit von Card).
 */
public class UTurnCard extends ProgrammingCard {

    public UTurnCard() {
        super("UTurn", "Turns 180 degrees");
    }

    @Override
    public String getImagePath() {
        // Pfad zu src/main/resources/cards/images/uturn.png
        return getClass().getResource("/cards/images/uturn.png").toExternalForm();
    }
    @Override
    public void execute(Robot robot, GamePhaseController controller) {
        robot.uturn();
    }
}


