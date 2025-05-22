package com.ecommerce.app.service;

import com.ecommerce.app.model.Cart;
import com.ecommerce.app.model.CartItem;
import com.ecommerce.app.model.Product;
import com.ecommerce.app.model.User;

import java.math.BigDecimal;
import java.util.List;

public interface CartService {
    // Get active cart for user or create one if it doesn't exist
    Cart getActiveCart(User user);
    
    // Legacy methods for backward compatibility
    CartItem addProductToCart(User user, Product product, int quantity);
    void removeProductFromCart(User user, Long productId);
    void updateCartItemQuantity(User user, Long productId, int quantity);
    List<CartItem> getCartItems(User user);
    void clearCart(User user);
    BigDecimal getCartTotal(User user);
    int getCartItemCount(User user);
    
    // New methods using Cart directly
    CartItem addProductToCart(Cart cart, Product product, int quantity);
    void removeProductFromCart(Cart cart, Long productId);
    void updateCartItemQuantity(Cart cart, Long productId, int quantity);
    void clearCart(Cart cart);
    BigDecimal getCartTotal(Cart cart);
    int getCartItemCount(Cart cart);
}
