package com.ecommerce.app.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpSession;

import com.ecommerce.app.dto.ApiResponseDTO;
import com.ecommerce.app.model.User;
import com.ecommerce.app.service.UserService;
import com.ecommerce.app.service.WishlistService;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * REST API Controller for wishlist related operations
 */
@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistApiController {
    
    private final WishlistService wishlistService;
    private final UserService userService;
    
    /**
     * Get the current wishlist count for the authenticated user
     * 
     * @return JSON response with wishlist count
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> getWishlistCount(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("count", 0);
            
            return ResponseEntity.ok(
                ApiResponseDTO.<Map<String, Object>>builder()
                    .success(false)
                    .message("User not authenticated")
                    .data(response)
                    .build()
            );
        }
        
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        int count = wishlistService.getWishlistItemCount(user);
        
        Map<String, Object> response = new HashMap<>();
        response.put("count", count);
        
        return ResponseEntity.ok(
            ApiResponseDTO.<Map<String, Object>>builder()
                .success(true)
                .message("Wishlist count retrieved successfully")
                .data(response)
                .build()
        );
    }
}
