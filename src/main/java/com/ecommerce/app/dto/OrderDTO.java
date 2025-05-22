package com.ecommerce.app.dto;

import com.ecommerce.app.model.Order;
import com.ecommerce.app.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Long id;
    private String orderNumber;
    private Long userId;
    private String userName;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private String shippingAddress;
    private String billingAddress;
    private String paymentMethod;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItemDTO> items = new ArrayList<>();
    
    // Convert from Entity to DTO
    public static OrderDTO fromEntity(Order order) {
        if (order == null) {
            return null;
        }
        
        // Format order number with leading zeros
        String orderNumber = String.format("ORD-%07d", order.getId());
        
        return OrderDTO.builder()
            .id(order.getId())
            .orderNumber(orderNumber)
            .userId(order.getUser() != null ? order.getUser().getId() : null)
            .userName(order.getUser() != null ? 
                order.getUser().getFirstName() + " " + order.getUser().getLastName() : "Guest")
            .totalAmount(order.getTotalAmount())
            .status(order.getStatus())
            .shippingAddress(order.getShippingAddress())
            .billingAddress(order.getBillingAddress())
            .paymentMethod(order.getPaymentMethod())
            .createdAt(order.getCreatedAt())
            .updatedAt(order.getUpdatedAt())
            .items(OrderItemDTO.fromEntities(order.getOrderItems()))
            .build();
    }
    
    // Convert list of entities to list of DTOs
    public static List<OrderDTO> fromEntities(List<Order> orders) {
        if (orders == null) {
            return new ArrayList<>();
        }
        
        return orders.stream()
            .map(OrderDTO::fromEntity)
            .toList();
    }
}
