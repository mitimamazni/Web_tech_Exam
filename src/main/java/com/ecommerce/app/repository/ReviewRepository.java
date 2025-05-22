package com.ecommerce.app.repository;

import com.ecommerce.app.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    // Add custom queries if needed
}
