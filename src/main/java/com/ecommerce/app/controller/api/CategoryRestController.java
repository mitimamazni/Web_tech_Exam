package com.ecommerce.app.controller.api;

import com.ecommerce.app.dto.ApiResponseDTO;
import com.ecommerce.app.dto.CategoryDTO;
import com.ecommerce.app.model.Category;
import com.ecommerce.app.service.CategoryService;
import com.ecommerce.app.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/categories")
public class CategoryRestController {

    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private ProductService productService;
    
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<CategoryDTO>>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        List<CategoryDTO> categoryDTOs = new ArrayList<>();
        
        for (Category category : categories) {
            int productCount = productService.countProductsByCategory(category.getId());
            categoryDTOs.add(CategoryDTO.fromEntityWithProductCount(category, productCount));
        }
        
        return ResponseEntity.ok(ApiResponseDTO.success(categoryDTOs));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<CategoryDTO>> getCategory(@PathVariable Long id) {
        Optional<Category> categoryOpt = categoryService.getCategoryById(id);
        
        if (categoryOpt.isPresent()) {
            int productCount = productService.countProductsByCategory(id);
            CategoryDTO categoryDTO = CategoryDTO.fromEntityWithProductCount(categoryOpt.get(), productCount);
            return ResponseEntity.ok(ApiResponseDTO.success(categoryDTO));
        } else {
            return ResponseEntity.status(404)
                .body(ApiResponseDTO.error("Category not found", 404));
        }
    }
}
