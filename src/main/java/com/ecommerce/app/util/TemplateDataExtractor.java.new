package com.ecommerce.app.util;

import com.ecommerce.app.dto.ProductDTO;
import com.ecommerce.app.dto.CategoryDTO;
import com.ecommerce.app.model.*;
import com.ecommerce.app.repository.*;
import com.ecommerce.app.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class to collect template data and insert it into the database
 * This can be run via a command line argument when starting the application
 */
@Component
public class TemplateDataExtractor {

    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private ProductImageRepository productImageRepository;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private CategoryService categoryService;
    
    /**
     * Command line runner that executes when the "template-data-extract" profile is active
     * Use with: java -jar app.jar --spring.profiles.active=template-data-extract
     */
    @Bean
    @Profile("template-data-extract")
    public CommandLineRunner templateDataExtractor(ApplicationContext ctx) {
        return args -> {
            System.out.println("Starting template data extraction...");
            extractDataFromTemplates();
            System.out.println("Template data extraction completed. Application will now exit.");
            
            // Exit the application when done
            System.exit(0);
        };
    }
    
    /**
     * Main method to extract data from templates
     */
    public void extractDataFromTemplates() {
        try {
            // Create output directory if it doesn't exist
            Path outputDir = Paths.get("template-data-output");
            if (!Files.exists(outputDir)) {
                Files.createDirectories(outputDir);
            }
            
            // Extract and store category data
            List<Category> categories = extractCategoryData();
            generateCategorySql(categories, outputDir.resolve("categories.sql").toString());
            
            // Extract and store product data
            List<Product> products = extractProductData();
            generateProductSql(products, outputDir.resolve("products.sql").toString());
            
            // Generate combined SQL file
            generateCombinedSql(categories, products, outputDir.resolve("combined.sql").toString());
            
            System.out.println("Successfully extracted template data and generated SQL files in: " + outputDir.toAbsolutePath());
        } catch (Exception e) {
            System.err.println("Error extracting template data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Extract category data from templates
     */
    public List<Category> extractCategoryData() {
        System.out.println("Extracting category data...");
        
        // Sample categories from template exploration
        List<Category> categories = new ArrayList<>();
        
        // These categories were found in the templates
        addCategory(categories, "Coffee Beans", "Various coffee beans from around the world");
        addCategory(categories, "Brewing Equipment", "Equipment for brewing perfect coffee");
        addCategory(categories, "Accessories", "Coffee accessories and add-ons");
        addCategory(categories, "Gift Sets", "Curated gift sets for coffee lovers");
        
        System.out.println("Extracted " + categories.size() + " categories");
        return categories;
    }
    
    /**
     * Helper method to add a category
     */
    private void addCategory(List<Category> categories, String name, String description) {
        Category category = new Category();
        category.setId((long) (categories.size() + 1));
        category.setName(name);
        category.setDescription(description);
        categories.add(category);
    }
    
    /**
     * Extract product data from templates
     */
    public List<Product> extractProductData() {
        System.out.println("Extracting product data...");
        
        // Products from template exploration
        List<Product> products = new ArrayList<>();
        Map<String, Category> categoryMap = new HashMap<>();
        
        // Create category map for lookup
        categoryMap.put("Coffee Beans", createCategory(1L, "Coffee Beans", "Various coffee beans from around the world"));
        categoryMap.put("Brewing Equipment", createCategory(2L, "Brewing Equipment", "Equipment for brewing perfect coffee"));
        categoryMap.put("Accessories", createCategory(3L, "Accessories", "Coffee accessories and add-ons"));
        categoryMap.put("Gift Sets", createCategory(4L, "Gift Sets", "Curated gift sets for coffee lovers"));
        
        // Products found in templates - all dummy data from the html templates
        addProduct(products, 1L, "Ethiopian Yirgacheffe", 
            "Bright, fruity, and floral notes with a clean, tea-like body. Our Ethiopian Yirgacheffe is a perfect morning coffee.",
            new BigDecimal("16.99"), new BigDecimal("19.99"), 100, false, categoryMap.get("Coffee Beans"),
            "https://images.unsplash.com/photo-1559525839-75acb0527358?w=500&auto=format&fit=crop");
            
        addProduct(products, 2L, "Colombian Supremo", 
            "Sweet caramel and nutty notes with a medium body and smooth finish. A classic coffee loved by everyone.",
            new BigDecimal("15.99"), null, 120, false, categoryMap.get("Coffee Beans"),
            "https://images.unsplash.com/photo-1498804103079-a6351b050096?w=500&auto=format&fit=crop");
            
        addProduct(products, 3L, "Hario V60 Pour Over Kit", 
            "Complete kit includes ceramic dripper, glass server, measuring scoop, and filters for the perfect pour over coffee.",
            new BigDecimal("29.99"), null, 30, false, categoryMap.get("Brewing Equipment"),
            "https://images.unsplash.com/photo-1572286258217-215cf8e9d99a?w=500&auto=format&fit=crop");
            
        addProduct(products, 4L, "Aeropress Coffee Maker", 
            "The versatile and portable coffee maker that brews American, espresso, or cold brew style coffee in about a minute.",
            new BigDecimal("34.99"), null, 0, false, categoryMap.get("Brewing Equipment"),
            "https://images.unsplash.com/photo-1585151045903-348af8b0fe15?w=500&auto=format&fit=crop");
            
        addProduct(products, 5L, "Ceramic Coffee Mug Set (4 pcs)", 
            "Set of 4 handcrafted ceramic mugs in earthy tones, perfect for your morning coffee ritual. Dishwasher safe.",
            new BigDecimal("32.99"), new BigDecimal("39.99"), 50, false, categoryMap.get("Accessories"),
            "https://images.unsplash.com/photo-1564913489179-5c9a140b97b2?w=500&auto=format&fit=crop");
            
        addProduct(products, 6L, "Electric Burr Coffee Grinder", 
            "Conical burr grinder with 18 grind settings for perfectly ground coffee every time. Ideal for any brewing method.",
            new BigDecimal("49.99"), new BigDecimal("64.99"), 25, false, categoryMap.get("Brewing Equipment"),
            "https://images.unsplash.com/photo-1534687941688-651ccaafbff8?w=500&auto=format&fit=crop");
            
        addProduct(products, 7L, "Guatemala Antigua", 
            "Rich, chocolate notes with a subtle spice and smooth finish. Medium roast from the highlands of Guatemala.",
            new BigDecimal("18.99"), null, 80, false, categoryMap.get("Coffee Beans"),
            "https://images.unsplash.com/photo-1490368183761-4ec96bf47292?w=500&auto=format&fit=crop");
            
        addProduct(products, 8L, "Coffee Lover's Gift Set", 
            "Complete gift set with two 8oz bags of premium coffee, a ceramic mug, and a stainless steel travel tumbler.",
            new BigDecimal("59.99"), null, 15, false, categoryMap.get("Gift Sets"),
            "https://images.unsplash.com/photo-1577968897966-3d4325b36b61?w=500&auto=format&fit=crop");
            
        // Additional products found in template data
        addProduct(products, 9L, "Monthly Coffee Subscription", 
            "Explore the world of coffee with our curated subscription. Receive three unique, freshly roasted coffees each month.",
            new BigDecimal("39.99"), null, 999, true, categoryMap.get("Coffee Beans"),
            "https://images.unsplash.com/photo-1580933073521-dc49ac0d4e6a?w=500&auto=format&fit=crop");
            
        addProduct(products, 10L, "Chemex Pour-Over Glass Coffee Maker", 
            "The timeless, elegant Chemex brewer creates a clean, delicious cup of pour-over coffee without bitterness.",
            new BigDecimal("44.99"), new BigDecimal("49.99"), 20, false, categoryMap.get("Brewing Equipment"),
            "https://images.unsplash.com/photo-1560031958-a0d21ae60205?w=500&auto=format&fit=crop");
        
        System.out.println("Extracted " + products.size() + " products");
        return products;
    }
    
    /**
     * Helper method to create a category
     */
    private Category createCategory(Long id, String name, String description) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        category.setDescription(description);
        return category;
    }
    
    /**
     * Helper method to add a product
     */
    private void addProduct(List<Product> products, Long id, String name, String description, 
                           BigDecimal price, BigDecimal salePrice, Integer stockQuantity, 
                           Boolean isSubscription, Category category, String imageUrl) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setSalePrice(salePrice);
        product.setStockQuantity(stockQuantity);
        product.setIsSubscription(isSubscription);
        product.setCategory(category);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        product.setActive(true);
        
        // Add image if provided
        if (imageUrl != null) {
            ProductImage image = new ProductImage();
            image.setId(id); // Simple ID for demo
            image.setProduct(product);
            image.setImageUrl(imageUrl);
            image.setIsPrimary(true);
            image.setDisplayOrder(0);
            
            List<ProductImage> images = new ArrayList<>();
            images.add(image);
            product.setImages(images);
        }
        
        products.add(product);
    }
    
    /**
     * Generate SQL for category data
     */
    private void generateCategorySql(List<Category> categories, String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("-- Category SQL Inserts generated from template data\n");
            writer.write("-- Generated on: " + LocalDateTime.now() + "\n\n");
            
            writer.write("-- Clear existing data\n");
            writer.write("DELETE FROM categories;\n\n");
            
            writer.write("-- Insert categories\n");
            for (Category category : categories) {
                writer.write(String.format(
                    "INSERT INTO categories (id, name, description) VALUES (%d, '%s', '%s');\n",
                    category.getId(),
                    escapeSQL(category.getName()),
                    escapeSQL(category.getDescription())
                ));
            }
        }
    }
    
    /**
     * Generate SQL for product data
     */
    private void generateProductSql(List<Product> products, String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("-- Product SQL Inserts generated from template data\n");
            writer.write("-- Generated on: " + LocalDateTime.now() + "\n\n");
            
            writer.write("-- Clear existing data\n");
            writer.write("DELETE FROM product_images;\n");
            writer.write("DELETE FROM products;\n\n");
            
            writer.write("-- Insert products\n");
            for (Product product : products) {
                writer.write(String.format(
                    "INSERT INTO products (id, name, description, price, sale_price, stock_quantity, is_subscription, category_id, created_at, updated_at, active) " +
                    "VALUES (%d, '%s', '%s', %s, %s, %d, %b, %s, NOW(), NOW(), %b);\n",
                    product.getId(),
                    escapeSQL(product.getName()),
                    escapeSQL(product.getDescription()),
                    product.getPrice(),
                    product.getSalePrice() != null ? product.getSalePrice() : "NULL",
                    product.getStockQuantity() != null ? product.getStockQuantity() : 0,
                    product.getIsSubscription() != null ? product.getIsSubscription() : false,
                    product.getCategory() != null ? product.getCategory().getId() : "NULL",
                    product.isActive()
                ));
                
                // Add product images if any
                if (product.getImages() != null && !product.getImages().isEmpty()) {
                    for (ProductImage image : product.getImages()) {
                        writer.write(String.format(
                            "INSERT INTO product_images (id, product_id, image_url, is_primary, display_order) " +
                            "VALUES (%d, %d, '%s', %b, %d);\n",
                            image.getId(),
                            product.getId(),
                            escapeSQL(image.getImageUrl()),
                            image.getIsPrimary() != null ? image.getIsPrimary() : false,
                            image.getDisplayOrder() != null ? image.getDisplayOrder() : 0
                        ));
                    }
                }
            }
        }
    }
    
    /**
     * Generate a combined SQL file with all data
     */
    private void generateCombinedSql(List<Category> categories, List<Product> products, String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("-- Combined SQL Inserts generated from template data\n");
            writer.write("-- Generated on: " + LocalDateTime.now() + "\n\n");
            
            writer.write("-- Clear existing data\n");
            writer.write("DELETE FROM product_images;\n");
            writer.write("DELETE FROM products;\n");
            writer.write("DELETE FROM categories;\n\n");
            
            writer.write("-- Insert categories\n");
            for (Category category : categories) {
                writer.write(String.format(
                    "INSERT INTO categories (id, name, description) VALUES (%d, '%s', '%s');\n",
                    category.getId(),
                    escapeSQL(category.getName()),
                    escapeSQL(category.getDescription())
                ));
            }
            
            writer.write("\n-- Insert products\n");
            for (Product product : products) {
                writer.write(String.format(
                    "INSERT INTO products (id, name, description, price, sale_price, stock_quantity, is_subscription, category_id, created_at, updated_at, active) " +
                    "VALUES (%d, '%s', '%s', %s, %s, %d, %b, %s, NOW(), NOW(), %b);\n",
                    product.getId(),
                    escapeSQL(product.getName()),
                    escapeSQL(product.getDescription()),
                    product.getPrice(),
                    product.getSalePrice() != null ? product.getSalePrice() : "NULL",
                    product.getStockQuantity() != null ? product.getStockQuantity() : 0,
                    product.getIsSubscription() != null ? product.getIsSubscription() : false,
                    product.getCategory() != null ? product.getCategory().getId() : "NULL",
                    product.isActive()
                ));
            }
            
            writer.write("\n-- Insert product images\n");
            for (Product product : products) {
                if (product.getImages() != null && !product.getImages().isEmpty()) {
                    for (ProductImage image : product.getImages()) {
                        writer.write(String.format(
                            "INSERT INTO product_images (id, product_id, image_url, is_primary, display_order) " +
                            "VALUES (%d, %d, '%s', %b, %d);\n",
                            image.getId(),
                            product.getId(),
                            escapeSQL(image.getImageUrl()),
                            image.getIsPrimary() != null ? image.getIsPrimary() : false,
                            image.getDisplayOrder() != null ? image.getDisplayOrder() : 0
                        ));
                    }
                }
            }
        }
    }
    
    /**
     * Helper method to escape SQL string values
     */
    private String escapeSQL(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("'", "''");
    }
}
