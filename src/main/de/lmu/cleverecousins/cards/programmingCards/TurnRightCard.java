package de.lmu.cleverecousins.cards.programmingCards;

import de.lmu.cleverecousins.GamePhaseController;
import de.lmu.cleverecousins.Robot;

/**
 * Konkrete Programmierkarte „Turn Right“.
 * Erbt von ProgrammingCard (und damit von Card).
 */
public class TurnRightCard extends ProgrammingCard {

    public TurnRightCard() {
        super("TurnRight", "Rotates the robot 90° to the right");
    }

    @Override
    public String getImagePath() {
        // Pfad zu src/main/resources/cards/images/turnright.png
        return getClass().getResource("/cards/images/turnright.png").toExternalForm();
    }
    @Override
    public void execute(Robot robot, GamePhaseController controller) {
        robot.rotateClockwise();
    }
}
