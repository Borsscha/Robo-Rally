package de.lmu.cleverecousins.cards.programmingCards;

import de.lmu.cleverecousins.GamePhaseController;
import de.lmu.cleverecousins.Robot;

/**
 * When executed, moves the robot forward by two spaces in its current direction
 */
public class MoveTwoCard extends ProgrammingCard {

    public MoveTwoCard() {
        super("MoveII", "Moves your robot forward by two spaces");
    }

    /**
     * Returns the path to the image representing the Move Two card.
     *
     * @return the full resource path to the card image
     */
    @Override
    public String getImagePath() {
        // Pfad zu src/main/resources/cards/images/movetwo.png
        return getClass().getResource("/cards/images/movetwo.png").toExternalForm();
    }

    /**
     * Executes the effect of the Move Two card for the given robot.
     *
     * Moves the robot forward by two spaces based on its current orientation.
     *
     * @param robot the robot executing the card
     * @param controller the game phase controller managing robot actions
     */
    @Override
    public void execute(Robot robot, GamePhaseController controller) {
        controller.executeMoveForward(robot, 2);
    }
}


