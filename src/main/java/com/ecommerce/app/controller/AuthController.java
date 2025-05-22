package com.ecommerce.app.controller;

import com.ecommerce.app.model.ERole;
import com.ecommerce.app.model.User;
import com.ecommerce.app.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;
    
    @GetMapping("/login")
    public String showLoginForm() {
        return "auth/login";
    }
    
    @PostMapping("/login")
    public String login(@RequestParam String username, 
                        @RequestParam String password,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        
        Optional<User> userOpt = userService.findByUsername(username);
        
        if (userOpt.isPresent() && userOpt.get().getPassword().equals(password)) {
            User user = userOpt.get();
            
            // Store in session
            session.setAttribute("username", username);
            session.setAttribute("userId", user.getId());
            
            // Check if user is admin
            boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getName() == ERole.ROLE_ADMIN);
            session.setAttribute("isAdmin", isAdmin);
            
            // Set username in browser and redirect based on role
            if (isAdmin) {
                // For admin users, first set username then redirect directly to admin page
                session.setAttribute("adminRedirect", true);
                return "redirect:/auth/set-username?username=" + username + "&admin=true";
            } else {
                // For regular users, follow the normal flow
                return "redirect:/auth/set-username?username=" + username;
            }
        }
        
        redirectAttributes.addFlashAttribute("error", "Invalid username or password");
        return "redirect:/login";
    }
    
    @GetMapping("/auth/set-username")
    public String setUsernamePage(@RequestParam String username, 
                                  @RequestParam(required = false) Boolean admin,
                                  Model model) {
        model.addAttribute("username", username);
        // If admin=true parameter is passed, add redirect attribute to the model
        if (Boolean.TRUE.equals(admin)) {
            model.addAttribute("adminRedirect", true);
        }
        return "auth/set-username";
    }
    
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout";
    }
    
    @PostMapping("/logout")
    public String logoutPost(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout";
    }
    
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }
    
    @PostMapping("/register")
    public String register(@ModelAttribute User user, 
                         RedirectAttributes redirectAttributes) {
        
        if (userService.existsByUsername(user.getUsername())) {
            redirectAttributes.addFlashAttribute("error", "Username is already taken");
            return "redirect:/register";
        }
        
        if (userService.existsByEmail(user.getEmail())) {
            redirectAttributes.addFlashAttribute("error", "Email is already in use");
            return "redirect:/register";
        }
        
        // Register as regular user (not admin)
        userService.registerUser(user, false);
        
        redirectAttributes.addFlashAttribute("success", "Registration successful! Please log in.");
        return "redirect:/login";
    }
    
    @ModelAttribute
    public void addSessionUsernameToModel(HttpSession session, Model model) {
        Object username = session.getAttribute("username");
        if (username != null) {
            model.addAttribute("sessionUsername", username.toString());
        }
    }
    
    @GetMapping("/api/session-username")
    @ResponseBody
    public Map<String, Object> getSessionUsername(HttpSession session) {
        Object username = session.getAttribute("username");
        Map<String, Object> response = new HashMap<>();
        response.put("username", username != null ? username.toString() : null);
        return response;
    }
}
