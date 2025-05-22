package com.ecommerce.app.repository;

import com.ecommerce.app.model.Cart;
import com.ecommerce.app.model.CartItem;
import com.ecommerce.app.model.Product;
import com.ecommerce.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    // For backward compatibility - will use cart join query
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.user = :user")
    List<CartItem> findByUser(@Param("user") User user);
    
    // For backward compatibility - will use cart join query
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.user = :user AND ci.product = :product")
    Optional<CartItem> findByUserAndProduct(@Param("user") User user, @Param("product") Product product);
    
    // For backward compatibility
    @Query("DELETE FROM CartItem ci WHERE ci.cart.user = :user")
    void deleteByUser(@Param("user") User user);
    
    // New methods
    List<CartItem> findByCart(Cart cart);
    
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
    
    void deleteByCart(Cart cart);
}
