package com.ecommerce.app.util;

import com.ecommerce.app.model.ERole;
import com.ecommerce.app.model.User;
import jakarta.servlet.http.HttpSession;

/**
 * Helper utility class for managing authentication in the session
 */
public class SessionHelper {

    /**
     * Set user authentication information in the session
     * @param session The HTTP session
     * @param user The authenticated user
     */
    public static void setSessionUser(HttpSession session, User user) {
        if (user == null) {
            clearSessionUser(session);
            return;
        }
        
        session.setAttribute("username", user.getUsername());
        session.setAttribute("userId", user.getId());
        
        boolean isAdmin = user.getRoles().stream()
            .anyMatch(role -> role.getName() == ERole.ROLE_ADMIN);
        session.setAttribute("isAdmin", isAdmin);
    }
    
    /**
     * Clear user authentication information from the session
     * @param session The HTTP session
     */
    public static void clearSessionUser(HttpSession session) {
        session.removeAttribute("username");
        session.removeAttribute("userId");
        session.removeAttribute("isAdmin");
    }
    
    /**
     * Check if a user is authenticated in the current session
     * @param session The HTTP session
     * @return true if a user is authenticated
     */
    public static boolean isAuthenticated(HttpSession session) {
        return session.getAttribute("username") != null;
    }
    
    /**
     * Check if the authenticated user is an admin
     * @param session The HTTP session
     * @return true if an authenticated admin user
     */
    public static boolean isAdmin(HttpSession session) {
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        return isAdmin != null && isAdmin;
    }
    
    /**
     * Get the current authenticated user's ID
     * @param session The HTTP session
     * @return The user ID or null if not authenticated
     */
    public static Long getCurrentUserId(HttpSession session) {
        return (Long) session.getAttribute("userId");
    }
}
