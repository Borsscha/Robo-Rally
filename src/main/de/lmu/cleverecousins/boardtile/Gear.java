package de.lmu.cleverecousins.boardtile;

import de.lmu.cleverecousins.Position;
import de.lmu.cleverecousins.Robot;


public class Gear extends BoardTile{

    private boolean clockwise;

    public Gear(Position position, boolean clockwise){
        super(position);
        this.clockwise = clockwise;
    }

    @Override
    public void activate(Robot robot){
        if(clockwise){
            robot.rotateClockwise();
        }
        else{
            robot.rotateCounterclockwise();
        }
    }
}
