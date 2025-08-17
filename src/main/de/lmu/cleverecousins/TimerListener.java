package de.lmu.cleverecousins;

/**
 * Listener interface for receiving a callback when a {@link GameTimer} expires.
 * <p>
 * Implementations register via {@link GameTimer#addListener(TimerListener)} and
 * are invoked once when the countdown finishes.
 */
@FunctionalInterface
public interface TimerListener {

    /**
     * Called exactly once when the associated timer has expired.
     */
    void onTimerExpired();
}
