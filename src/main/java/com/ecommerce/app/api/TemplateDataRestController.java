package com.ecommerce.app.api;

import com.ecommerce.app.model.Category;
import com.ecommerce.app.model.Product;
import com.ecommerce.app.util.TemplateDataExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for template data collection API endpoints
 */
@RestController
@RequestMapping("/api/template-data")
public class TemplateDataRestController {

    @Autowired
    private TemplateDataExtractor templateDataExtractor;
    
    /**
     * Get all template data
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllTemplateData() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> data = new HashMap<>();
            
            // Extract product and category data
            List<Product> products = templateDataExtractor.extractProductData();
            List<Category> categories = templateDataExtractor.extractCategoryData();
            
            // Add to data map
            data.put("products", products);
            data.put("categories", categories);
            
            // No actual user or order data in templates, so returning empty lists
            data.put("users", List.of());
            data.put("orders", List.of());
            
            response.put("success", true);
            response.put("data", data);
            response.put("message", "Data collected successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error collecting template data: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Export SQL statements for all collected data
     */
    @GetMapping("/sql-export")
    public ResponseEntity<Map<String, Object>> exportSqlStatements() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Run the extraction process to generate SQL files
            templateDataExtractor.extractDataFromTemplates();
            
            // Read the generated SQL files
            Map<String, String> sqlStatements = new HashMap<>();
            
            Path outputDir = Paths.get("template-data-output");
            if (Files.exists(outputDir)) {
                // Read categories SQL
                Path categoriesSqlPath = outputDir.resolve("categories.sql");
                if (Files.exists(categoriesSqlPath)) {
                    sqlStatements.put("categories", readFile(categoriesSqlPath.toString()));
                }
                
                // Read products SQL
                Path productsSqlPath = outputDir.resolve("products.sql");
                if (Files.exists(productsSqlPath)) {
                    sqlStatements.put("products", readFile(productsSqlPath.toString()));
                }
                
                // Read combined SQL
                Path combinedSqlPath = outputDir.resolve("combined_import.sql");
                if (Files.exists(combinedSqlPath)) {
                    sqlStatements.put("combined", readFile(combinedSqlPath.toString()));
                }
            }
            
            response.put("success", true);
            response.put("data", sqlStatements);
            response.put("message", "SQL export generated successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error generating SQL export: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Get only product data from templates
     */
    @GetMapping("/products")
    public ResponseEntity<Map<String, Object>> getProductData() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Product> products = templateDataExtractor.extractProductData();
            
            response.put("success", true);
            response.put("data", products);
            response.put("message", "Product data collected successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error collecting product data: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Get only category data from templates
     */
    @GetMapping("/categories")
    public ResponseEntity<Map<String, Object>> getCategoryData() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Category> categories = templateDataExtractor.extractCategoryData();
            
            response.put("success", true);
            response.put("data", categories);
            response.put("message", "Category data collected successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error collecting category data: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Helper method to read a file into a string
     */
    private String readFile(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }
}
