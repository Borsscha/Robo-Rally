package de.lmu.cleverecousins.boardtile;

import de.lmu.cleverecousins.Position;
import de.lmu.cleverecousins.Robot;

public abstract class BoardTile {

    protected Position position;

    ///Wände an den vier Kanten
    protected boolean wallNorth;
    protected boolean wallEast;
    protected boolean wallSouth;
    protected boolean wallWest;

    public BoardTile(Position position) {
        this.position = position;
    }

    public Position getPosition() {
        return position;
    }

    public boolean hasWallNorth() {
        return wallNorth;
    }

    public boolean hasWallEast() {
        return wallEast;
    }

    public boolean hasWallSouth() {
        return wallSouth;
    }

    public boolean hasWallWest() {
        return wallWest;
    }

    ///Wird überschrieben von Pit, CheckpointTile, Gear und PushPanel
    public abstract void activate(Robot robot);

}
