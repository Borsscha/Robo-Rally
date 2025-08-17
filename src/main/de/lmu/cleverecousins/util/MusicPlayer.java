package de.lmu.cleverecousins.util;

import de.lmu.util.LogConfigurator;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple helper to play looping background music via JavaFX's {@link MediaPlayer}.
 * <p>
 * All methods are static; the class keeps a single player instance internally.
 * Make sure the JavaFX runtime is initialized before calling these methods
 * (e.g. from within a JavaFX application thread).
 */
public class MusicPlayer {

    private static final Logger log = Logger.getLogger(MusicPlayer.class.getName());

    static {
        LogConfigurator.configureRootLogger(Level.FINE);
    }

    /** The singleton media player instance (null if nothing loaded). */
    private static MediaPlayer mediaPlayer;

    /**
     * Loads and starts looping the background track {@code /audio/background.mp3} from the classpath.
     * If music is already playing, it is stopped and replaced.
     */
    public static void playBackgroundMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }

        try {
            URL resource = MusicPlayer.class.getResource("/audio/background.mp3");
            Media media = new Media(resource.toExternalForm());
            mediaPlayer = new MediaPlayer(media);

            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE); // loop
            mediaPlayer.setVolume(0.2); // start quietly
            mediaPlayer.play();

            log.fine("[DEBUG] Hintergrundmusik gestartet. ");
        } catch (Exception e) {
            log.log(Level.SEVERE, "[FEHLER] Musik konnte nicht abgespielt werden", e);
        }
    }

    /**
     * Stops playback if a track is currently playing.
     */
    public static void stopMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            log.fine("[DEBUG] Musik gestoppt. ");
        }
    }

    /**
     * Sets the playback volume.
     *
     * @param volume value clamped to {@code 0.0 .. 1.0}
     */
    public static void setVolume(double volume) {
        if (mediaPlayer != null) {
            // volume zwischen 0.0 und 1.0
            if (volume < 0.0) volume = 0.0;
            if (volume > 1.0) volume = 1.0;
            mediaPlayer.setVolume(volume);
            log.fine("[DEBUG] Musiklautstärke gesetzt auf: " + volume);
        }
    }

    /**
     * @return current volume ({@code 0.0 .. 1.0}) or {@code 0.0} if no player exists
     */
    public static double getVolume() {
        if (mediaPlayer != null) {
            return mediaPlayer.getVolume();
        }
        return 0.0;
    }
}

