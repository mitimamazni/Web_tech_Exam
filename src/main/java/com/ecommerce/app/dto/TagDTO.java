package com.ecommerce.app.dto;

import com.ecommerce.app.model.Tag;
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
public class TagDTO {
    private Long id;
    private String name;
    private Integer productCount;
    
    // Convert from Entity to DTO
    public static TagDTO fromEntity(Tag tag) {
        if (tag == null) {
            return null;
        }
        
        return TagDTO.builder()
            .id(tag.getId())
            .name(tag.getName())
            .build();
    }
    
    // Convert from Entity to DTO with product count
    public static TagDTO fromEntityWithProductCount(Tag tag) {
        if (tag == null) {
            return null;
        }
        
        TagDTO dto = fromEntity(tag);
        if (tag.getProducts() != null) {
            dto.setProductCount(tag.getProducts().size());
        } else {
            dto.setProductCount(0);
        }
        
        return dto;
    }
    
    // Convert list of entities to list of DTOs
    public static List<TagDTO> fromEntities(List<Tag> tags) {
        if (tags == null) {
            return new ArrayList<>();
        }
        
        return tags.stream()
            .map(TagDTO::fromEntity)
            .collect(Collectors.toList());
    }
}
