package de.lmu.cleverecousins;

import de.lmu.cleverecousins.cards.DiscardDeck;
import de.lmu.cleverecousins.cards.ProgrammingDeck;
import de.lmu.cleverecousins.cards.damageCards.DamageCard;
import de.lmu.cleverecousins.cards.programmingCards.ProgrammingCard;
import de.lmu.util.LogConfigurator;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a single participant in the game, tying together a {@link Robot},
 * that robot's card piles (draw and discard), the player's current hand, and
 * progress-related values such as checkpoints and energy.
 * <p>
 * The {@code Player} is responsible for drawing and discarding cards, programming
 * its robot's registers, handling damage/reboot states, and tracking its
 * starting point for reboot purposes.
 */
public class Player {

    /** Logger for debugging player actions. */
    private static final Logger logger = Logger.getLogger(Player.class.getName());

    static {
        LogConfigurator.configureRootLogger(Level.FINE);
    }

    /** Unique server-side identifier for this player. */
    private final int clientID;

    /** The robot controlled by this player. */
    private final Robot robot;

    /** The deck from which programming cards are drawn. */
    private final ProgrammingDeck drawPile;

    /** The discard pile for used/dropped programming and damage cards. */
    private final DiscardDeck discardDeck;

    //private final List<UpgradeCard> upgradeSlots;

    /** The cards currently in the player's hand. */
    private final List<ProgrammingCard> hand;

    /** How many checkpoints the player has reached so far. */
    private int checkpointsReached;

    /** Energy reserve the player can spend (e.g., on upgrades). */
    private int energyReserve;

    /** Initial spawn point used for rebooting. */
    private Position startPoint;

    /** Flag indicating whether the player already chose a starting point. */
    private boolean hasChosenStartPoint = false;

    /**
     * @return {@code true} if the player has already selected a starting point
     */
    public boolean hasChosenStartPoint() {
        return hasChosenStartPoint;
    }

    /**
     * Sets whether the player selected a starting point.
     *
     * @param chosen {@code true} if chosen
     */
    public void setHasChosenStartPoint(boolean chosen) {
        this.hasChosenStartPoint = chosen;
    }

    /**
     * @return this player's client ID
     */
    public int getClientID() {
        return clientID;
    }


    // -----------------------------------------------------------------
    // Constructors
    // -----------------------------------------------------------------


    /**
     * Creates a new player instance.
     *
     * @param clientID  unique client identifier from the server
     * @param robot     robot controlled by this player
     * @param startPoint starting position (also used as reboot point)
     */
    public Player(int clientID, Robot robot, Position startPoint) {
        this.clientID = clientID;
        this.robot = robot;
        this.drawPile = new ProgrammingDeck();
        this.discardDeck = new DiscardDeck();
        // this.upgradeSlots = new ArrayList<>();
        this.hand = new ArrayList<>();
        this.checkpointsReached = 0;
        this.energyReserve = 0;
        this.startPoint = startPoint;
        this.robot.setPosition(startPoint);
        this.robot.setStartPoint(startPoint);
        this.robot.setOwner(this);
    }

    // -----------------------------------------------------------------
    // Deck / Hand Management
    // -----------------------------------------------------------------

    /**
     * Shuffles the draw pile to prepare the next round.
     */
    public void prepareNextRoundDeck() {
        drawPile.shuffle();
    }

    /**
     * Draws up to 9 programming cards into the hand, shuffling the discard pile back
     * into the draw pile if necessary.
     *
     * @return {@code true} if a reshuffle of the discard pile was required, {@code false} otherwise
     */
    public boolean drawCards() {
        logger.fine(() -> String.format("[DEBUG] drawCards() START - DrawPiled=%d, DiscardDeck=%d",
                drawPile.size(), discardDeck.size()));
        hand.clear();
        List<ProgrammingCard> drawn = drawPile.draw(9);
        hand.addAll(drawn);
        logger.fine(() -> String.format("[DEBUG] Nach erstem Ziehen - Hand=%d, DrawPiled=%d, DiscardDeck=%d",
                hand.size(), drawPile.size(), discardDeck.size()));
        boolean hadToShuffle = false;
        if (hand.size() < 9) {
            logger.fine("[DEBUG] Nicht genug Karten im DrawPile, shuffeln DiscardDeck rein...");
            discardDeck.shuffleInto(drawPile);
            drawPile.shuffle();
            hand.addAll(drawPile.draw(9 - hand.size()));
            hadToShuffle = true;
            logger.fine(() -> String.format("[DEBUG] Nach Shuffle & Nachziehen - Hand=%d, DrawPile=%d, DiscardDeck=%d",
                    hand.size(), drawPile.size(), discardDeck.size()));
        }
        return hadToShuffle;
    }

    /**
     * Discards the entire hand into the discard pile.
     */
    public void discardHand() {
        discardDeck.addAll(new ArrayList<>(hand));
        logger.fine(() -> "[DEBUG] discardHand(): " + hand.size() + " Karten ins DiscardDecl gelegt, jetzt Größe=" + discardDeck.size());
        hand.clear();
    }

    /**
     * Discards all cards currently in the robot's registers into the discard pile.
     */
    public void discardUsedCards() {
        List<ProgrammingCard> usedCards = new ArrayList<>(robot.clearRegisters());
        discardDeck.addAll(usedCards);
        logger.fine(() -> "[DEBUG] discardUsedCards(): " + usedCards.size() + " Karten ins DiscardDeck gelegt, jetzt Größe=" + discardDeck.size());
    }

    /**
     * Programs the robot's five registers from the given list of cards and discards the rest.
     *
     * @param chosenFive exactly five cards chosen from the player's hand
     * @throws IllegalArgumentException if the selection does not contain exactly five cards or
     *                                   if any of them are not in the hand
     */
    public void programFromHand(List<ProgrammingCard> chosenFive) {
        if (chosenFive.size() != 5 || !hand.containsAll(chosenFive)) {
            throw new IllegalArgumentException("非法编程选择");
        }
        for (int i = 0; i < 5; i++) {
            robot.setRegister(i, chosenFive.get(i));
        }
        for (ProgrammingCard card : hand) {
            if (!chosenFive.contains(card)) {
                discardDeck.add(card);
            }
        }
        hand.clear();
    }

    /**
     * Adds a damage card to the discard pile (to be shuffled in later).
     *
     * @param card damage card received
     */
    public void addDamageCard(DamageCard card) {
        discardDeck.add(card);
    }
    /*
    public boolean purchaseUpgrade(UpgradeCard card, int cost) { if (energyReserve >= cost) { energyReserve -= cost; upgradeSlots.add(card); return true; } return false; }
    */

    /**
     * Increments the number of checkpoints reached by this player.
     */
    public void reachCheckpoint() {
        checkpointsReached++;
    }

    /**
     * Increases the player's energy reserve and syncs the robot state.
     *
     * @param amount energy gained
     */
    public void gainEnergy(int amount) {
        energyReserve += amount;
        robot.gainEnergy(); // 同步机器人状态
    }

    /**
     * Reboots the robot to the original starting point, resets orientation,
     * clears registers and marks the robot as not destroyed.
     */
    public void rebootToStartPoint() {
        robot.setPosition(startPoint);
        robot.setDirection(Direction.TOP); // 默认方向
        robot.setDestroyed(false);
        robot.clearRegisters();
        this.isRebooting = false;
    }

    /**
     * Clears the player's hand and all robot registers. Typically used when resetting state.
     */
    public void resetAllRegisters() {
        hand.clear();
        robot.clearRegisters();
    }

    /**
     * Sets (or clears) a card in a specific robot register by name.
     *
     * @param cardName      name of the card to place; {@code null} to clear the slot
     * @param registerIndex index (0-4) of the register to modify
     * @return {@code true} if the register was set/cleared successfully; {@code false} if the card was not found
     */
    public boolean setRegisterCard(String cardName, int registerIndex) {
        if (registerIndex < 0 || registerIndex > 4) return false;
        if (cardName == null) {
            robot.setRegister(registerIndex, null);
            return true;
        }
        for (ProgrammingCard card : hand) {
            if (card.getName().equals(cardName)) {
                robot.setRegister(registerIndex, card);
                return true;
            }
        }
        return false; // Karte nicht in Hand gefunden
    }

    // -----------------------------------------------------------------
    // Getters / Setters
    // -----------------------------------------------------------------

    /**
     * @return the robot controlled by this player
     */
    public Robot getRobot() {
        return robot;
    }

    /**
     * @return the player's current hand (modifiable list)
     */
    public List<ProgrammingCard> getHand() {
        return hand;
    }

    /**
     * @return number of checkpoints reached
     */
    public int getCheckpointsReached() {
        return checkpointsReached;
    }

    /**
     * @return the draw pile
     */
    public ProgrammingDeck getDrawPile() {
        return drawPile;
    }

    /**
     * @return the discard deck
     */
    public DiscardDeck getDiscardDeck() {
        return discardDeck;
    }

    /**
     * @return current energy reserve value
     */
    public int getEnergyReserve() {
        return energyReserve;
    }

    /**
     * @return the starting point used for spawning/rebooting
     */
    public Position getStartPoint() {
        return startPoint;
    }

    /**
     * Updates the starting point and synchronizes the robot's internal start point.
     *
     * @param startPoint new starting position
     */
    public void setStartPoint(Position startPoint) {
        this.startPoint = startPoint;
        this.robot.setStartPoint(startPoint);
    }

    /**
     * Returns a human‑readable representation for debugging/logging.
     * Format: {@code Player{energy=<energy>, checkpoints=<checkpoints>, robot=<robot>}}
     *
     * @return string with the most relevant player fields
     */
    @Override
    public String toString() {
        return "Player{" +
                "energy=" + energyReserve +
                ", checkpoints=" + checkpointsReached +
                ", robot=" + robot +
//", upgrades=" + upgradeSlots +
                '}';
    }

    /** Flag indicating whether the player/robot is currently rebooting. */
    private boolean isRebooting = false;

    /**
     * @return {@code true} if the player/robot is currently rebooting
     */
    public boolean isRebooting() {
        return isRebooting;
    }

    /**
     * Sets the rebooting flag.
     *
     * @param rebooting {@code true} if reboot is in progress
     */
    public void setRebooting(boolean rebooting) {
        isRebooting = rebooting;
    }
}