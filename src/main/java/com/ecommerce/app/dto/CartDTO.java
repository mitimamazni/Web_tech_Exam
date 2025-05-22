package com.ecommerce.app.dto;

import com.ecommerce.app.model.Cart;
import com.ecommerce.app.model.CartStatus;
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
public class CartDTO {
    private Long id;
    private Long userId;
    private List<CartItemDTO> items = new ArrayList<>();
    private CartStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private BigDecimal totalPrice;
    private Integer itemCount;
    
    // Convert from Entity to DTO
    public static CartDTO fromEntity(Cart cart) {
        if (cart == null) {
            return null;
        }
        
        List<CartItemDTO> itemDTOs = cart.getCartItems() != null
            ? CartItemDTO.fromEntities(cart.getCartItems())
            : new ArrayList<>();
            
        // Calculate total price
        BigDecimal total = BigDecimal.ZERO;
        if (cart.getCartItems() != null) {
            total = cart.getCartItems().stream()
                .map(item -> item.getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        
        // Calculate item count
        int count = cart.getCartItems() != null
            ? cart.getCartItems().stream()
                .mapToInt(item -> item.getQuantity())
                .sum()
            : 0;
        
        return CartDTO.builder()
            .id(cart.getId())
            .userId(cart.getUser() != null ? cart.getUser().getId() : null)
            .items(itemDTOs)
            .status(cart.getStatus())
            .createdAt(cart.getCreatedAt())
            .updatedAt(cart.getUpdatedAt())
            .totalPrice(total)
            .itemCount(count)
            .build();
    }
}
