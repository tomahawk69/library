package org.library.common.utils;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

public class LoggingUtils {
    private final static Logger LOGGER = LogManager.getLogger(LoggingUtils.class);

    public static void setDebugEnabled(boolean enabled) {
        if (enabled) {
            setLoggingLevel(Level.DEBUG);
        } else {
            setLoggingLevel(Level.INFO);
        }
    }

    public static void setLoggingLevel(Level level) {
        LOGGER.info("Change logging level to " + level);
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        loggerConfig.setLevel(level);
        ctx.updateLoggers();
    }

    public static boolean getDebugEnabled() {
        LOGGER.debug("Get logging level");
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        return loggerConfig.getLevel().equals(Level.DEBUG);
    }
}
