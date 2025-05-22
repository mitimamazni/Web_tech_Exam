package com.ecommerce.app.controller;

import com.ecommerce.app.model.Product;
import com.ecommerce.app.service.CategoryService;
import com.ecommerce.app.service.ProductService;
import com.ecommerce.app.utils.DebugUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/products")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DebugUtils debugUtils;

    @GetMapping
    public String getAllProducts(Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "name") String sort) {

        Page<Product> productPage = productService.getAllProductsPaged(
            PageRequest.of(page, size, Sort.by(sort))
        );
        
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("size", size);
        model.addAttribute("sort", sort);
        model.addAttribute("categories", categoryService.getAllCategories());
        
        return "product/list";
    }

    @GetMapping("/{id}")
    public String getProductDetail(@PathVariable Long id, Model model) {
        productService.getProductById(id).ifPresent(product -> {
            model.addAttribute("product", product);
        });
        
        return "product/detail";
    }

    @GetMapping("/category/{categoryId}")
    public String getProductsByCategory(@PathVariable Long categoryId,
                                      Model model,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "12") int size) {
        
        Page<Product> productPage = productService.getProductsByCategory(
            categoryId, PageRequest.of(page, size)
        );
        
        categoryService.getCategoryById(categoryId).ifPresent(category -> {
            model.addAttribute("category", category);
        });
        
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("size", size);
        model.addAttribute("categories", categoryService.getAllCategories());
        
        return "product/category";
    }

    @GetMapping("/search")
    public String searchProducts(@RequestParam String keyword,
                              Model model,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "12") int size) {
        
        Page<Product> productPage = productService.searchProducts(
            keyword, PageRequest.of(page, size)
        );
        
        model.addAttribute("keyword", keyword);
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("size", size);
        model.addAttribute("categories", categoryService.getAllCategories());
        
        return "product/search";
    }

    /**
     * API endpoint to get all products
     * @param page Page number (0-based)
     * @param size Items per page
     * @param sort Sort field
     * @return JSON with products data
     */
    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getProductsApi(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "name") String sort) {
        
        debugUtils.logMethodEntry(logger, "getProductsApi", page, size, sort);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Page<Product> productPage = productService.getAllProductsPaged(
                PageRequest.of(page, size, Sort.by(sort))
            );
            
            response.put("products", productPage.getContent());
            response.put("currentPage", page);
            response.put("totalPages", productPage.getTotalPages());
            response.put("totalItems", productPage.getTotalElements());
            response.put("success", true);
            
            debugUtils.logMethodExit(logger, "getProductsApi", "Found " + productPage.getContent().size() + " products");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching products: ", e);
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * API endpoint to get product details by ID
     * @param id Product ID
     * @return JSON with product data
     */
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getProductByIdApi(@PathVariable Long id) {
        debugUtils.logMethodEntry(logger, "getProductByIdApi", id);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Product product = productService.getProductById(id).orElse(null);
            
            if (product == null) {
                response.put("success", false);
                response.put("error", "Product not found");
                return ResponseEntity.notFound().build();
            }
            
            response.put("product", product);
            response.put("success", true);
            
            debugUtils.logMethodExit(logger, "getProductByIdApi", "Found product: " + product.getName());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching product with ID {}: ", id, e);
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * API endpoint to get products by category ID
     * @param categoryId Category ID
     * @param page Page number (0-based)
     * @param size Items per page
     * @return JSON with products data
     */
    @GetMapping("/api/category/{categoryId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getProductsByCategoryApi(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        
        debugUtils.logMethodEntry(logger, "getProductsByCategoryApi", categoryId, page, size);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Page<Product> productPage = productService.getProductsByCategory(
                categoryId, PageRequest.of(page, size));
            
            response.put("products", productPage.getContent());
            response.put("currentPage", page);
            response.put("totalPages", productPage.getTotalPages());
            response.put("totalItems", productPage.getTotalElements());
            response.put("success", true);
            response.put("categoryId", categoryId);
            response.put("categoryName", categoryService.getCategoryById(categoryId)
                .map(c -> c.getName()).orElse("Unknown"));
            
            debugUtils.logMethodExit(logger, "getProductsByCategoryApi", 
                "Found " + productPage.getContent().size() + " products in category " + categoryId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching products for category {}: ", categoryId, e);
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}