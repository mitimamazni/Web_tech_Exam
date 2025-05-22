package com.ecommerce.app.controller.api;

import com.ecommerce.app.dto.ApiResponseDTO;
import com.ecommerce.app.dto.PageResponseDTO;
import com.ecommerce.app.dto.ProductDTO;
import com.ecommerce.app.dto.ReviewDTO;
import com.ecommerce.app.model.Product;
import com.ecommerce.app.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
public class ProductRestController {

    @Autowired
    private ProductService productService;
    
    @GetMapping
    public ResponseEntity<ApiResponseDTO<PageResponseDTO<ProductDTO>>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? 
            Sort.Direction.DESC : Sort.Direction.ASC;
            
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<Product> productPage = productService.getAllProductsPaged(pageable);
        
        List<ProductDTO> productDTOs = ProductDTO.fromEntities(productPage.getContent());
        PageResponseDTO<ProductDTO> pageResponse = PageResponseDTO.fromPage(productPage, productDTOs);
        
        return ResponseEntity.ok(ApiResponseDTO.success(pageResponse));
    }
    
    /**
     * Get all products without pagination
     * Warning: This endpoint should be used carefully as it may return a large dataset
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponseDTO<List<ProductDTO>>> getAllProductsNoPage(
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? 
            Sort.Direction.DESC : Sort.Direction.ASC;
            
        Sort sort = Sort.by(sortDirection, sortBy);
        List<Product> products = productService.getAllProductsSorted(sort);
        
        List<ProductDTO> productDTOs = ProductDTO.fromEntities(products);
        
        return ResponseEntity.ok(ApiResponseDTO.success(productDTOs));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<ProductDTO>> getProduct(@PathVariable Long id) {
        Optional<Product> productOpt = productService.getProductById(id);
        
        if (productOpt.isPresent()) {
            ProductDTO productDTO = ProductDTO.fromEntity(productOpt.get());
            return ResponseEntity.ok(ApiResponseDTO.success(productDTO));
        } else {
            return ResponseEntity.status(404)
                .body(ApiResponseDTO.error("Product not found", 404));
        }
    }
    
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponseDTO<PageResponseDTO<ProductDTO>>> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productService.getProductsByCategory(categoryId, pageable);
        
        List<ProductDTO> productDTOs = ProductDTO.fromEntities(productPage.getContent());
        PageResponseDTO<ProductDTO> pageResponse = PageResponseDTO.fromPage(productPage, productDTOs);
        
        return ResponseEntity.ok(ApiResponseDTO.success(pageResponse));
    }
    
    @GetMapping("/search")
    public ResponseEntity<ApiResponseDTO<PageResponseDTO<ProductDTO>>> searchProducts(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productService.searchProducts(query, pageable);
        
        List<ProductDTO> productDTOs = ProductDTO.fromEntities(productPage.getContent());
        PageResponseDTO<ProductDTO> pageResponse = PageResponseDTO.fromPage(productPage, productDTOs);
        
        return ResponseEntity.ok(ApiResponseDTO.success(pageResponse));
    }
    
    @GetMapping("/{id}/reviews")
    public ResponseEntity<ApiResponseDTO<List<ReviewDTO>>> getProductReviews(@PathVariable Long id) {
        Optional<Product> productOpt = productService.getProductById(id);
        
        if (productOpt.isPresent()) {
            List<ReviewDTO> reviewDTOs = ReviewDTO.fromEntities(productOpt.get().getReviews());
            return ResponseEntity.ok(ApiResponseDTO.success(reviewDTOs));
        } else {
            return ResponseEntity.status(404)
                .body(ApiResponseDTO.error("Product not found", 404));
        }
    }
}
