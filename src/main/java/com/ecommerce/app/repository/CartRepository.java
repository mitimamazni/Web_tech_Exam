package com.ecommerce.app.repository;

import com.ecommerce.app.model.Cart;
import com.ecommerce.app.model.CartStatus;
import com.ecommerce.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    List<Cart> findByUser(User user);
    
    List<Cart> findByUserAndStatus(User user, CartStatus status);
    
    Optional<Cart> findFirstByUserAndStatusOrderByCreatedAtDesc(User user, CartStatus status);
}
