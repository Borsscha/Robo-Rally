package de.lmu.cleverecousins.cards.programmingCards;

import de.lmu.cleverecousins.GamePhaseController;
import de.lmu.cleverecousins.Robot;


public class MoveThreeCard extends ProgrammingCard {

    /**
    Concrete programming card "Move Three".
     When executed, moves the robot forward by three spaces in its current direction.

     */
    public MoveThreeCard() {
        super("MoveIII", "Moves 3 steps forward");
    }

    /**
     * Returns the path to the image representing the Move Three card.
     *
     * @return the full resource path to the card image
     */
    @Override
    public String getImagePath() {
        // Pfad zu src/main/resources/cards/images/movethree.png
        return getClass().getResource("/cards/images/movethree.png").toExternalForm();
    }

    /**
     * Executes the effect of the Move Three card for the given robot.
     *
     * Moves the robot forward by three spaces based on its current orientation.
     *
     * @param robot the robot executing the card
     * @param controller the game phase controller managing robot actions
     */
    @Override
    public void execute(Robot robot, GamePhaseController controller) {
        controller.executeMoveForward(robot, 3);
    }
}
