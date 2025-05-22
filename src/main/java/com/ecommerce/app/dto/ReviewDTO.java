package com.ecommerce.app.dto;

import com.ecommerce.app.model.Review;
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
public class ReviewDTO {
    private Long id;
    private Long productId;
    private String productName;
    private Long userId;
    private String userName;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    
    // Convert from Entity to DTO
    public static ReviewDTO fromEntity(Review review) {
        if (review == null) {
            return null;
        }
        
        return ReviewDTO.builder()
            .id(review.getId())
            .productId(review.getProduct() != null ? review.getProduct().getId() : null)
            .productName(review.getProduct() != null ? review.getProduct().getName() : null)
            .userId(review.getUser() != null ? review.getUser().getId() : null)
            .userName(review.getUser() != null ? 
                review.getUser().getFirstName() + " " + review.getUser().getLastName() : "Anonymous")
            .rating(review.getRating())
            .comment(review.getComment())
            .createdAt(review.getCreatedAt())
            .build();
    }
    
    // Convert list of entities to list of DTOs
    public static List<ReviewDTO> fromEntities(List<Review> reviews) {
        if (reviews == null) {
            return new ArrayList<>();
        }
        
        return reviews.stream()
            .map(ReviewDTO::fromEntity)
            .collect(Collectors.toList());
    }
}
