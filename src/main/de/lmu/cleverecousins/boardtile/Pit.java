package de.lmu.cleverecousins.boardtile;

import de.lmu.cleverecousins.Position;
import de.lmu.cleverecousins.Robot;

public class Pit extends BoardTile{

    public Pit(Position position){
        super(position);
    }

    @Override
    public void activate(Robot robot){
        robot.setDestroyed(true);
        robot.removeFromGame();
    }
}
