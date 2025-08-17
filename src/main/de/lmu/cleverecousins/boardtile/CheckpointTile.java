package de.lmu.cleverecousins.boardtile;

import de.lmu.cleverecousins.Position;
import de.lmu.cleverecousins.Robot;


public class CheckpointTile extends BoardTile{

    private int checkpointNumber;

    public CheckpointTile(Position position, int number){
        super(position);
        this.checkpointNumber = number;
    }

    @Override
    public void activate(Robot robot){
        robot.reachCheckpoint(checkpointNumber);
    }

    public int getCheckpointNumber(){
        return checkpointNumber;
    }
}
