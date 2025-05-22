package com.ecommerce.app.service;

import com.ecommerce.app.model.Order;
import com.ecommerce.app.model.OrderStatus;
import com.ecommerce.app.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderService {
    Order createOrder(User user, String shippingAddress);
    
    List<Order> getOrdersByUser(User user);
    
    List<Order> getAllOrders();
    
    Page<Order> getAllOrdersPaged(Pageable pageable);
    
    Optional<Order> getOrderById(Long id);
    
    Order updateOrderStatus(Long orderId, OrderStatus status);
    
    List<Order> getOrdersByStatus(OrderStatus status);
    
    List<Order> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Get the most recent orders for a user with limit
     * @param user The user
     * @param limit Maximum number of orders to return
     * @return List of recent orders
     */
    List<Order> getRecentOrdersByUser(User user, int limit);
}
