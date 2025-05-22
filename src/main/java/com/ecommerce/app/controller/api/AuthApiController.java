package com.ecommerce.app.controller.api;

import com.ecommerce.app.util.AuthHelper;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for handling authentication-related API endpoints
 */
@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    /**
     * Check if the user is currently authenticated
     * 
     * @param session The current HTTP session
     * @return HTTP 200 OK if authenticated with user info, 401 Unauthorized if not
     */
    @GetMapping("/check-session")
    public ResponseEntity<?> checkSession(HttpSession session) {
        if (AuthHelper.isAuthenticated(session)) {
            Map<String, Object> response = new HashMap<>();
            response.put("authenticated", true);
            response.put("username", session.getAttribute("username"));
            response.put("userId", session.getAttribute("userId"));
            response.put("isAdmin", AuthHelper.isAdmin(session));
            
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body(Map.of("authenticated", false));
        }
    }
}
