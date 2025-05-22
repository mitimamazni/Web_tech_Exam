package com.ecommerce.app.dto;

import com.ecommerce.app.model.Wishlist;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishlistDTO {
    private Long id;
    private Long userId;
    private List<WishlistItemDTO> items = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer itemCount;
    
    // Convert from Entity to DTO
    public static WishlistDTO fromEntity(Wishlist wishlist) {
        if (wishlist == null) {
            return null;
        }
        
        List<WishlistItemDTO> itemDTOs = new ArrayList<>();
        if (wishlist.getWishlistItems() != null) {
            itemDTOs = wishlist.getWishlistItems().stream()
                .map(WishlistItemDTO::new)
                .collect(Collectors.toList());
        }
        
        return WishlistDTO.builder()
            .id(wishlist.getId())
            .userId(wishlist.getUser() != null ? wishlist.getUser().getId() : null)
            .items(itemDTOs)
            .createdAt(wishlist.getCreatedAt())
            .updatedAt(wishlist.getUpdatedAt())
            .itemCount(itemDTOs.size())
            .build();
    }
}
