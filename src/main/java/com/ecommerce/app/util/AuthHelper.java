package com.ecommerce.app.util;

import com.ecommerce.app.model.ERole;
import com.ecommerce.app.model.User;
import jakarta.servlet.http.HttpSession;

/**
 * Helper class for authentication and session management
 */
public class AuthHelper {

    public static void setUserInSession(HttpSession session, User user) {
        if (user == null) {
            clearUserFromSession(session);
            return;
        }
        
        session.setAttribute("username", user.getUsername());
        session.setAttribute("userId", user.getId());
        
        // Check if user is admin
        boolean isAdmin = user.getRoles().stream()
            .anyMatch(role -> role.getName() == ERole.ROLE_ADMIN);
        session.setAttribute("isAdmin", isAdmin);
    }

    public static void clearUserFromSession(HttpSession session) {
        session.removeAttribute("username");
        session.removeAttribute("userId");
        session.removeAttribute("isAdmin");
    }

    public static boolean isAuthenticated(HttpSession session) {
        return session.getAttribute("username") != null;
    }
    
    public static boolean isAdmin(HttpSession session) {
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        return isAdmin != null && isAdmin;
    }
    
    public static Long getCurrentUserId(HttpSession session) {
        return (Long) session.getAttribute("userId");
    }
}
