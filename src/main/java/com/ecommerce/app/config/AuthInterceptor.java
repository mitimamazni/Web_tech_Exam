package com.ecommerce.app.config;

import com.ecommerce.app.annotation.RequiresAuth;
import com.ecommerce.app.util.AuthHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequiresAuth requiresAuth = handlerMethod.getMethod().getAnnotation(RequiresAuth.class);
        
        // If no annotation is present, allow access
        if (requiresAuth == null) {
            // Check if class has annotation
            requiresAuth = handlerMethod.getBeanType().getAnnotation(RequiresAuth.class);
            if (requiresAuth == null) {
                return true;
            }
        }
        
        HttpSession session = request.getSession();
        
        // Check if authenticated
        if (!AuthHelper.isAuthenticated(session)) {
            response.sendRedirect("/login");
            return false;
        }
        
        // Check if admin required
        if (requiresAuth.adminOnly() && !AuthHelper.isAdmin(session)) {
            response.sendRedirect("/");  // Redirect to home if admin access required but user is not admin
            return false;
        }
        
        return true;
    }
}
