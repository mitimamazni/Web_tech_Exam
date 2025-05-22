package com.ecommerce.app.dto;

import com.ecommerce.app.model.ProductImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageDTO {
    private Long id;
    private String imageUrl;
    private Boolean isPrimary;
    private Integer displayOrder;
    
    // Convert from Entity to DTO
    public static ProductImageDTO fromEntity(ProductImage image) {
        if (image == null) {
            return null;
        }
        
        return ProductImageDTO.builder()
            .id(image.getId())
            .imageUrl(image.getImageUrl())
            .isPrimary(image.getIsPrimary())
            .displayOrder(image.getDisplayOrder())
            .build();
    }
    
    // Convert list of entities to list of DTOs
    public static List<ProductImageDTO> fromEntities(List<ProductImage> images) {
        if (images == null) {
            return new ArrayList<>();
        }
        
        return images.stream()
            .map(ProductImageDTO::fromEntity)
            .collect(Collectors.toList());
    }
}
