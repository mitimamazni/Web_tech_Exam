package com.ecommerce.app.controller;

import com.ecommerce.app.utils.DebugUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.http.HttpSession;

/**
 * Controller for debugging purposes.
 * This controller provides endpoints for debugging and monitoring the application.
 */
@RestController
@RequestMapping("/api/debug")
public class DebugController {
    
    private static final Logger logger = LoggerFactory.getLogger(DebugController.class);
    
    @Autowired
    private DebugUtils debugUtils;
    
    /**
     * Check if debug mode is enabled
     * @return Status of debug mode
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getDebugStatus() {
        Map<String, Object> response = new HashMap<>();
        
        response.put("debugEnabled", debugUtils.isDebugEnabled());
        response.put("timestamp", System.currentTimeMillis());
        
        logger.info("Debug status checked: {}", debugUtils.isDebugEnabled());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Log a debug message from the frontend
     * @param payload Message payload
     * @return Confirmation of logging
     */
    @PostMapping("/log")
    public ResponseEntity<Map<String, Object>> logDebugMessage(@RequestBody Map<String, Object> payload) {
        Map<String, Object> response = new HashMap<>();
        
        if (!debugUtils.isDebugEnabled()) {
            response.put("status", "ignored");
            response.put("message", "Debug mode is disabled");
            return ResponseEntity.ok(response);
        }
        
        String level = payload.getOrDefault("level", "info").toString();
        String message = payload.getOrDefault("message", "").toString();
        Object data = payload.get("data");
        
        switch (level.toLowerCase()) {
            case "error":
                logger.error("CLIENT: {} - Data: {}", message, data);
                break;
            case "warn":
                logger.warn("CLIENT: {} - Data: {}", message, data);
                break;
            case "debug":
                logger.debug("CLIENT: {} - Data: {}", message, data);
                break;
            case "info":
            default:
                logger.info("CLIENT: {} - Data: {}", message, data);
                break;
        }
        
        response.put("status", "success");
        response.put("message", "Log message received");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Check the user session state
     * @param session The HTTP session
     * @return Session information
     */
    @GetMapping("/session")
    public ResponseEntity<Map<String, Object>> checkSession(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        if (!debugUtils.isDebugEnabled()) {
            response.put("status", "disabled");
            return ResponseEntity.ok(response);
        }
        
        response.put("sessionId", session.getId());
        response.put("creationTime", session.getCreationTime());
        response.put("lastAccessedTime", session.getLastAccessedTime());
        response.put("maxInactiveInterval", session.getMaxInactiveInterval());
        response.put("isNew", session.isNew());
        
        Map<String, Object> attributes = new HashMap<>();
        java.util.Enumeration<String> attributeNames = session.getAttributeNames();
        
        while (attributeNames.hasMoreElements()) {
            String name = attributeNames.nextElement();
            // Don't include sensitive session values
            if (!name.toLowerCase().contains("password") && 
                !name.toLowerCase().contains("token") &&
                !name.toLowerCase().contains("secret")) {
                attributes.put(name, session.getAttribute(name));
            } else {
                attributes.put(name, "***REDACTED***");
            }
        }
        
        response.put("attributes", attributes);
        
        logger.info("Session debug info accessed: {}", session.getId());
        
        return ResponseEntity.ok(response);
    }
}
