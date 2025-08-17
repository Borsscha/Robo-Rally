package de.lmu.cleverecousins.cards.programmingCards;

import de.lmu.cleverecousins.GamePhaseController;
import de.lmu.cleverecousins.Robot;

/**
 * Konkrete Programmierkarte „Turn Left“.
 * Erbt von ProgrammingCard (und damit von Card).
 */
public class TurnLeftCard extends ProgrammingCard {

    public TurnLeftCard() {
        super("TurnLeft", "Rotates the robot 90° to the left");
    }

    @Override
    public String getImagePath() {
        // Pfad zu src/main/resources/cards/images/turnleft.png
        return getClass().getResource("/cards/images/turnleft.png").toExternalForm();
    }
    @Override
    public void execute(Robot robot, GamePhaseController controller) {
        robot.rotateCounterclockwise();
    }
}


