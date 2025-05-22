package com.ecommerce.app.dto;

import com.ecommerce.app.model.OrderItem;
import com.ecommerce.app.model.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {
    private Long id;
    private Long productId;
    private String productName;
    private String imageUrl;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal totalPrice;
    
    // Convert from Entity to DTO
    public static OrderItemDTO fromEntity(OrderItem orderItem) {
        if (orderItem == null) {
            return null;
        }
        
        Product product = orderItem.getProduct();
        String imageUrl = "/images/product-placeholder.jpg";
        
        if (product != null && product.getImages() != null && !product.getImages().isEmpty()) {
            imageUrl = product.getImages().stream()
                .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                .findFirst()
                .orElse(product.getImages().get(0))
                .getImageUrl();
        }
        
        return OrderItemDTO.builder()
            .id(orderItem.getId())
            .productId(product != null ? product.getId() : null)
            .productName(product != null ? product.getName() : "Unknown")
            .imageUrl(imageUrl)
            .quantity(orderItem.getQuantity())
            .price(orderItem.getPrice())
            .totalPrice(orderItem.getTotalPrice())
            .build();
    }
    
    // Convert list of entities to list of DTOs
    public static List<OrderItemDTO> fromEntities(List<OrderItem> orderItems) {
        if (orderItems == null) {
            return new ArrayList<>();
        }
        
        return orderItems.stream()
            .map(OrderItemDTO::fromEntity)
            .collect(Collectors.toList());
    }
}
