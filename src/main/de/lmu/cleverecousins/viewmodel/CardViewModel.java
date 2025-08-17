package de.lmu.cleverecousins.viewmodel;

import de.lmu.cleverecousins.cards.Card;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

/**
 * ViewModel for the card programming UI.
 * <p>
 * Holds the player's hand cards and the five register slots, plus flags controlling
 * whether selection is currently allowed/finished. Designed to be observed/bound
 * from JavaFX views.
 */
public class CardViewModel {

    /** Observable list of cards currently in hand. */
    private final ObservableList<Card> handCards = FXCollections.observableArrayList();

    /** Observable list of the five register slots (nullable entries). */
    private final ObservableList<Card> registerSlots = FXCollections.observableArrayList();

    /** Lock flag: once true, no further changes to registers are allowed. */
    private final BooleanProperty selectionFinished = new SimpleBooleanProperty(false);

    /** Explicit toggle to allow/block selection independent of {@link #selectionFinished}. */
    private final BooleanProperty canSelect = new SimpleBooleanProperty(true);

    /**
     * Creates a new model with five empty register slots.
     */
    public CardViewModel() {
        // 5 leere Register-Slots vorbereiten
        for (int i = 0; i < 5; i++) {
            registerSlots.add(null);
        }
    }

    /**
     * @return observable list of hand cards
     */
    public ObservableList<Card> getHandCards() {
        return handCards;
    }

    /**
     * @return observable list of register slots (size 5, nullable entries)
     */
    public ObservableList<Card> getRegisterSlots() {
        return registerSlots;
    }

    /**
     * Tries to place the given card into the next free register slot.
     *
     * @param card card to place
     * @return index of the occupied slot, or {@code -1} if none was free or selection is blocked
     */
    public int assignCardToNextFreeSlot(Card card) {
        if (!canSelect.get() || selectionFinished.get()) return -1;  // blockieren
        for (int i = 0; i < registerSlots.size(); i++) {
            if (registerSlots.get(i) == null) {
                registerSlots.set(i, card);
                handCards.remove(card);
                return i;
            }
        }
        return -1;
    }

    /**
     * Removes the card from the specified register slot and returns it to the hand.
     *
     * @param index slot index (0–4)
     */
    public void removeCardFromRegisterSlot(int index) {
        if (!canSelect.get() || selectionFinished.get()) return;  // blockieren
        Card removed = registerSlots.get(index);
        if (removed != null) {
            handCards.add(removed);
            registerSlots.set(index, null);
        }
    }

    /**
     * @return {@code true} if all five registers are filled
     */
    public boolean isRegisterFull() {
        return registerSlots.stream().allMatch(c -> c != null);
    }

    /**
     * Clears all remaining hand cards after programming is finished.
     */
    public void discardRemainingCards() {
        handCards.clear();
    }

    /**
     * @return names of all cards currently assigned to registers (in slot order)
     */
    public List<String> getSelectedCards() {
        return registerSlots.stream()
                .filter(c -> c != null)
                .map(Card::getName)
                .toList();
    }

    /**
     * Adds a card to the hand list.
     *
     * @param card card to add
     */
    public void addCardToHand(Card card) {
        handCards.add(card);
    }


    // ---------------- Selection flags ----------------


    /**
     * @return {@code true} if selection has been marked as finished
     */
    public boolean isSelectionFinished() {
        return selectionFinished.get();
    }

    /**
     * Sets the finished flag. When set to {@code true}, selection is automatically disabled.
     *
     * @param finished new finished state
     */
    public void setSelectionFinished(boolean finished) {
        selectionFinished.set(finished);
        // wenn finished true -> automatisch keine Auswahl mehr zulassen
        if (finished) {
            canSelect.set(false);
        }
    }

    /**
     * @return property for binding the finished flag
     */
    public BooleanProperty selectionFinishedProperty() {
        return selectionFinished;
    }

    /**
     * Resets the finished flag and re-enables selection.
     */
    public void resetSelectionFinished() {
        selectionFinished.set(false);
        canSelect.set(true);
    }

    /**
     * @return {@code true} if selection is currently allowed
     */
    public boolean isCanSelect() {
        return canSelect.get();
    }

    /**
     * Explicitly enables/disables selection (even if not finished).
     *
     * @param selectable {@code true} to allow selection
     */
    public void setCanSelect(boolean selectable) {
        canSelect.set(selectable);
    }

    /**
     * @return property for binding the selection toggle
     */
    public BooleanProperty canSelectProperty() {
        return canSelect;
    }

    /**
     * Clears a specific register slot (does not move the card back to hand).
     *
     * @param index slot index (0–4)
     */
    public void clearRegisterSlot(int index) {
        if (index >= 0 && index < registerSlots.size()) {
            registerSlots.set(index, null);
        }
    }
}

