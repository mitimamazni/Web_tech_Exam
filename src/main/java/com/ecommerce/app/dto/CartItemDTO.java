package com.ecommerce.app.dto;

import com.ecommerce.app.model.CartItem;
import com.ecommerce.app.model.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {
    private Long id;
    private Long productId;
    private String productName;
    private String imageUrl;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal totalPrice;
    private Boolean inStock;
    private LocalDateTime createdAt;
    
    // Convert from Entity to DTO
    public static CartItemDTO fromEntity(CartItem cartItem) {
        if (cartItem == null) {
            return null;
        }
        
        Product product = cartItem.getProduct();
        String imageUrl = "/images/product-placeholder.jpg";
        
        if (product != null && product.getImages() != null && !product.getImages().isEmpty()) {
            imageUrl = product.getImages().stream()
                .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                .findFirst()
                .orElse(product.getImages().get(0))
                .getImageUrl();
        }
        
        boolean inStock = product != null && 
            product.getStockQuantity() != null && 
            product.getStockQuantity() >= cartItem.getQuantity();
        
        return CartItemDTO.builder()
            .id(cartItem.getId())
            .productId(product != null ? product.getId() : null)
            .productName(product != null ? product.getName() : "Unknown")
            .imageUrl(imageUrl)
            .quantity(cartItem.getQuantity())
            .price(cartItem.getPrice())
            .totalPrice(cartItem.getTotalPrice())
            .inStock(inStock)
            .createdAt(cartItem.getCreatedAt())
            .build();
    }
    
    // Convert list of entities to list of DTOs
    public static List<CartItemDTO> fromEntities(List<CartItem> cartItems) {
        if (cartItems == null) {
            return new ArrayList<>();
        }
        
        return cartItems.stream()
            .map(CartItemDTO::fromEntity)
            .collect(Collectors.toList());
    }
}
