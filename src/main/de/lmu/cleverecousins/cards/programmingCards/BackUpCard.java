package de.lmu.cleverecousins.cards.programmingCards;

import de.lmu.cleverecousins.GamePhaseController;
import de.lmu.cleverecousins.Robot;

/**
 * Concrete programming card "Back Up".
 *
 * When executed, moves the robot one space backward relative to its current facing direction.
 */
public class BackUpCard extends ProgrammingCard {


    /**
     * Constructs a "Back Up" card with a predefined name and description.
     */
    public BackUpCard() {
        super("Back Up", "Moves your robot backwards by one space");
    }

    /**
     * Returns the path to the image representing the Back Up card.
     *
     * @return the full resource path to the card image
     */
    @Override
    public String getImagePath() {
        // Pfad zu src/main/resources/cards/images/backup.png
        return getClass().getResource("/cards/images/backup.png").toExternalForm();
    }

    /**
     * Executes the effect of the Back Up card for the given robot.
     *
     * Moves the robot one space backward based on its current orientation.
     *
     * @param robot the robot executing the card
     * @param controller the game phase controller that handles movement logic
     */
    @Override
    public void execute(Robot robot, GamePhaseController controller) {
        controller.executeMoveBackward(robot, 1);
    }
}

