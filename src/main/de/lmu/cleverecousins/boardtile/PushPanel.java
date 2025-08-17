package de.lmu.cleverecousins.boardtile;

import de.lmu.cleverecousins.Direction;
import de.lmu.cleverecousins.Position;
import de.lmu.cleverecousins.Robot;

import java.util.List;

public class PushPanel extends BoardTile{

    private Direction pushDirection;
    private List<Integer> activeRegisters;


    public PushPanel(Position position, Direction direction, List<Integer> registers){
        super(position);
        this.pushDirection = direction;
        this.activeRegisters = registers;
    }

    @Override
    public void activate(Robot robot) {
        int currentRegister = robot.getCurrentRegisterIndex();
        if (currentRegister >= 0 && activeRegisters.contains(currentRegister)) {
            robot.moveInDirection(pushDirection, 1);
        }
    }

    public Direction getPushDirection(){
        return pushDirection;
    }
}
