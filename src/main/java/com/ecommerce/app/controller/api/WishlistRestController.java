package com.ecommerce.app.controller.api;

import com.ecommerce.app.dto.ApiResponseDTO;
import com.ecommerce.app.dto.WishlistDTO;
import com.ecommerce.app.model.Product;
import com.ecommerce.app.model.User;
import com.ecommerce.app.model.Wishlist;
import com.ecommerce.app.service.ProductService;
import com.ecommerce.app.service.UserService;
import com.ecommerce.app.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistRestController {

    @Autowired
    private WishlistService wishlistService;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping
    public ResponseEntity<ApiResponseDTO<WishlistDTO>> getWishlist(@RequestParam String username) {
        if (username == null || username.isEmpty()) {
            return ResponseEntity.status(401)
                .body(ApiResponseDTO.error("Username is required", 401));
        }
        
        Optional<User> userOpt = userService.findByUsername(username);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404)
                .body(ApiResponseDTO.error("User not found", 404));
        }
        
        User user = userOpt.get();
        Wishlist wishlist = wishlistService.getUserWishlist(user);
        WishlistDTO wishlistDTO = WishlistDTO.fromEntity(wishlist);
        
        return ResponseEntity.ok(ApiResponseDTO.success(wishlistDTO));
    }
    
    @PostMapping("/add")
    public ResponseEntity<ApiResponseDTO<WishlistDTO>> addToWishlist(
            @RequestParam Long productId,
            @RequestParam String username) {
        
        if (username == null || username.isEmpty()) {
            return ResponseEntity.status(401)
                .body(ApiResponseDTO.error("Username is required", 401));
        }
        
        Optional<User> userOpt = userService.findByUsername(username);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404)
                .body(ApiResponseDTO.error("User not found", 404));
        }
        
        Optional<Product> productOpt = productService.getProductById(productId);
        if (productOpt.isEmpty()) {
            return ResponseEntity.status(404)
                .body(ApiResponseDTO.error("Product not found", 404));
        }
        
        User user = userOpt.get();
        Product product = productOpt.get();
        
        wishlistService.addProductToWishlist(user, product);
        Wishlist wishlist = wishlistService.getUserWishlist(user);
        WishlistDTO wishlistDTO = WishlistDTO.fromEntity(wishlist);
        
        return ResponseEntity.ok(ApiResponseDTO.success(wishlistDTO));
    }
    
    @DeleteMapping("/remove")
    public ResponseEntity<ApiResponseDTO<WishlistDTO>> removeFromWishlist(
            @RequestParam Long productId,
            @RequestParam String username) {
        
        if (username == null || username.isEmpty()) {
            return ResponseEntity.status(401)
                .body(ApiResponseDTO.error("Username is required", 401));
        }
        
        Optional<User> userOpt = userService.findByUsername(username);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404)
                .body(ApiResponseDTO.error("User not found", 404));
        }
        
        Optional<Product> productOpt = productService.getProductById(productId);
        if (productOpt.isEmpty()) {
            return ResponseEntity.status(404)
                .body(ApiResponseDTO.error("Product not found", 404));
        }
        
        User user = userOpt.get();
        Product product = productOpt.get();
        
        wishlistService.removeProductFromWishlist(user, product);
        Wishlist wishlist = wishlistService.getUserWishlist(user);
        WishlistDTO wishlistDTO = WishlistDTO.fromEntity(wishlist);
        
        return ResponseEntity.ok(ApiResponseDTO.success(wishlistDTO));
    }
    
    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponseDTO<String>> clearWishlist(@RequestParam String username) {
        if (username == null || username.isEmpty()) {
            return ResponseEntity.status(401)
                .body(ApiResponseDTO.error("Username is required", 401));
        }
        
        Optional<User> userOpt = userService.findByUsername(username);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404)
                .body(ApiResponseDTO.error("User not found", 404));
        }
        
        User user = userOpt.get();
        wishlistService.clearWishlist(user);
        
        return ResponseEntity.ok(ApiResponseDTO.success("Wishlist cleared successfully"));
    }
}
