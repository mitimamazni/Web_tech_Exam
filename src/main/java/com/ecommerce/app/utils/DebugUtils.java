package com.ecommerce.app.utils;

import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Utility class for debugging in the application.
 * Provides methods for logging debug information with different levels.
 */
@Component
public class DebugUtils {
    
    @Value("${app.debug.enabled:true}")
    private boolean debugEnabled;
    
    /**
     * Log debug information if debug is enabled.
     * 
     * @param logger The logger to use
     * @param message The message to log
     * @param args Arguments for the message
     */
    public void debug(Logger logger, String message, Object... args) {
        if (debugEnabled && logger.isDebugEnabled()) {
            logger.debug(message, args);
        }
    }
    
    /**
     * Log info information if debug is enabled.
     * 
     * @param logger The logger to use
     * @param message The message to log
     * @param args Arguments for the message
     */
    public void info(Logger logger, String message, Object... args) {
        if (debugEnabled && logger.isInfoEnabled()) {
            logger.info(message, args);
        }
    }
    
    /**
     * Log method entry with parameters if debug is enabled.
     * 
     * @param logger The logger to use
     * @param methodName The method name
     * @param params Method parameters
     */
    public void logMethodEntry(Logger logger, String methodName, Object... params) {
        if (debugEnabled && logger.isDebugEnabled()) {
            logger.debug("ENTER: {} with parameters: {}", 
                         methodName, 
                         params != null ? Arrays.toString(params) : "none");
        }
    }
    
    /**
     * Log method exit with result if debug is enabled.
     * 
     * @param logger The logger to use
     * @param methodName The method name
     * @param result The method result
     */
    public void logMethodExit(Logger logger, String methodName, Object result) {
        if (debugEnabled && logger.isDebugEnabled()) {
            logger.debug("EXIT: {} with result: {}", 
                         methodName, 
                         result != null ? result.toString() : "null");
        }
    }
    
    /**
     * Check if debug is enabled.
     * 
     * @return true if debug is enabled, false otherwise
     */
    public boolean isDebugEnabled() {
        return debugEnabled;
    }
}
