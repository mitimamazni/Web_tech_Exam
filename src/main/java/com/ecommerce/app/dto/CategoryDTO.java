package com.ecommerce.app.dto;

import com.ecommerce.app.model.Category;
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
public class CategoryDTO {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private Integer productCount;
    
    // Convert from Entity to DTO
    public static CategoryDTO fromEntity(Category category) {
        if (category == null) {
            return null;
        }
        
        return CategoryDTO.builder()
            .id(category.getId())
            .name(category.getName())
            .build();
    }
    
    // Convert from Entity to DTO with product count
    public static CategoryDTO fromEntityWithProductCount(Category category, int productCount) {
        if (category == null) {
            return null;
        }
        
        return CategoryDTO.builder()
            .id(category.getId())
            .name(category.getName())
            .productCount(productCount)
            .build();
    }
    
    // Convert list of entities to list of DTOs
    public static List<CategoryDTO> fromEntities(List<Category> categories) {
        if (categories == null) {
            return new ArrayList<>();
        }
        
        return categories.stream()
            .map(CategoryDTO::fromEntity)
            .collect(Collectors.toList());
    }
}
