package com.ecommerce.app.repository;

import com.ecommerce.app.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    // Add custom queries if needed
}
