package com.ecommerce.app.repository;

import com.ecommerce.app.model.Product;
import com.ecommerce.app.model.User;
import com.ecommerce.app.model.Wishlist;
import com.ecommerce.app.model.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<WishlistItem, Long> {
    @Query("SELECT w FROM WishlistItem w WHERE w.wishlist.user = :user")
    List<WishlistItem> findByUser(@Param("user") User user);
    
    @Query("SELECT w FROM WishlistItem w WHERE w.wishlist.user = :user AND w.product = :product")
    Optional<WishlistItem> findByUserAndProduct(@Param("user") User user, @Param("product") Product product);
    
    @Query("SELECT COUNT(w) FROM WishlistItem w WHERE w.wishlist.user = :user")
    int countByUser(@Param("user") User user);
    
    @Query("SELECT CASE WHEN COUNT(w) > 0 THEN true ELSE false END FROM WishlistItem w WHERE w.wishlist.user = :user AND w.product = :product")
    boolean existsByUserAndProduct(@Param("user") User user, @Param("product") Product product);
    
    @Modifying
    @Query("DELETE FROM WishlistItem w WHERE w.wishlist.user = :user AND w.product = :product")
    void deleteByUserAndProduct(@Param("user") User user, @Param("product") Product product);
    
    @Modifying
    @Query("DELETE FROM WishlistItem w WHERE w.wishlist.user = :user")
    void deleteByUser(@Param("user") User user);
}
