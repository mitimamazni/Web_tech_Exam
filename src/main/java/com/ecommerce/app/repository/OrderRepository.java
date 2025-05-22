package com.ecommerce.app.repository;

import com.ecommerce.app.model.Order;
import com.ecommerce.app.model.OrderStatus;
import com.ecommerce.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
    
    List<Order> findByUserOrderByCreatedAtDesc(User user);
    
    List<Order> findByStatus(OrderStatus status);
    
    List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
