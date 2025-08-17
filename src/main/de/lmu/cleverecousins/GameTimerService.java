package de.lmu.cleverecousins;

/**
 * Thin service wrapper around a single {@link GameTimer} instance.
 * <p>
 * Intended to encapsulate timer creation and provide access to the shared timer.
 */
public class GameTimerService {

    /** The underlying countdown timer. */
    private final GameTimer timer;

    /**
     * Creates the service and initializes its {@link GameTimer}.
     *
     * @param durationSeconds duration of the timer in seconds
     */
    public GameTimerService(int durationSeconds){
        this.timer = new GameTimer(durationSeconds);
    }

    /**
     * Returns the wrapped {@link GameTimer}.
     *
     * @return the timer instance managed by this service
     */
    public GameTimer getTimer(){
        return timer;
    }
}
