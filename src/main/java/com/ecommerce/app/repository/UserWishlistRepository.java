package com.ecommerce.app.repository;

import com.ecommerce.app.model.User;
import com.ecommerce.app.model.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserWishlistRepository extends JpaRepository<Wishlist, Long> {
    
    Optional<Wishlist> findByUser(User user);
}
