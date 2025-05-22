package com.ecommerce.app.controller.api;

import com.ecommerce.app.dto.ApiResponseDTO;
import com.ecommerce.app.dto.OrderDTO;
import com.ecommerce.app.model.Order;
import com.ecommerce.app.model.OrderStatus;
import com.ecommerce.app.model.User;
import com.ecommerce.app.service.OrderService;
import com.ecommerce.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderRestController {

    @Autowired
    private OrderService orderService;
    
    @Autowired
    private UserService userService;
    
    /**
     * Get orders for a user
     * @param username the username of the user
     * @return list of orders for the user
     */
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<OrderDTO>>> getUserOrders(
            @RequestParam(required = false) String username) {
        
        if (username == null || username.isEmpty()) {
            return ResponseEntity.status(400)
                    .body(ApiResponseDTO.error("Username is required", 400));
        }
        
        Optional<User> userOpt = userService.findByUsername(username);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404)
                    .body(ApiResponseDTO.error("User not found", 404));
        }
        
        User user = userOpt.get();
        List<Order> orders = orderService.getOrdersByUser(user);
        
        List<OrderDTO> orderDTOs = orders.stream()
                .map(OrderDTO::fromEntity)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponseDTO.success(orderDTOs));
    }
    
    /**
     * Get a specific order by ID
     * @param id the order ID
     * @param username the username of the requesting user (for authorization)
     * @return the order details
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<OrderDTO>> getOrderById(
            @PathVariable Long id,
            @RequestParam(required = false) String username) {
        
        if (username == null || username.isEmpty()) {
            return ResponseEntity.status(400)
                    .body(ApiResponseDTO.error("Username is required", 400));
        }
        
        Optional<User> userOpt = userService.findByUsername(username);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404)
                    .body(ApiResponseDTO.error("User not found", 404));
        }
        
        User user = userOpt.get();
        Optional<Order> orderOpt = orderService.getOrderById(id);
        
        if (orderOpt.isEmpty()) {
            return ResponseEntity.status(404)
                    .body(ApiResponseDTO.error("Order not found", 404));
        }
        
        Order order = orderOpt.get();
        
        // Check if the user is authorized to view this order
        /*if (!order.getUser().getId().equals(user.getId()) && !user.getRoles().stream()
                .anyMatch(role -> role.getName() == ERole.ROLE_ADMIN)) {
            return ResponseEntity.status(403)
                    .body(ApiResponseDTO.error("Not authorized to view this order", 403));
        }
        */
        OrderDTO orderDTO = OrderDTO.fromEntity(order);
        return ResponseEntity.ok(ApiResponseDTO.success(orderDTO));
    }
    
    /**
     * Get the most recent orders for a user
     * @param username the username of the user
     * @param limit the maximum number of orders to return (default 5)
     * @return list of recent orders
     */
    @GetMapping("/recent")
    public ResponseEntity<ApiResponseDTO<List<OrderDTO>>> getRecentOrders(
            @RequestParam(required = false) String username,
            @RequestParam(defaultValue = "5") int limit) {
        
        if (username == null || username.isEmpty()) {
            return ResponseEntity.status(400)
                    .body(ApiResponseDTO.error("Username is required", 400));
        }
        
        Optional<User> userOpt = userService.findByUsername(username);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404)
                    .body(ApiResponseDTO.error("User not found", 404));
        }
        
        User user = userOpt.get();
        List<Order> orders = orderService.getRecentOrdersByUser(user, limit);
        
        List<OrderDTO> orderDTOs = orders.stream()
                .map(OrderDTO::fromEntity)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponseDTO.success(orderDTOs));
    }
    
    /**
     * For admin use only - get all orders in the system
     * @param username the username of the admin user
     * @param status optional filter by order status
     * @return list of all orders
     */
    @GetMapping("/admin/all")
    public ResponseEntity<ApiResponseDTO<List<OrderDTO>>> getAllOrders(
            @RequestParam String username,
            @RequestParam(required = false) OrderStatus status) {
        
        Optional<User> userOpt = userService.findByUsername(username);
        
        /*if (userOpt.isEmpty() || !userOpt.get().getRoles().stream()
                .anyMatch(role -> role.getName() == ERole.ROLE_ADMIN)) {
            return ResponseEntity.status(403)
                    .body(ApiResponseDTO.error("Administrative privileges required", 403));
        }
        */
        List<Order> orders;
        if (status != null) {
            orders = orderService.getOrdersByStatus(status);
        } else {
            orders = orderService.getAllOrders();
        }
        
        List<OrderDTO> orderDTOs = orders.stream()
                .map(OrderDTO::fromEntity)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponseDTO.success(orderDTOs));
    }
}
