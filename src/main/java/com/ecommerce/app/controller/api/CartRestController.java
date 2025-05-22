package com.ecommerce.app.controller.api;

import com.ecommerce.app.dto.ApiResponseDTO;
import com.ecommerce.app.dto.CartDTO;
import com.ecommerce.app.model.Cart;
import com.ecommerce.app.model.Product;
import com.ecommerce.app.model.User;
import com.ecommerce.app.service.CartService;
import com.ecommerce.app.service.ProductService;
import com.ecommerce.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/cart")
public class CartRestController {

    @Autowired
    private CartService cartService;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private UserService userService;
    
    /**
     * Get the number of items in the user's cart
     * @param username the username of the user
     * @return the number of items in the cart
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponseDTO<Integer>> getCartItemCount(@RequestParam(required = false) String username) {
        if (username == null || username.isEmpty()) {
            return ResponseEntity.status(200)
                .body(ApiResponseDTO.success(0)); // Return zero items if no username
        }
        
        Optional<User> userOpt = userService.findByUsername(username);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(200)
                .body(ApiResponseDTO.success(0)); // Return zero items if user not found
        }
        
        User user = userOpt.get();
        Cart cart = cartService.getActiveCart(user);
        int itemCount = cartService.getCartItemCount(user);
        
        return ResponseEntity.ok(ApiResponseDTO.success(itemCount));
    }
    
    @GetMapping
    public ResponseEntity<ApiResponseDTO<CartDTO>> getCart(@RequestParam String username) {
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
        Cart cart = cartService.getActiveCart(user);
        CartDTO cartDTO = CartDTO.fromEntity(cart);
        
        return ResponseEntity.ok(ApiResponseDTO.success(cartDTO));
    }
    
    @PostMapping("/add")
    public ResponseEntity<ApiResponseDTO<CartDTO>> addToCart(
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") Integer quantity,
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
        
        if (product.getStockQuantity() < quantity) {
            return ResponseEntity.status(400)
                .body(ApiResponseDTO.error("Not enough stock", 400));
        }
        
        cartService.addProductToCart(user, product, quantity);
        Cart cart = cartService.getActiveCart(user);
        CartDTO cartDTO = CartDTO.fromEntity(cart);
        
        return ResponseEntity.ok(ApiResponseDTO.success(cartDTO));
    }
    
    @PostMapping("/update")
    public ResponseEntity<ApiResponseDTO<CartDTO>> updateCartItem(
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") Integer quantity,
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
        
        if (quantity <= 0) {
            // If quantity is 0 or negative, remove the item from cart
            cartService.removeProductFromCart(user, productId);
            Cart cart = cartService.getActiveCart(user);
            CartDTO cartDTO = CartDTO.fromEntity(cart);
            return ResponseEntity.ok(ApiResponseDTO.success(cartDTO));
        }
        
        if (product.getStockQuantity() < quantity) {
            return ResponseEntity.status(400)
                .body(ApiResponseDTO.error("Not enough stock", 400));
        }
        
        cartService.updateCartItemQuantity(user, productId, quantity);
        Cart cart = cartService.getActiveCart(user);
        CartDTO cartDTO = CartDTO.fromEntity(cart);
        
        return ResponseEntity.ok(ApiResponseDTO.success(cartDTO));
    }
    
    @DeleteMapping("/remove")
    public ResponseEntity<ApiResponseDTO<CartDTO>> removeFromCart(
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
        
        cartService.removeProductFromCart(user, productId);
        Cart cart = cartService.getActiveCart(user);
        CartDTO cartDTO = CartDTO.fromEntity(cart);
        
        return ResponseEntity.ok(ApiResponseDTO.success(cartDTO));
    }
    
    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponseDTO<String>> clearCart(@RequestParam String username) {
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
        cartService.clearCart(user);
        
        return ResponseEntity.ok(ApiResponseDTO.success("Cart cleared successfully"));
    }
}
