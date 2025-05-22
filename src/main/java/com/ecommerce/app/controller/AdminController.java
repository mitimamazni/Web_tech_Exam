package com.ecommerce.app.controller;

import com.ecommerce.app.exception.AccessDeniedException;
import com.ecommerce.app.model.*;
import com.ecommerce.app.service.CategoryService;
import com.ecommerce.app.service.OrderService;
import com.ecommerce.app.service.ProductService;
import com.ecommerce.app.service.UserService;
import java.util.ArrayList;
import java.util.HashSet;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    // Add admin check method
    private boolean isAdmin(HttpSession session) {
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        return isAdmin != null && isAdmin;
    }
    
    // Admin authorization check for handlers
    private void verifyAdminAccess(HttpSession session) {
        if (!isAdmin(session)) {
            throw new AccessDeniedException("You need admin privileges to access this page");
        }
    }
    
    // Add model attributes common to all admin pages
    @ModelAttribute
    public void addCommonAttributes(Model model) {
        // Add any common attributes needed across all admin pages
        model.addAttribute("currentYear", java.time.Year.now().getValue());
    }

    // Dashboard
    @GetMapping
    public String dashboard(Model model, HttpSession session) {
        verifyAdminAccess(session);
        
        List<Product> products = productService.getAllProducts();
        List<Category> categories = categoryService.getAllCategories();
        List<Order> orders = orderService.getAllOrders();
        List<User> users = userService.findAllUsers();
        
        model.addAttribute("productCount", products.size());
        model.addAttribute("categoryCount", categories.size());
        model.addAttribute("orderCount", orders.size());
        model.addAttribute("userCount", users.size());
        
        // Add recent orders (up to 5)
        int ordersToShow = Math.min(5, orders.size());
        model.addAttribute("recentOrders", orders.subList(0, ordersToShow));
        
        return "admin/dashboard";
    }
    
    // Analytics & Reports
    @GetMapping("/analytics")
    public String analytics(Model model, HttpSession session) {
        try {
            verifyAdminAccess(session);
            
            // Calculate real revenue stats
            List<Order> allOrders = orderService.getAllOrders();
            
            // Get orders from last 30 days
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            List<Order> recentOrders = orderService.getOrdersByDateRange(thirtyDaysAgo, LocalDateTime.now());
            
            // Get orders from previous 30 days
            LocalDateTime sixtyDaysAgo = LocalDateTime.now().minusDays(60);
            List<Order> previousPeriodOrders = orderService.getOrdersByDateRange(sixtyDaysAgo, thirtyDaysAgo);
            
            // Calculate total revenue for current period
            double totalRevenue = recentOrders.stream()
                .mapToDouble(order -> order.getTotalAmount().doubleValue())
                .sum();
            
            // Calculate total revenue for previous period
            double previousRevenue = previousPeriodOrders.stream()
                .mapToDouble(order -> order.getTotalAmount().doubleValue())
                .sum();
            
            // Calculate revenue change as percentage
            double revenueChange = 0.0;
            if (previousRevenue > 0) {
                revenueChange = ((totalRevenue - previousRevenue) / previousRevenue) * 100;
            }
            
            // Format total revenue
            String formattedRevenue = String.format("%,.2f", totalRevenue);
            
            // Order stats
            int orderCount = recentOrders.size();
            int previousOrderCount = previousPeriodOrders.size();
            
            double orderChange = 0.0;
            if (previousOrderCount > 0) {
                orderChange = ((double)(orderCount - previousOrderCount) / previousOrderCount) * 100;
            }
            
            // Average order value
            double avgOrderValue = orderCount > 0 ? totalRevenue / orderCount : 0;
            double prevAvgOrderValue = previousOrderCount > 0 ? previousRevenue / previousOrderCount : 0;
            
            double aovChange = 0.0;
            if (prevAvgOrderValue > 0) {
                aovChange = ((avgOrderValue - prevAvgOrderValue) / prevAvgOrderValue) * 100;
            }
            
            String formattedAOV = String.format("$%,.2f", avgOrderValue);
            
            // Conversion rate (would be calculated with visitor data in a real app)
            // Using placeholder values since we don't have visitor data
            double conversionRate = 3.2;
            double conversionChange = -0.5;
            
            // Calculate top products manually based on order data
            Map<Product, Integer> productOrderCounts = new HashMap<>();
            // Collect all order items from recent orders
            for (Order order : allOrders) {
                if (order.getOrderItems() != null) {
                    for (OrderItem item : order.getOrderItems()) {
                        Product product = item.getProduct();
                        int currentCount = productOrderCounts.getOrDefault(product, 0);
                        productOrderCounts.put(product, currentCount + item.getQuantity());
                    }
                }
            }
            
            // Sort by most ordered and take top 5
            List<Map.Entry<Product, Integer>> sortedProducts = productOrderCounts.entrySet().stream()
                .sorted(Map.Entry.<Product, Integer>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toList());
                
            List<Map<String, Object>> topProducts = new ArrayList<>();
            
            for (Map.Entry<Product, Integer> entry : sortedProducts) {
                Product product = entry.getKey();
                Integer unitsSold = entry.getValue();
                double revenue = unitsSold * product.getPrice().doubleValue();
                topProducts.add(createProductStat(
                    product.getId(), 
                    product.getName(), 
                    unitsSold, 
                    String.format("%,.2f", revenue)
                ));
            }
            
            // Add chart data - monthly sales for the last 6 months
            List<String> monthLabels = new ArrayList<>();
            List<Double> monthlyRevenueData = new ArrayList<>();
            
            for (int i = 5; i >= 0; i--) {
                java.time.YearMonth yearMonth = java.time.YearMonth.now().minusMonths(i);
                String monthName = yearMonth.getMonth().toString().substring(0, 3);
                monthLabels.add(monthName);
                
                // Calculate revenue for the month manually
                LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
                LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);
                
                double monthRevenue = orderService.getOrdersByDateRange(startOfMonth, endOfMonth).stream()
                    .mapToDouble(order -> order.getTotalAmount().doubleValue())
                    .sum();
                monthlyRevenueData.add(monthRevenue);
            }
            
            // Add categorical data - product categories
            List<Category> categories = categoryService.getAllCategories();
            List<String> categoryLabels = new ArrayList<>();
            List<Integer> categoryData = new ArrayList<>();
            
            for (Category category : categories) {
                categoryLabels.add(category.getName());
                int productCount = productService.countProductsByCategory(category.getId());
                categoryData.add(productCount);
            }
            
            // Add weekly order data
            List<String> weekdayLabels = List.of("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun");
            
            // Calculate orders by day of week manually
            List<Integer> ordersByDay = new ArrayList<>();
            for (int day = 1; day <= 7; day++) {
                final int dayOfWeek = day;
                long count = allOrders.stream()
                    .filter(order -> order.getCreatedAt().getDayOfWeek().getValue() == dayOfWeek)
                    .count();
                ordersByDay.add((int) count);
            }
            
            model.addAttribute("totalRevenue", formattedRevenue);
            model.addAttribute("revenueChange", revenueChange);
            model.addAttribute("orderCount", orderCount);
            model.addAttribute("orderChange", orderChange);
            model.addAttribute("averageOrderValue", formattedAOV);
            model.addAttribute("aovChange", aovChange);
            model.addAttribute("conversionRate", conversionRate);
            model.addAttribute("conversionChange", conversionChange);
            model.addAttribute("topProducts", topProducts);
            
            model.addAttribute("salesLabels", monthLabels);
            model.addAttribute("salesData", monthlyRevenueData);
            model.addAttribute("categoryLabels", categoryLabels);
            model.addAttribute("categoryData", categoryData);
            model.addAttribute("trafficLabels", weekdayLabels);
            model.addAttribute("trafficData", ordersByDay);
            
            // Add services to the model for template usage
            model.addAttribute("productService", productService);
            model.addAttribute("categoryService", categoryService);
            model.addAttribute("orderService", orderService);
            
            return "admin/analytics";
        } catch (Exception e) {
            // Log the exception
            System.err.println("Error in analytics: " + e.getMessage());
            e.printStackTrace();
            
            // Add error message to the model
            model.addAttribute("error", "An error occurred while loading analytics data: " + e.getMessage());
            return "error/general";
        }
    }
    
    // Helper method for creating product stats
    private Map<String, Object> createProductStat(Long id, String name, int unitsSold, String revenue) {
        Map<String, Object> product = new HashMap<>();
        product.put("id", id);
        product.put("name", name);
        product.put("unitsSold", unitsSold);
        product.put("revenue", revenue);
        return product;
    }
    
    // Handle report generation requests
    @PostMapping("/analytics/reports/{reportType}")
    public ResponseEntity<byte[]> generateReport(@PathVariable String reportType, 
                                              @RequestParam Map<String, String> params,
                                              HttpSession session) {
        try {
            verifyAdminAccess(session);
        } catch (AccessDeniedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        String reportContent = "";
        String filename = "";
        
        switch (reportType) {
            case "sales":
                reportContent = generateSalesReportData(params);
                filename = "sales-report.csv";
                break;
            case "products":
                reportContent = generateProductsReportData(params);
                filename = "products-report.csv";
                break;
            case "users":
                reportContent = generateUsersReportData(params);
                filename = "users-report.csv";
                break;
            case "orders":
                reportContent = generateOrdersReportData(params);
                filename = "orders-report.csv";
                break;
            default:
                return ResponseEntity.badRequest().build();
        }
        
        // Set up response headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", filename);
        
        return ResponseEntity
                .ok()
                .headers(headers)
                .body(reportContent.getBytes(StandardCharsets.UTF_8));
    }
    
    // Helper methods for generating real report data
    private String generateSalesReportData(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        sb.append("Date,Order ID,Product,Quantity,Unit Price,Total\n");
        
        // Get date range from parameters
        int days = Integer.parseInt(params.getOrDefault("dateRange", "30"));
        
        // Get orders within date range
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        List<Order> orders = orderService.getOrdersByDateRange(startDate, LocalDateTime.now());
        
        // Process orders and include their items
        for (Order order : orders) {
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String orderDate = order.getCreatedAt().format(formatter);
            
            for (OrderItem item : order.getOrderItems()) {
                Product product = item.getProduct();
                Double unitPrice = product.getPrice().doubleValue();
                Double total = unitPrice * item.getQuantity();
                
                sb.append(String.format("%s,%d,\"%s\",%d,%.2f,%.2f\n",
                    orderDate,
                    order.getId(),
                    product.getName().replace("\"", "\"\""), // Escape quotes in CSV
                    item.getQuantity(),
                    unitPrice,
                    total
                ));
            }
        }
        
        return sb.toString();
    }
    
    private String generateProductsReportData(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        sb.append("Product ID,Name,Category,Price,Sale Price,Stock,Status,Created Date,Updated Date\n");
        
        // Filter based on parameters
        boolean includeInactive = "on".equals(params.get("includeInactive"));
        
        // Get all products (we don't have a separate method for inactive products)
        List<Product> products = productService.getAllProducts();
        
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        // Process products
        for (Product product : products) {
            String categoryName = product.getCategory() != null ? product.getCategory().getName() : "Uncategorized";
            String status = product.isActive() ? "Active" : "Inactive";
            String createdDate = product.getCreatedAt() != null ? product.getCreatedAt().format(formatter) : "";
            String updatedDate = product.getUpdatedAt() != null ? product.getUpdatedAt().format(formatter) : "";
            String salePriceStr = product.getSalePrice() != null ? product.getSalePrice().toString() : "";
            
            sb.append(String.format("%d,\"%s\",\"%s\",%.2f,%s,%d,%s,%s,%s\n",
                product.getId(),
                product.getName().replace("\"", "\"\""), // Escape quotes in CSV
                categoryName.replace("\"", "\"\""), // Escape quotes in CSV
                product.getPrice().doubleValue(),
                salePriceStr,
                product.getStockQuantity() != null ? product.getStockQuantity() : 0,
                status,
                createdDate,
                updatedDate
            ));
        }
        
        return sb.toString();
    }
    
    private String generateOrdersReportData(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        sb.append("Order ID,Date,Customer,Status,Items,Total,Payment Method\n");
        
        // Get date range from parameters
        int days = Integer.parseInt(params.getOrDefault("dateRange", "30"));
        
        // Get orders within date range
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        List<Order> orders = orderService.getOrdersByDateRange(startDate, LocalDateTime.now());
        
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        // Process orders
        for (Order order : orders) {
            String orderDate = order.getCreatedAt().format(formatter);
            User user = order.getUser();
            String customerName = user != null ? user.getFirstName() + " " + user.getLastName() : "Guest";
            
            sb.append(String.format("%d,%s,\"%s\",%s,%d,%.2f,%s\n",
                order.getId(),
                orderDate,
                customerName.replace("\"", "\"\""), // Escape quotes
                order.getStatus(),
                order.getOrderItems().size(),
                order.getTotalAmount().doubleValue(),
                order.getPaymentMethod()
            ));
        }
        
        return sb.toString();
    }
    
    private String generateUsersReportData(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        sb.append("User ID,Username,Name,Email,Phone,Registered Date,Orders,Total Spent\n");
        
        // Get all users
        List<User> users = userService.findAllUsers();
        
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        // Process users
        for (User user : users) {
            String registeredDate = user.getCreatedAt() != null ? user.getCreatedAt().format(formatter) : "";
            String fullName = (user.getFirstName() != null ? user.getFirstName() : "") + " " + 
                             (user.getLastName() != null ? user.getLastName() : "");
            fullName = fullName.trim();
            
            // Get user's orders
            List<Order> userOrders = orderService.getOrdersByUser(user);
            double totalSpent = userOrders.stream()
                .mapToDouble(order -> order.getTotalAmount().doubleValue())
                .sum();
            
            sb.append(String.format("%d,\"%s\",\"%s\",\"%s\",\"%s\",%s,%d,%.2f\n",
                user.getId(),
                user.getUsername() != null ? user.getUsername().replace("\"", "\"\"") : "", // Escape quotes
                fullName.replace("\"", "\"\""), // Escape quotes
                user.getEmail() != null ? user.getEmail().replace("\"", "\"\"") : "", // Escape quotes 
                user.getPhoneNumber() != null ? user.getPhoneNumber().replace("\"", "\"\"") : "", // Escape quotes
                registeredDate,
                userOrders.size(),
                totalSpent
            ));
        }
        
        return sb.toString();
    }
    
    // Product Management
    @GetMapping("/products")
    public String productList(Model model, 
                            @RequestParam(defaultValue = "0") int page,
                            HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/";
        }
        
        Page<Product> productPage = productService.getAllProductsPaged(
            PageRequest.of(page, 10));
        
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        
        return "admin/product/list";
    }

    @GetMapping("/products/{id}")
    public String productDetail(@PathVariable Long id, Model model, HttpSession session) {
        try {
            verifyAdminAccess(session);
            
            Product product = productService.getProductById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            
            // Add safety checks for potential null references
            if (product.getImages() == null) {
                product.setImages(new ArrayList<>());
            }
            
            if (product.getTags() == null) {
                product.setTags(new HashSet<>());
            }
            
            if (product.getReviews() == null) {
                product.setReviews(new ArrayList<>());
            }
            
            model.addAttribute("product", product);
            
            return "admin/product/detail";
        } catch (Exception e) {
            // Log the error
            System.err.println("Error in product detail: " + e.getMessage());
            e.printStackTrace();
            
            // Add error message to the model
            model.addAttribute("error", "Error loading product: " + e.getMessage());
            return "error/general";
        }
    }
    
    @GetMapping("/products/new")
    public String newProductForm(Model model, HttpSession session) {
        verifyAdminAccess(session);
        
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("tags", productService.getAllTags());
        
        return "admin/product/form";
    }
    
    @GetMapping("/products/{id}/edit")
    public String editProductForm(@PathVariable Long id, Model model, HttpSession session) {
        verifyAdminAccess(session);
        
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("tags", productService.getAllTags());
        
        return "admin/product/form";
    }
    
    @PostMapping("/products/create")
    public String createProduct(@Valid Product product, 
                              BindingResult result,
                              @RequestParam(required = false) Long categoryId,
                              @RequestParam(required = false) List<Long> tagIds,
                              @RequestParam(required = false) List<MultipartFile> imageFiles,
                              Model model,
                              RedirectAttributes redirectAttributes,
                              HttpSession session) {
        verifyAdminAccess(session);
        
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("tags", productService.getAllTags());
            return "admin/product/form";
        }
        
        try {
            // Save the product first to get an ID
            Product savedProduct = productService.saveProduct(product);
            
            // Set category if provided
            if (categoryId != null) {
                categoryService.getCategoryById(categoryId).ifPresent(category -> {
                    savedProduct.setCategory(category);
                    productService.saveProduct(savedProduct);
                });
            }
            
            // Set tags if provided
            if (tagIds != null && !tagIds.isEmpty()) {
                productService.setProductTags(savedProduct, tagIds);
            }
            
            // Handle image uploads if any
            if (imageFiles != null && !imageFiles.isEmpty()) {
                productService.addProductImages(savedProduct, imageFiles, true); // First image as primary
            }
            
            redirectAttributes.addFlashAttribute("success", "Product created successfully");
            return "redirect:/admin/products/" + savedProduct.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create product: " + e.getMessage());
            return "redirect:/admin/products/new";
        }
    }
    
    @PostMapping("/products/{id}/update")
    public String updateProduct(@PathVariable Long id,
                              @Valid Product product, 
                              BindingResult result,
                              @RequestParam(required = false) Long categoryId,
                              @RequestParam(required = false) List<Long> tagIds,
                              @RequestParam(required = false) List<MultipartFile> imageFiles,
                              @RequestParam(required = false) List<Long> deleteImageIds,
                              @RequestParam(required = false) Long primaryImageId,
                              Model model,
                              RedirectAttributes redirectAttributes,
                              HttpSession session) {
        verifyAdminAccess(session);
        
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("tags", productService.getAllTags());
            return "admin/product/form";
        }
        
        try {
            // Get existing product
            Product existingProduct = productService.getProductById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            
            // Update basic properties
            existingProduct.setName(product.getName());
            existingProduct.setDescription(product.getDescription());
            existingProduct.setPrice(product.getPrice());
            existingProduct.setSalePrice(product.getSalePrice());
            existingProduct.setStockQuantity(product.getStockQuantity());
            existingProduct.setActive(product.isActive());
            existingProduct.setIsSubscription(product.getIsSubscription());
            
            // Save the updated product
            Product savedProduct = productService.saveProduct(existingProduct);
            
            // Update category if provided
            if (categoryId != null) {
                categoryService.getCategoryById(categoryId).ifPresent(category -> {
                    savedProduct.setCategory(category);
                    productService.saveProduct(savedProduct);
                });
            } else {
                // Remove category if none selected
                savedProduct.setCategory(null);
                productService.saveProduct(savedProduct);
            }
            
            // Update tags if provided
            productService.setProductTags(savedProduct, tagIds != null ? tagIds : List.of());
            
            // Delete images if requested
            if (deleteImageIds != null && !deleteImageIds.isEmpty()) {
                productService.deleteProductImages(savedProduct, deleteImageIds);
            }
            
            // Set primary image if requested
            if (primaryImageId != null) {
                productService.setPrimaryProductImage(savedProduct, primaryImageId);
            }
            
            // Add new images if uploaded
            if (imageFiles != null && !imageFiles.stream().allMatch(MultipartFile::isEmpty)) {
                boolean setFirstAsPrimary = primaryImageId == null && 
                                           (savedProduct.getImages().isEmpty() || 
                                            savedProduct.getImages().stream().noneMatch(ProductImage::getIsPrimary));
                productService.addProductImages(savedProduct, imageFiles, setFirstAsPrimary);
            }
            
            redirectAttributes.addFlashAttribute("success", "Product updated successfully");
            return "redirect:/admin/products/" + savedProduct.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update product: " + e.getMessage());
            return "redirect:/admin/products/" + id + "/edit";
        }
    }
    
    @PostMapping("/products/{id}/toggle")
    public String toggleProductStatus(@PathVariable Long id,
                                    RedirectAttributes redirectAttributes,
                                    HttpSession session) {
        verifyAdminAccess(session);
        
        try {
            Product product = productService.getProductById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            
            // Toggle active status
            product.setActive(!product.isActive());
            productService.saveProduct(product);
            
            redirectAttributes.addFlashAttribute("success", 
                "Product " + (product.isActive() ? "activated" : "deactivated") + " successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to toggle product status: " + e.getMessage());
        }
        
        return "redirect:/admin/products";
    }
    
    @PostMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id, 
                                RedirectAttributes redirectAttributes,
                                HttpSession session) {
        verifyAdminAccess(session);
        
        productService.deleteProduct(id);
        redirectAttributes.addFlashAttribute("success", "Product deleted successfully");
        
        return "redirect:/admin/products";
    }

    // User Management
    @GetMapping("/users")
    public String userList(Model model, 
                        @RequestParam(defaultValue = "0") int page,
                        HttpSession session) {
        verifyAdminAccess(session);
        
        Page<User> userPage = userService.getAllUsersPaged(
            PageRequest.of(page, 10));
        
        model.addAttribute("users", userPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", userPage.getTotalPages());
        
        return "admin/user/list";
    }
    
    @GetMapping("/users/{id}")
    public String userDetail(@PathVariable Long id, Model model, HttpSession session) {
        try {
            verifyAdminAccess(session);
            
            User user = userService.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Get user orders
            List<Order> userOrders = orderService.getOrdersByUser(user);
            
            model.addAttribute("user", user);
            model.addAttribute("userOrders", userOrders);
            
            return "admin/user/detail";
        } catch (Exception e) {
            // Log the exception
            System.err.println("Error in user detail: " + e.getMessage());
            e.printStackTrace();
            
            // Add error message to the model
            model.addAttribute("error", "Error loading user: " + e.getMessage());
            return "error/general";
        }
    }
    
    @PostMapping("/users/{id}/role")
    public String updateUserRole(@PathVariable Long id, 
                               @RequestParam("role") String role,
                               RedirectAttributes redirectAttributes,
                               HttpSession session) {
        verifyAdminAccess(session);
        
        try {
            userService.updateUserRole(id, role);
            redirectAttributes.addFlashAttribute("success", "User role updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update user role: " + e.getMessage());
        }
        
        return "redirect:/admin/users/" + id;
    }
    
    // Order Management with extended functionality
    @GetMapping("/orders")
    public String orderList(Model model, 
                          @RequestParam(defaultValue = "0") int page,
                          HttpSession session) {
        verifyAdminAccess(session);
        
        Page<Order> orderPage = orderService.getAllOrdersPaged(
            PageRequest.of(page, 10));
        
        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("orderStatuses", OrderStatus.values());
        
        return "admin/order/list";
    }
    
    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable Long id, Model model, HttpSession session) {
        verifyAdminAccess(session);
        
        Order order = orderService.getOrderById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        model.addAttribute("order", order);
        model.addAttribute("statusOptions", OrderStatus.values());
        
        return "admin/order/detail";
    }
    
    @PostMapping("/orders/{id}/status")
    public String updateOrderStatus(@PathVariable Long id, 
                                  @RequestParam("status") OrderStatus status,
                                  RedirectAttributes redirectAttributes,
                                  HttpSession session) {
        try {
            verifyAdminAccess(session);
            
            // First check if the order exists
            Order order = orderService.getOrderById(id)
                    .orElseThrow(() -> new RuntimeException("Order not found"));
                    
            // Then update the status
            orderService.updateOrderStatus(id, status);
            redirectAttributes.addFlashAttribute("success", "Order status updated successfully from " 
                + order.getStatus() + " to " + status);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update order status: " + e.getMessage());
        }
        
        return "redirect:/admin/orders/" + id;
    }
    
    // Handle admin access denied exceptions
    @ExceptionHandler(AccessDeniedException.class)
    public ModelAndView handleAdminAccessDenied(AccessDeniedException ex, RedirectAttributes redirectAttributes) {
        ModelAndView modelAndView = new ModelAndView("redirect:/login");
        redirectAttributes.addFlashAttribute("error", "You need admin privileges to access this page");
        return modelAndView;
    }
}
