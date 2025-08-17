package de.lmu.cleverecousins;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * One-shot countdown timer used for game phases.
 * <p>
 * The timer runs for a fixed duration (in seconds) and notifies all registered
 * {@link TimerListener}s when it expires. It can be started once and stopped manually.
 */
public class GameTimer {

    /** Duration of this timer in milliseconds. */
    private final int durationMillis;

    /** {@code true} while the timer is counting down. */
    private boolean running;

    /** Registered listeners to be notified on expiry. */
    private final List<TimerListener> listeners = new ArrayList<>();

    /** Internal {@link Timer} instance scheduling the expiration task. */
    private Timer internalTimer;


    /**
     * Creates a new timer with the given duration.
     *
     * @param seconds duration of the countdown in seconds
     */
    public GameTimer(int seconds){
        this.durationMillis = seconds * 1000;
        this.running = false;
    }

    /**
     * Starts the timer if it is not already running.
     * When the time elapses, all listeners are notified via {@link TimerListener#onTimerExpired()}.
     */
    public void start(){
        if(running) return;
        running = true;

        internalTimer = new Timer();
        internalTimer.schedule(new TimerTask(){
            @Override
            public void run(){
                running = false;
                notifyListeners();
            }
        }, durationMillis);
    }

    /**
     * Stops the timer manually. No listener notification is sent.
     * Safe to call if the timer is not running.
     */
    public void stop(){
        if(internalTimer != null){
            internalTimer.cancel();
            internalTimer = null;
        }
        running = false;
    }

    /**
     * Registers a listener to be informed when the timer expires.
     *
     * @param listener listener to add
     */
    public void addListener(TimerListener listener){
        listeners.add(listener);
    }

    /**
     * Notifies all registered listeners that the timer has expired.
     */
    private void notifyListeners(){
        for(TimerListener listener : listeners){
            listener.onTimerExpired();
        }
    }

    /**
     * Indicates whether the timer is currently running.
     *
     * @return {@code true} if counting down, otherwise {@code false}
     */
    public boolean isRunning(){
        return running;
    }
}
