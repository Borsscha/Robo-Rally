package de.lmu.cleverecousins.cards.programmingCards;

import de.lmu.cleverecousins.GamePhaseController;
import de.lmu.cleverecousins.Robot;

/**
 * Concrete programming card "Move One".
 *
 * When executed, moves the robot forward by one space in the direction it is currently facing.

 */
public class MoveOneCard extends ProgrammingCard {


    public MoveOneCard() {
        super("MoveI", "Moves your robot forward by one space");
    }

    /**
     * Returns the path to the image representing the Move One card.
     *
     * @return the full resource path to the card image
     */
    @Override
    public String getImagePath() {
        // Pfad zu src/main/resources/cards/images/moveone.png
        return getClass().getResource("/cards/images/moveone.png").toExternalForm();
    }

    /**
     * Executes the effect of the Move One card for the given robot.
     *
     * Moves the robot one space forward based on its current orientation.
     *
     * @param robot the robot executing the card
     * @param controller the game phase controller handling the movement
     */
    @Override
    public void execute(Robot robot, GamePhaseController controller) {
        controller.executeMoveForward(robot, 1);
    }
}
