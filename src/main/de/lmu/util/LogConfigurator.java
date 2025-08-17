package de.lmu.util;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility to set the global logging level for the root {@link Logger} and all of its
 * current {@link Handler}s in one call.
 * <p>
 * This is a convenience for small apps/tests where a full logging config file is overkill.
 * Note that handlers added <em>after</em> this call will keep their own default level unless
 * you configure them separately.
 */
public class LogConfigurator {

    /**
     * Sets the root logger's level and updates every already-attached handler to the same level.
     *
     * @param level the minimum {@link Level} to log globally (e.g. {@link Level#FINE})
     */
    public static void configureRootLogger(Level level){
        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(level);
        for(Handler handler : rootLogger.getHandlers()){
        handler.setLevel(level);
        }
    }
}