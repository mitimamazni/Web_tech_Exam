package com.ecommerce.app.repository;

import com.ecommerce.app.model.ProductImage;
import com.ecommerce.app.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByProduct(Product product);
    List<ProductImage> findByProductIdOrderByDisplayOrder(Long productId);
    ProductImage findByProductAndIsPrimaryTrue(Product product);
}
