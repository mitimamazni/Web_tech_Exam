package com.ecommerce.app.controller;

import com.ecommerce.app.dto.WishlistItemDTO;
import com.ecommerce.app.model.Product;
import com.ecommerce.app.model.User;
import com.ecommerce.app.service.ProductService;
import com.ecommerce.app.service.UserService;
import com.ecommerce.app.service.WishlistService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/wishlist")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private UserService userService;
    
    /**
     * Display the wishlist view page
     * For logged in users, this will merge browser localStorage wishlist with their account wishlist
     */
    @GetMapping
    public String viewWishlist(HttpSession session, Model model) {
        // This page will now be accessible to both logged-in and guest users
        // The actual wishlist rendering will be handled client-side with JS
        return "wishlist/view";
    }
    
    /**
     * Get product details by ID for the wishlist
     * This endpoint is used by the client-side wishlist to get up-to-date product information
     */
    @GetMapping("/product/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getProductDetails(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Product product = productService.getProductById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            
            WishlistItemDTO productData = new WishlistItemDTO();
            productData.setProductId(product.getId());
            productData.setName(product.getName());
            productData.setPrice(product.getPrice());
            productData.setSalePrice(product.getSalePrice());
            
            // Get primary image or first image URL
            String imageUrl = "/images/product-placeholder.jpg"; // Default placeholder
            if (product.getImages() != null && !product.getImages().isEmpty()) {
                imageUrl = product.getImages().stream()
                    .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                    .findFirst()
                    .orElse(product.getImages().get(0))
                    .getImageUrl();
            }
            productData.setImageUrl(imageUrl);
            
            response.put("product", productData);
            response.put("success", true);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error getting product: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Support endpoint for integrating browser wishlist with server-side wishlist
     * For authenticated users who wish to save their wishlist to their account
     */
    @PostMapping("/sync")
    @ResponseBody
    public ResponseEntity<?> syncWishlist(@RequestBody List<Map<String, Object>> wishlistItems,
                             HttpSession session) {
        String username = (String) session.getAttribute("username");
        Map<String, Object> response = new HashMap<>();
        
        if (username == null) {
            response.put("success", false);
            response.put("message", "User not logged in");
            return ResponseEntity.ok(response);
        }
        
        try {
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Clear existing wishlist
            wishlistService.clearWishlist(user);
            
            // Add all items from browser localStorage
            for (Map<String, Object> item : wishlistItems) {
                Long productId = Long.valueOf(item.get("id").toString());
                Product product = productService.getProductById(productId)
                        .orElseThrow(() -> new RuntimeException("Product not found"));
                
                wishlistService.addProductToWishlist(user, product);
            }
            
            int wishlistCount = wishlistService.getWishlistItemCount(user);
            response.put("success", true);
            response.put("message", "Wishlist synchronized successfully");
            response.put("wishlistCount", wishlistCount);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error synchronizing wishlist: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Support endpoint for backwards compatibility with server-side wishlists
     * This is no longer the primary method for adding items to wishlist
     */
    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<?> addToWishlist(@RequestBody Map<String, Object> payload,
                               HttpSession session) {
        
        Long productId = Long.valueOf(payload.get("productId").toString());
        
        String username = (String) session.getAttribute("username");
        Map<String, Object> response = new HashMap<>();
        
        if (username == null) {
            // For browser-side wishlist, allow adding without login
            response.put("success", true);
            response.put("browserOnly", true);
            return ResponseEntity.ok(response);
        }
        
        try {
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
                    
            Product product = productService.getProductById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
                    
            wishlistService.addProductToWishlist(user, product);
            
            // Return wishlist count
            int wishlistCount = wishlistService.getWishlistItemCount(user);
            response.put("success", true);
            response.put("message", product.getName() + " added to wishlist successfully");
            response.put("wishlistCount", wishlistCount);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error adding product to wishlist: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Support endpoint for backwards compatibility with server-side wishlists
     * This is no longer the primary method for removing items from wishlist
     */
    @PostMapping("/remove")
    @ResponseBody
    public ResponseEntity<?> removeFromWishlist(@RequestBody Map<String, Object> payload,
                                  HttpSession session) {
        
        Long productId = Long.valueOf(payload.get("productId").toString());
        
        String username = (String) session.getAttribute("username");
        Map<String, Object> response = new HashMap<>();
        
        if (username == null) {
            // For browser-side wishlist, allow removal without login
            response.put("success", true);
            response.put("browserOnly", true);
            return ResponseEntity.ok(response);
        }
        
        try {
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
                    
            Product product = productService.getProductById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
                    
            wishlistService.removeProductFromWishlist(user, product);
            
            // Return wishlist count
            int wishlistCount = wishlistService.getWishlistItemCount(user);
            response.put("success", true);
            response.put("message", "Product removed from wishlist successfully");
            response.put("wishlistCount", wishlistCount);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error removing product from wishlist: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Compatibility endpoint that provides dummy data
     * Browser-side wishlist now manages counts directly
     */
    @GetMapping("/count")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getWishlistCount(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        String username = (String) session.getAttribute("username");
        if (username == null) {
            response.put("wishlistCount", 0);
            return ResponseEntity.ok(response);
        }
        
        try {
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            int wishlistCount = wishlistService.getWishlistItemCount(user);
            response.put("wishlistCount", wishlistCount);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Compatibility endpoint that provides mini wishlist data
     * Browser-side wishlist now manages this directly
     */
    @GetMapping("/mini")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getMiniWishlist(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        String username = (String) session.getAttribute("username");
        if (username == null) {
            response.put("items", List.of());
            return ResponseEntity.ok(response);
        }
        
        try {
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            List<Product> wishlistItems = wishlistService.getWishlistItems(user);
            List<Map<String, Object>> items = new ArrayList<>();
            
            for (Product product : wishlistItems) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", product.getId());
                item.put("name", product.getName());
                item.put("price", "$" + product.getPrice());
                
                // Get image URL (primary image or first image or fallback to placeholder)
                String imageUrl = "/images/product-placeholder.jpg"; // Default placeholder
                if (product.getImages() != null && !product.getImages().isEmpty()) {
                    imageUrl = product.getImages().stream()
                        .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                        .findFirst()
                        .orElse(product.getImages().get(0))
                        .getImageUrl();
                }
                item.put("image", imageUrl);
                
                items.add(item);
            }
            
            response.put("items", items);
            response.put("count", items.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Endpoint for browser-side wishlist to get multiple products at once
     */
    @GetMapping("/products")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getWishlistProducts(@RequestParam List<Long> ids) {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> productList = new ArrayList<>();
        
        try {
            for (Long id : ids) {
                Product product = productService.getProductById(id)
                        .orElse(null);
                
                if (product != null) {
                    Map<String, Object> productData = new HashMap<>();
                    productData.put("id", product.getId());
                    productData.put("name", product.getName());
                    productData.put("price", product.getPrice());
                    productData.put("salePrice", product.getSalePrice());
                    
                    // Get primary image or first image URL
                    String imageUrl = "/images/product-placeholder.jpg"; // Default placeholder
                    if (product.getImages() != null && !product.getImages().isEmpty()) {
                        imageUrl = product.getImages().stream()
                            .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                            .findFirst()
                            .orElse(product.getImages().get(0))
                            .getImageUrl();
                    }
                    productData.put("imageUrl", imageUrl);
                    
                    productList.add(productData);
                }
            }
            
            response.put("products", productList);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error getting products: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
