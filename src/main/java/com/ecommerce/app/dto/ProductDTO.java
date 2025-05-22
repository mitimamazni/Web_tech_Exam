package com.ecommerce.app.dto;

import com.ecommerce.app.model.Product;
import com.ecommerce.app.model.ProductImage;
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
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal salePrice;
    private Integer stockQuantity;
    private Boolean isSubscription;
    private CategoryDTO category;
    private List<ProductImageDTO> images = new ArrayList<>();
    private List<TagDTO> tags = new ArrayList<>();
    private Double averageRating;
    private LocalDateTime createdAt;
    private boolean active;
    
    // Helper method to get the primary image URL
    public String getImageUrl() {
        if (images == null || images.isEmpty()) {
            return "/images/product-placeholder.jpg";
        }
        
        return images.stream()
            .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
            .findFirst()
            .orElse(images.get(0))
            .getImageUrl();
    }
    
    // Convert from Entity to DTO
    public static ProductDTO fromEntity(Product product) {
        if (product == null) {
            return null;
        }
        
        return ProductDTO.builder()
            .id(product.getId())
            .name(product.getName())
            .description(product.getDescription())
            .price(product.getPrice())
            .salePrice(product.getSalePrice())
            .stockQuantity(product.getStockQuantity())
            .isSubscription(product.getIsSubscription())
            .category(CategoryDTO.fromEntity(product.getCategory()))
            .images(product.getImages() != null 
                   ? product.getImages().stream()
                       .map(ProductImageDTO::fromEntity)
                       .collect(Collectors.toList())
                   : new ArrayList<>())
            .tags(product.getTags() != null
                 ? product.getTags().stream()
                     .map(TagDTO::fromEntity)
                     .collect(Collectors.toList())
                 : new ArrayList<>())
            .averageRating(product.getAverageRating())
            .createdAt(product.getCreatedAt())
            .active(product.isActive())
            .build();
    }
    
    // Convert list of entities to list of DTOs
    public static List<ProductDTO> fromEntities(List<Product> products) {
        if (products == null) {
            return new ArrayList<>();
        }
        
        return products.stream()
            .map(ProductDTO::fromEntity)
            .collect(Collectors.toList());
    }
}
