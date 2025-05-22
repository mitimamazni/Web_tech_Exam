package com.ecommerce.app.controller;

import com.ecommerce.app.model.Address;
import com.ecommerce.app.model.AddressType;
import com.ecommerce.app.model.CartItem;
import com.ecommerce.app.model.Order;
import com.ecommerce.app.model.OrderStatus;
import com.ecommerce.app.model.User;
import com.ecommerce.app.service.AddressService;
import com.ecommerce.app.service.CartService;
import com.ecommerce.app.service.OrderService;
import com.ecommerce.app.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;

import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CartService cartService;

    @Autowired
    private UserService userService;
    
    @Autowired
    private AddressService addressService;

    @GetMapping
    public String viewOrders(HttpSession session, Model model) {
        // Get username from session
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/login";
        }
        
        // Find the user and use it to get their orders
        User currentUser = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Order> orders = orderService.getOrdersByUser(currentUser);
        model.addAttribute("orders", orders);
        
        return "order/list";
    }

    @GetMapping("/{id}")
    public String viewOrderDetails(@PathVariable Long id,
                                 HttpSession session,
                                 Model model) {
        
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/login";
        }
        
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Order order = orderService.getOrderById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        // Check if the user is the order owner or an admin
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        if (!order.getUser().getId().equals(user.getId()) && !(isAdmin != null && isAdmin)) {
            return "redirect:/orders";
        }
        
        model.addAttribute("order", order);
        return "order/details";
    }

    @GetMapping("/checkout")
    public String checkout(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        System.out.println("==== Checkout Process Started ====");
        String username = (String) session.getAttribute("username");
        System.out.println("Username from session: " + username);
        
        if (username == null) {
            System.out.println("No username in session, redirecting to login");
            redirectAttributes.addFlashAttribute("error", "Please login to continue with checkout");
            return "redirect:/login?redirect=/orders/checkout";
        }
        
        try {
            System.out.println("Looking up user: " + username);
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            System.out.println("User found: " + user.getId());
            
            System.out.println("Getting cart items for user");
            List<CartItem> cartItems = cartService.getCartItems(user);
            System.out.println("Cart items count: " + (cartItems != null ? cartItems.size() : "null"));
            
            if (cartItems == null || cartItems.isEmpty()) {
                System.out.println("Cart is empty, redirecting back to cart");
                redirectAttributes.addFlashAttribute("info", "Your cart is empty. Please add items before checkout");
                return "redirect:/cart";
            }
            
            // Get user addresses for selection during checkout
            System.out.println("Getting user addresses");
            List<Address> addresses = addressService.getAddressByUser(user);
            System.out.println("Found " + addresses.size() + " addresses");
            
            System.out.println("Calculating cart total");
            BigDecimal total = cartService.getCartTotal(user);
            System.out.println("Cart total: " + total);
            
            model.addAttribute("cartItems", cartItems);
            model.addAttribute("total", total);
            model.addAttribute("user", user);
            model.addAttribute("addresses", addresses);
            
            System.out.println("Rendering checkout page");
            return "order/checkout";
        } catch (Exception e) {
            // Log the error
            System.err.println("Error during checkout process: " + e.getMessage());
            System.err.println("Error type: " + e.getClass().getName());
            e.printStackTrace();
            
            // Add user-friendly error message
            redirectAttributes.addFlashAttribute("error", "There was an error processing your checkout. Please try again later: " + e.getMessage());
            return "redirect:/cart";
        }
    }

    @PostMapping("/place")
    public String placeOrder(HttpSession session,
                           @RequestParam(value = "addressId", required = false) String addressIdStr,
                           @RequestParam(value = "streetAddress", required = false) String streetAddress,
                           @RequestParam(value = "city", required = false) String city,
                           @RequestParam(value = "state", required = false) String state,
                           @RequestParam(value = "postalCode", required = false) String postalCode,
                           @RequestParam(value = "country", required = false) String country,
                           @RequestParam(value = "saveAddress", required = false) Boolean saveAddress,
                           RedirectAttributes redirectAttributes) {
        
        String username = (String) session.getAttribute("username");
        if (username == null) {
            redirectAttributes.addFlashAttribute("error", "Please log in to place an order");
            return "redirect:/login";
        }
        
        try {
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            List<CartItem> cartItems = cartService.getCartItems(user);
            
            if (cartItems.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Your cart is empty");
                return "redirect:/cart";
            }
            
            // Validate address selection
            if (addressIdStr == null || addressIdStr.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Please select a shipping address");
                return "redirect:/orders/checkout";
            }
            
            // Process shipping address
            String shippingAddress;
            
            if ("new".equals(addressIdStr)) {
                // Create a formatted address string from form fields
                if (streetAddress == null || city == null || state == null || 
                    postalCode == null || country == null || 
                    streetAddress.trim().isEmpty() || city.trim().isEmpty() || 
                    state.trim().isEmpty() || postalCode.trim().isEmpty() || 
                    country.trim().isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "Please provide all address fields");
                    return "redirect:/orders/checkout";
                }
                
                shippingAddress = String.format("%s, %s, %s %s, %s", 
                    streetAddress.trim(), city.trim(), state.trim(), postalCode.trim(), country.trim());
                    
                // Save the new address if requested
                if (Boolean.TRUE.equals(saveAddress)) {
                    try {
                        Address address = new Address();
                        address.setUser(user);
                        address.setAddressType(AddressType.SHIPPING);
                        address.setStreetAddress(streetAddress.trim());
                        address.setCity(city.trim());
                        address.setState(state.trim());
                        address.setPostalCode(postalCode.trim());
                        address.setCountry(country.trim());
                        
                        // If this is the first address, set it as default
                        if (addressService.getAddressByUser(user).isEmpty()) {
                            address.setIsDefault(true);
                        }
                        
                        addressService.saveAddress(address);
                    } catch (Exception e) {
                        // Log the error but continue with order placement
                        System.err.println("Error saving address: " + e.getMessage());
                    }
                }
            } else {
                // Use existing address
                try {
                    Long addressId = Long.parseLong(addressIdStr);
                    Address address = addressService.getAddressById(addressId)
                            .orElseThrow(() -> new RuntimeException("Address not found"));
                    
                    // Verify the address belongs to the user
                    if (!address.getUser().getId().equals(user.getId())) {
                        redirectAttributes.addFlashAttribute("error", "Invalid address selected");
                        return "redirect:/orders/checkout";
                    }
                    
                    shippingAddress = String.format("%s, %s, %s %s, %s", 
                        address.getStreetAddress(), address.getCity(), 
                        address.getState(), address.getPostalCode(), address.getCountry());
                } catch (NumberFormatException e) {
                    redirectAttributes.addFlashAttribute("error", "Invalid address ID format");
                    return "redirect:/orders/checkout";
                } catch (Exception e) {
                    redirectAttributes.addFlashAttribute("error", "Please select a valid shipping address");
                    return "redirect:/orders/checkout";
                }
            }
            
            try {
                // Create the order (this will also clear the cart in the service)
                Order order = orderService.createOrder(user, shippingAddress);
                
                // Remove cart count from session
                session.removeAttribute("cartItemCount");
                
                // Redirect to confirmation page
                return "redirect:/orders/" + order.getId() + "/confirmation";
            } catch (Exception e) {
                // Log the detailed error
                System.err.println("Error creating order: " + e.getMessage());
                e.printStackTrace();
                
                // Provide a user-friendly error message
                String errorMessage = e.getMessage();
                if (errorMessage != null && errorMessage.contains("stock")) {
                    redirectAttributes.addFlashAttribute("error", 
                        "Some items in your cart are no longer in stock in the requested quantity. " +
                        "Please update your cart and try again.");
                } else {
                    redirectAttributes.addFlashAttribute("error", 
                        "There was a problem processing your order. Please try again later.");
                }
                return "redirect:/orders/checkout";
            }
        } catch (Exception e) {
            // Log the detailed error
            System.err.println("Unexpected error in order placement: " + e.getMessage());
            e.printStackTrace();
            
            redirectAttributes.addFlashAttribute("error", "An unexpected error occurred. Please try again later.");
            return "redirect:/orders/checkout";
        }
    }

    @PostMapping("/{id}/cancel")
    public String cancelOrder(@PathVariable Long id, 
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/login";
        }
        
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Order order = orderService.getOrderById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        // Check if user is authorized to cancel
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        if (!order.getUser().getId().equals(user.getId()) && !(isAdmin != null && isAdmin)) {
            redirectAttributes.addFlashAttribute("error", "You are not authorized to cancel this order");
            return "redirect:/orders";
        }
        
        try {
            orderService.updateOrderStatus(id, OrderStatus.CANCELLED);
            // Use query parameter instead of flash attribute for JavaScript to pick up
            return "redirect:/orders/" + id + "?success=Order+cancelled+successfully";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error cancelling order: " + e.getMessage());
            return "redirect:/orders/" + id;
        }
    }

    @GetMapping("/{id}/confirmation")
    public String orderConfirmation(@PathVariable Long id,
                                 HttpSession session,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/login";
        }
        
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Order order = orderService.getOrderById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        // Verify that this order belongs to the current user
        if (!order.getUser().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("error", "You do not have permission to view this order");
            return "redirect:/orders";
        }
        
        model.addAttribute("order", order);
        return "order/confirmation";
    }
}
