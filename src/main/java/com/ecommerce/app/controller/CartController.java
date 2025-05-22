package com.ecommerce.app.controller;

import com.ecommerce.app.model.Cart;
import com.ecommerce.app.model.CartItem;
import com.ecommerce.app.model.Product;
import com.ecommerce.app.model.User;
import com.ecommerce.app.service.CartService;
import com.ecommerce.app.service.ProductService;
import com.ecommerce.app.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        // Get username from session
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/login";
        }
        
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Get active cart for user
        Cart activeCart = cartService.getActiveCart(user);
        
        model.addAttribute("cartItems", activeCart.getCartItems());
        model.addAttribute("total", cartService.getCartTotal(activeCart));
        
        return "cart/view";
    }
    
    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<?> addToCart(@RequestBody Map<String, Object> payload,
                          HttpSession session) {
        
        Long productId = Long.valueOf(payload.get("productId").toString());
        int quantity = payload.get("quantity") != null ? 
                      Integer.parseInt(payload.get("quantity").toString()) : 1;
        
        String username = (String) session.getAttribute("username");
        Map<String, Object> response = new HashMap<>();
        
        if (username == null) {
            response.put("redirect", "/login");
            return ResponseEntity.ok(response);
        }
        
        try {
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
                    
            Product product = productService.getProductById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            
            // Get or create active cart for user
            Cart activeCart = cartService.getActiveCart(user);
            
            cartService.addProductToCart(activeCart, product, quantity);
            
            // Return cart count
            int cartCount = cartService.getCartItemCount(activeCart);
            response.put("success", true);
            response.put("message", product.getName() + " added to cart successfully");
            response.put("cartCount", cartCount);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error adding product to cart: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    
    /**
     * Remove item from cart via AJAX
     */
    @PostMapping("/remove-ajax")
    @ResponseBody
    public Map<String, Object> removeFromCartAjax(@RequestBody Map<String, Object> payload,
                             HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        Long productId = Long.valueOf(payload.get("productId").toString());
        
        String username = (String) session.getAttribute("username");
        if (username == null) {
            response.put("success", false);
            response.put("message", "User not logged in");
            response.put("redirect", "/login");
            return response;
        }
        
        try {
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
                    
            Product product = productService.getProductById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            
            Cart activeCart = cartService.getActiveCart(user);
            cartService.removeProductFromCart(activeCart, productId);
            
            int cartCount = cartService.getCartItemCount(activeCart);
            response.put("success", true);
            response.put("message", "Item removed from cart successfully");
            response.put("cartCount", cartCount);
            
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error removing item: " + e.getMessage());
            return response;
        }
    }
    
    /**
     * Update cart item quantity - Now returns JSON response
     */
    @PostMapping("/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateCartItem(@RequestParam Long productId,
                               @RequestParam int quantity,
                               HttpSession session) {
        Map<String, Object> response = new HashMap<>();
                               
        String username = (String) session.getAttribute("username");
        if (username == null) {
            response.put("success", false);
            response.put("message", "User not logged in");
            response.put("redirect", "/login");
            return ResponseEntity.ok(response);
        }
        
        try {
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
                    
            Product product = productService.getProductById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            
            // Get active cart
            Cart activeCart = cartService.getActiveCart(user);
                    
            if (quantity <= 0) {
                cartService.removeProductFromCart(activeCart, productId);
                response.put("success", true);
                response.put("message", product.getName() + " removed from cart");
            } else {
                cartService.updateCartItemQuantity(activeCart, productId, quantity);
                response.put("success", true);
                response.put("message", "Cart updated successfully");
            }
            
            // Include cart count in response
            int cartCount = cartService.getCartItemCount(activeCart);
            response.put("cartCount", cartCount);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating cart: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/remove")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> removeFromCart(@RequestParam Long productId,
                               HttpSession session) {
        Map<String, Object> response = new HashMap<>();
                               
        String username = (String) session.getAttribute("username");
        if (username == null) {
            response.put("success", false);
            response.put("message", "User not logged in");
            response.put("redirect", "/login");
            return ResponseEntity.ok(response);
        }
        
        try {
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
                    
            Product product = productService.getProductById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
                    
            // Get active cart
            Cart activeCart = cartService.getActiveCart(user);
                    
            cartService.removeProductFromCart(activeCart, productId);
            
            // Include cart count in response
            int cartCount = cartService.getCartItemCount(activeCart);
            response.put("success", true);
            response.put("message", product.getName() + " removed from cart");
            response.put("cartCount", cartCount);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error removing item: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/clear")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> clearCart(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        String username = (String) session.getAttribute("username");
        if (username == null) {
            response.put("success", false);
            response.put("message", "User not logged in");
            return ResponseEntity.ok(response);
        }
        
        try {
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Get active cart
            Cart activeCart = cartService.getActiveCart(user);
            
            // Clear the cart
            cartService.clearCart(activeCart);
            
            response.put("success", true);
            response.put("message", "Cart cleared successfully");
            response.put("cartCount", 0);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error clearing cart: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
    
    @GetMapping("/count")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCartCount(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        String username = (String) session.getAttribute("username");
        if (username == null) {
            response.put("cartCount", 0);
        } else {
            try {
                User user = userService.findByUsername(username).orElse(null);
                if (user != null) {
                    Cart activeCart = cartService.getActiveCart(user);
                    response.put("cartCount", cartService.getCartItemCount(activeCart));
                } else {
                    response.put("cartCount", 0);
                }
            } catch (Exception e) {
                response.put("cartCount", 0);
            }
        }
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/mini")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getMiniCart(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        String username = (String) session.getAttribute("username");
        
        if (username == null) {
            response.put("items", new ArrayList<>());
            response.put("subtotal", "$0.00");
            return ResponseEntity.ok(response);
        }
        
        try {
            User user = userService.findByUsername(username).orElse(null);
            if (user == null) {
                response.put("items", new ArrayList<>());
                response.put("subtotal", "$0.00");
                return ResponseEntity.ok(response);
            }
            
            Cart activeCart = cartService.getActiveCart(user);
            List<CartItem> cartItems = activeCart.getCartItems();
            List<Map<String, Object>> items = new ArrayList<>();
            
            for (CartItem cartItem : cartItems) {
                Map<String, Object> itemMap = new HashMap<>();
                Product product = cartItem.getProduct();
                
                itemMap.put("id", product.getId());
                
                // Handle product name consistently with the main cart view
                String productName = product.getName();
                if (productName != null && !productName.trim().isEmpty()) {
                    itemMap.put("name", productName);
                } else {
                    // Don't set a default name, the frontend will fetch it from the API if needed
                    itemMap.put("name", null);
                }
                
                // Format price with proper decimal places
                if (cartItem.getPrice() != null) {
                    itemMap.put("price", String.format("%.2f", cartItem.getPrice()));
                } else {
                    itemMap.put("price", "0.00");
                }
                
                itemMap.put("quantity", cartItem.getQuantity());
                
                // Get image URL (primary image or first image or fallback to a default URL)
                String imageUrl = null;
                if (product.getImages() != null && !product.getImages().isEmpty()) {
                    imageUrl = product.getImages().stream()
                        .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                        .findFirst()
                        .orElse(product.getImages().get(0))
                        .getImageUrl();
                }
                
                itemMap.put("image", imageUrl != null ? imageUrl : 
                         "https://images.unsplash.com/photo-1611854779393-1b2da9d400fe?w=100&auto=format&fit=crop");
                
                items.add(itemMap);
            }
            
            response.put("items", items);
            response.put("subtotal", "$" + cartService.getCartTotal(activeCart).toString());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("error", "Error loading cart: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Get all cart items and totals for the current user (AJAX/dynamic use)
     */
    @GetMapping("/items")
    @ResponseBody
    public Map<String, Object> getCartItems(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        String username = (String) session.getAttribute("username");
        if (username == null) {
            response.put("success", false);
            response.put("message", "User not logged in");
            response.put("redirect", "/login");
            return response;
        }
        try {
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Cart activeCart = cartService.getActiveCart(user);
            List<Map<String, Object>> items = new ArrayList<>();
            for (CartItem item : activeCart.getCartItems()) {
                Map<String, Object> itemData = new HashMap<>();
                Product product = item.getProduct();
                itemData.put("productId", product.getId());
                // Handle product name - pass name if available, client will fetch from API if needed
                String productName = product.getName();
                if (productName != null && !productName.trim().isEmpty()) {
                    itemData.put("name", productName);
                } else {
                    // Don't set a default name, the frontend will fetch it from the API
                    itemData.put("name", null);
                }
                itemData.put("price", product.getPrice() != null ? product.getPrice() : 0.0);
                itemData.put("salePrice", product.getSalePrice());
                itemData.put("quantity", item.getQuantity());
                String imageUrl = "/images/product-placeholder.jpg";
                if (product.getImages() != null && !product.getImages().isEmpty()) {
                    imageUrl = product.getImages().stream()
                        .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                        .findFirst()
                        .orElse(product.getImages().get(0))
                        .getImageUrl();
                }
                itemData.put("imageUrl", imageUrl);
                itemData.put("stockQuantity", product.getStockQuantity());
                items.add(itemData);
            }
            Map<String, Object> data = new HashMap<>();
            data.put("items", items);
            // Ensure subtotal is a valid number
            BigDecimal subtotal = cartService.getCartTotal(activeCart);
            // Convert BigDecimal to String with proper format to ensure it's parsed correctly
            data.put("subtotal", subtotal != null ? subtotal.toString() : "0.00");
            // Calculate tax and total
            response.put("success", true);
            response.put("data", data);
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error loading cart: " + e.getMessage());
            return response;
        }
    }
}
