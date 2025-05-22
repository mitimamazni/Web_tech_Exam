package com.ecommerce.app.service;

import com.ecommerce.app.dto.ReviewDTO;
import com.ecommerce.app.model.Review;
import java.util.List;

/**
 * Service interface for managing product reviews
 */
public interface ReviewService {

    /**
     * Get all reviews for a product
     */
    List<ReviewDTO> getReviewsByProductId(Long productId);
    
    /**
     * Add a new review for a product
     */
    ReviewDTO addReview(ReviewDTO reviewDTO, Long userId, Long productId);
    
    /**
     * Update an existing review
     */
    ReviewDTO updateReview(Long reviewId, ReviewDTO reviewDTO);
    
    /**
     * Delete a review
     */
    void deleteReview(Long reviewId);
    
    /**
     * Get average rating for a product
     */
    Double getAverageRatingForProduct(Long productId);
}
