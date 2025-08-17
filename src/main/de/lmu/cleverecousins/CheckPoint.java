package de.lmu.cleverecousins;

public class CheckPoint {

    private final int number;
    private final Position position;

    public CheckPoint(int number, Position position){
        this.number = number;
        this.position = position;
    }

    public int getNumber(){
        return number;
    }

    public Position getPosition(){
        return position;
    }

    public boolean isReachedBy(Robot robot){
        return this.position.equals(robot.getPosition());
    }

    @Override
    public String toString(){
        return "Checkpoint " + number + " at " + position;
    }
}
