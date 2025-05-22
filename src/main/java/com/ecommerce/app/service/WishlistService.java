package com.ecommerce.app.service;

import com.ecommerce.app.dto.WishlistItemDTO;
import com.ecommerce.app.model.Product;
import com.ecommerce.app.model.User;
import com.ecommerce.app.model.Wishlist;

import java.util.List;

public interface WishlistService {
    // User-based operations (server-side)
    void addProductToWishlist(User user, Product product);
    void removeProductFromWishlist(User user, Product product);
    void clearWishlist(User user);
    List<Product> getWishlistItems(User user);
    List<WishlistItemDTO> getWishlistItemDTOs(User user);
    int getWishlistItemCount(User user);
    boolean isProductInWishlist(User user, Long productId);
    
    // Get or create wishlist entity
    Wishlist getUserWishlist(User user);
}
