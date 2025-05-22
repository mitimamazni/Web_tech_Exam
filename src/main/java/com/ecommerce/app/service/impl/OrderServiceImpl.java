package com.ecommerce.app.service.impl;

import com.ecommerce.app.model.*;
import com.ecommerce.app.repository.OrderRepository;
import com.ecommerce.app.service.CartService;
import com.ecommerce.app.service.OrderService;
import com.ecommerce.app.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductService productService;

    @Override
    @Transactional
    public Order createOrder(User user, String shippingAddress) {
        try {
            List<CartItem> cartItems = cartService.getCartItems(user);
            
            if (cartItems.isEmpty()) {
                throw new RuntimeException("Cannot create order with empty cart");
            }
            
            Order order = new Order();
            order.setUser(user);
            order.setStatus(OrderStatus.PENDING);
            order.setShippingAddress(shippingAddress);
            // Set default billing address and payment method to prevent validation errors
            order.setBillingAddress(shippingAddress); // Default to same as shipping
            order.setPaymentMethod("Credit Card"); // Default payment method
            
            // First check if all products have sufficient stock
            for (CartItem cartItem : cartItems) {
                Product product = cartItem.getProduct();
                if (product.getStockQuantity() < cartItem.getQuantity()) {
                    throw new RuntimeException("Not enough stock available for product: " + product.getName());
                }
            }
            
            // Convert cart items to order items
            List<OrderItem> orderItems = cartItems.stream()
                    .map(cartItem -> {
                        OrderItem orderItem = new OrderItem();
                        orderItem.setOrder(order);
                        orderItem.setProduct(cartItem.getProduct());
                        orderItem.setQuantity(cartItem.getQuantity());
                        
                        // Use sale price if available, otherwise use regular price
                        BigDecimal price = cartItem.getProduct().getSalePrice() != null ? 
                                cartItem.getProduct().getSalePrice() : cartItem.getProduct().getPrice();
                        orderItem.setPrice(price);
                        
                        try {
                            // Update product stock
                            productService.updateProductStock(cartItem.getProduct().getId(), cartItem.getQuantity());
                        } catch (Exception e) {
                            throw new RuntimeException("Error updating product stock: " + e.getMessage());
                        }
                        
                        return orderItem;
                    })
                    .collect(Collectors.toList());
            
            // Add each order item using the addOrderItem helper method
            for (OrderItem item : orderItems) {
                order.addOrderItem(item);
            }
            
            // Calculate the total before saving
            order.calculateTotal();
            
            // Save the order first before clearing the cart
            Order savedOrder = orderRepository.save(order);
            
            // Clear the cart after creating order
            cartService.clearCart(user);
            
            return savedOrder;
        } catch (Exception e) {
            // Roll back the transaction and rethrow with a clear message
            throw new RuntimeException("Failed to create order: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Order> getOrdersByUser(User user) {
        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    @Override
    @Transactional
    public Order updateOrderStatus(Long orderId, OrderStatus status) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setStatus(status);
            return orderRepository.save(order);
        }
        throw new RuntimeException("Order not found with id: " + orderId);
    }

    @Override
    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    @Override
    public List<Order> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findByCreatedAtBetween(startDate, endDate);
    }

    @Override
    public List<Order> getRecentOrdersByUser(User user, int limit) {
        return orderRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public Page<Order> getAllOrdersPaged(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }
}
