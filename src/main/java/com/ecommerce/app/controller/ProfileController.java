package com.ecommerce.app.controller;

import com.ecommerce.app.dto.AddressDTO;
import com.ecommerce.app.dto.UserDTO;
import com.ecommerce.app.model.Address;
import com.ecommerce.app.model.Order;
import com.ecommerce.app.model.User;
import com.ecommerce.app.service.AddressService;
import com.ecommerce.app.service.OrderService;
import com.ecommerce.app.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private AddressService addressService;

    @GetMapping
    public String showProfile(HttpSession session, Model model) {
        // Get username from session
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/login";
        }
        
        // Find the user
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Convert to DTO to avoid lazy loading issues
        UserDTO userDTO = UserDTO.fromEntity(user);
        
        // Get recent orders (limit to 5)
        List<Order> recentOrders = orderService.getRecentOrdersByUser(user, 5);
        
        // Get user addresses
        List<Address> addresses = addressService.getAddressByUser(user);
        List<AddressDTO> addressDTOs = AddressDTO.fromEntities(addresses);
        
        // Get the first letter of user's name for profile picture
        String firstLetter = user.getFirstName().substring(0, 1).toUpperCase();
        
        model.addAttribute("user", userDTO);
        model.addAttribute("recentOrders", recentOrders);
        model.addAttribute("addresses", addressDTOs);
        model.addAttribute("profileLetter", firstLetter);
        
        return "user/profile";
    }
}
