package com.ecommerce.app.service;

import com.ecommerce.app.model.Product;
import com.ecommerce.app.model.Review;
import com.ecommerce.app.model.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    List<Product> getAllProducts();
    
    List<Product> getAllProductsSorted(Sort sort);
    
    Page<Product> getAllProductsPaged(Pageable pageable);
    
    Page<Product> getProductsByCategory(Long categoryId, Pageable pageable);
    
    Page<Product> searchProducts(String keyword, Pageable pageable);
    
    Optional<Product> getProductById(Long id);
    
    Product saveProduct(Product product);
    
    void deleteProduct(Long id);
    
    void updateProductStock(Long productId, int quantity);
    
    List<Product> getActiveProducts();
    
    Page<Product> getActiveProductsPaged(Pageable pageable);
    
    // Count products in a category
    int countProductsByCategory(Long categoryId);
    
    // Admin functions for product management
    void addProductImages(Product product, List<MultipartFile> imageFiles, boolean setFirstAsPrimary);
    
    void deleteProductImages(Product product, List<Long> imageIds);
    
    void setPrimaryProductImage(Product product, Long imageId);
    
    List<Tag> getAllTags();
    
    void setProductTags(Product product, List<Long> tagIds);
    
    // Review management
    Optional<Review> getReviewById(Long id);
    
    void deleteReview(Long reviewId);
    
    // Tag management
    Tag saveTag(Tag tag);
    
    void deleteTag(Long tagId);
}
