package de.lmu.cleverecousins;

import de.lmu.cleverecousins.cards.damageCards.DamageCard;
import de.lmu.cleverecousins.cards.programmingCards.ProgrammingCard;
import de.lmu.util.LogConfigurator;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a robot in the Robo Rally game.
 *
 * Each robot has a position, direction, energy reserve, and a set of programming card registers.
 * The robot can take actions such as moving, rotating, taking damage, rebooting, and interacting
 * with the board (e.g., checkpoints or pits).
 *
 * This class also manages the robot's state during the game, such as whether it is rebooting or destroyed.
 * It is owned by a {@link Player} and interacts closely with the game logic and board tiles.
 */
public class Robot {

    /**
     * Logger used for debugging and tracking important events during game phases.
     * Configured to log detailed information (Level.FINE and above).
     */
    private static final Logger logger = Logger.getLogger(Robot.class.getName());

    // Configure the root logger to show fine-grained debug output
    static {
        LogConfigurator.configureRootLogger(Level.FINE);
    }

    /** Current position of the robot on the board. */
    private Position position;

    /** Current facing direction of the robot. */
    private Direction direction;

    /** Registers holding the programming cards to be executed each round. */
    private final List<ProgrammingCard> registers;

    /** True if the robot is currently rebooting after falling or damage. */
    private boolean isRebooting;

    /** True if the robot has been removed from the game due to damage or a pit. */
    private boolean isDestroyed;

    /** Reference to the current game state (used for some card effects). */
    private Game game;

    /** Amount of energy the robot currently has. */
    private int energyReserve;

    /** Initial spawn/start position for reboot or reset. */
    private Position startPoint;

    /** Index of the currently executing register in the activation phase. */
    private int currentRegisterIndex;

    /** The player who controls this robot. */
    private Player owner;

    /** Internal step counter used for debugging or phased actions. */
    private int step;

    /** The next checkpoint number the robot needs to reach. */
    private int nextCheckpoint = 1;

    /** Total number of checkpoints reached so far. */
    private int checkpointCount = 0;

    /** Direction of the robot's last movement, used for effects like pushing. */
    private Direction lastMoveDirection;

    /** Set of checkpoint numbers the robot has reached, used to prevent duplicates. */
    private Set<Integer> reachedCheckpoints = new HashSet<>();

    /** Returns the index of the currently active register. */
    public int getCurrentRegisterIndex() {
        return currentRegisterIndex;
    }

    /**
     * Constructs a new Robot at a specific position and facing direction.
     *
     * @param position the initial position on the board
     * @param direction the initial facing direction
     */
    public Robot(Position position, Direction direction) {
        this.position = position;
        this.direction = direction;
        this.startPoint = position;
        this.registers = new ArrayList<>(Collections.nCopies(5, null));
        this.isRebooting = false;
        this.isDestroyed = false;
        this.energyReserve = 0;
        this.currentRegisterIndex = -1;
    }

    /**
     * Executes the programming card at the specified register index.
     *
     * @param index the index of the register to execute
     */
    public void executeRegister(int index) {
        if (index >= 0 && index < registers.size()) {
            this.currentRegisterIndex = index;
            ProgrammingCard card = registers.get(index);
        }
    }

    /** Returns the player who controls this robot. */
    public Player getOwner() {
        return owner;
    }

    /**
     * Sets the player who owns this robot.
     *
     * @param owner the controlling player
     */
    public void setOwner(Player owner) {
        this.owner = owner;
    }

    /**
     * Sets a programming card into a specific register slot.
     *
     * @param slot the register slot index (0–4)
     * @param card the programming card to assign
     */
    public void setRegister(int slot, ProgrammingCard card) {
        if (slot >= 0 && slot < registers.size()) {
            registers.set(slot, card);
        }
    }

    /**
     * Gets the programming card in the specified register.
     *
     * @param index the register index
     * @return the card in that slot, or null if none is set
     */
    public ProgrammingCard getRegister(int index) {
        if (index >= 0 && index < registers.size()) {
            return registers.get(index);
        }
        return null;
    }

    /** Checks whether all programming registers are filled with cards. */
    public boolean allRegistersFilled() {
        for (ProgrammingCard card : registers) {
            if (card == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Clears all registers and returns the list of cards that were in them.
     *
     * @return the list of previously set cards
     */
    public List<ProgrammingCard> clearRegisters() {
        List<ProgrammingCard> used = new ArrayList<>();
        for (int i = 0; i < registers.size(); i++) {
            ProgrammingCard card = registers.set(i, null);
            if (card != null) used.add(card);
        }
        return used;
    }

    /**
     * Applies a damage card effect to the robot.
     *
     * @param card the damage card to apply
     */
    public void takeDamage(DamageCard card) {
        card.applyEffect(this, game); // 例如减少能量、锁卡槽等
    }

    /** Reboots the robot, resetting position and clearing destruction. */
    public void reboot() {
        this.isRebooting = true;
        this.isDestroyed = false;
        this.position = startPoint;
        this.direction = Direction.TOP;
    }

    /** Returns whether the robot is currently rebooting. */
    public boolean isRebooting() {
        return isRebooting;
    }

    /** Clears the rebooting status after recovery. */
    public void clearRebooting() {
        this.isRebooting = false;
    }

    /** Returns whether the robot is destroyed and out of the game. */
    public boolean isDestroyed() {
        return isDestroyed;
    }

    /**
     * Sets the destroyed status of the robot.
     *
     * @param destroyed true to mark the robot as destroyed
     */
    public void setDestroyed(boolean destroyed) {
        this.isDestroyed = destroyed;
    }

    /** Returns the robot’s current position on the board. */
    public Position getPosition() {
        return position;
    }

    /** Returns the direction the robot is currently facing. */
    public Direction getDirection() {
        return direction;
    }

    /**
     * Sets the robot's current position.
     *
     * @param position the new position
     */
    public void setPosition(Position position) {
        this.position = position;
    }

    /**
     * Sets the robot's current direction.
     *
     * @param direction the new direction
     */
    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    /** Rotates the robot 90 degrees clockwise. */
    public void rotateClockwise() {
        this.direction = this.direction.turnRight();
    }

    /** Rotates the robot 90 degrees counterclockwise. */
    public void rotateCounterclockwise() {
        this.direction = this.direction.turnLeft();
    }

    /**
     * Moves the robot in a specific direction for a number of steps.
     * Records the direction of the last movement.
     *
     * @param dir the direction to move
     * @param steps number of steps to move
     */
    public void moveInDirection(Direction dir, int steps) {
        for (int i = 0; i < steps; i++) {
            this.lastMoveDirection = dir;   // 记录这次移动的方向
            this.position = this.position.moved(dir);
        }
    }

    /** Returns the direction the robot last moved in. */
    public Direction getLastMoveDirection() {
        return lastMoveDirection;
    }

    /**
     * Instantly turns the robot to a specific direction.
     *
     * @param newDirection the new direction to face
     */
    public void turnTowards(Direction newDirection) {
        this.direction = newDirection;
        logger.fine(() -> "Robot dreht sich zu Richtung: " + newDirection);
    }

    /** Adds one unit of energy to the robot’s reserve (if not rebooting). */
    public void gainEnergy() {
        if (!isRebooting) {
            this.energyReserve += 1;
        }
    }

    /**
     * Adds a specific amount of energy to the robot’s reserve.
     *
     * @param amount number of energy units to gain
     */
    public void gainEnergy(int amount) {
        if (!isRebooting) {
            this.energyReserve += amount;
        }
    }

    /** Returns the robot’s current energy reserve. */
    public int getEnergyReserve() {
        return energyReserve;
    }

    /** Checks if the robot is currently on its fifth register (index 4). */
    public boolean isOnFifthRegister() {
        return currentRegisterIndex == 4;
    }

    /** Resets the robot to its start point and facing north. */
    public void resetToStartPoint() {
        this.position = startPoint;
        this.direction = Direction.TOP;
    }

    /** Returns the robot’s configured starting point. */
    public Position getStartingPoint() {
        return startPoint;
    }

    /**
     * Sets the robot’s starting point.
     *
     * @param pos the new start position
     */
    public void setStartPoint(Position pos) {
        this.startPoint = pos;
    }

    /** Performs a 180-degree U-turn. */
    public void uturn() {
        this.direction = this.direction.uturn();
    }

    /**
     * Marks a checkpoint as reached by number.
     *
     * @param checkpointNumber the number of the checkpoint
     */
    public void reachCheckpoint(int checkpointNumber){
        reachedCheckpoints.add(checkpointNumber);
    }

    /** Removes the robot from the game (e.g. due to falling into a pit). */
    public void removeFromGame(){
        this.setDestroyed(true);
        this.position = null;
    }

    /** Returns a readable string representing the robot's state. */
    @Override
    public String toString() {
        return "Robot {" +
                "pos=" + position +
                ", dir=" + direction +
                ", energy=" + energyReserve +
                ", reboot=" + isRebooting +
                '}';
    }

    /**
     * Sets the position for reboot placement.
     *
     * @param position the reboot target position
     */
    public void setRebootPosition(Position position){
        this.position = position;
    }

    /** Returns the number of the next checkpoint the robot has to reach. */
    public int getNextCheckpoint() {
        return nextCheckpoint;
    }

    /**
     * Increments the nextCheckpoint if the correct checkpoint was reached.
     *
     * @param count the reached checkpoint number
     */
    public void reachedCheckpoint(int count) {
        if (count == nextCheckpoint) {
            nextCheckpoint++;  // Spieler muss als nächstes den nächsten erreichen
        }
    }

    /**
     * Increments and returns the total number of checkpoints reached.
     *
     * @return the checkpoint count before increment
     */
    public int addCheckpointCount() {
        return checkpointCount++;
    }

    /** Returns how many checkpoints the robot has reached so far. */
    public int getCheckpointCount() {
        return checkpointCount;
    }
}
