package com.ecommerce.app.repository;

import com.ecommerce.app.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);
    
    List<Product> findByActive(boolean active);
    
    Page<Product> findByActiveTrue(Pageable pageable);
    
    List<Product> findByActiveTrue();
    
    int countByCategoryId(Long categoryId);
}
