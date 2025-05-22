package com.ecommerce.app.controller;

import com.ecommerce.app.exception.AccessDeniedException;
import com.ecommerce.app.model.Category;
import com.ecommerce.app.model.Tag;
import com.ecommerce.app.service.CategoryService;
import com.ecommerce.app.service.ProductService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;
    
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
        // Add any common attributes needed across all admin category pages
        model.addAttribute("productService", productService);
    }
    
    @GetMapping
    public String categoryList(Model model, 
                            @RequestParam(defaultValue = "0") int page,
                            HttpSession session) {
        verifyAdminAccess(session);
        
        List<Category> categories = categoryService.getAllCategories();
        
        model.addAttribute("categories", categories);
        model.addAttribute("category", new Category());
        model.addAttribute("editing", false); // Initialize editing flag
        
        return "admin/category/list";
    }
    
    @PostMapping("/create")
    public String createCategory(@Valid Category category, 
                              BindingResult result,
                              RedirectAttributes redirectAttributes,
                              HttpSession session) {
        verifyAdminAccess(session);
        
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Invalid category data");
            return "redirect:/admin/categories";
        }
        
        try {
            Category savedCategory = categoryService.saveCategory(category);
            redirectAttributes.addFlashAttribute("success", "Category created successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create category: " + e.getMessage());
        }
        
        return "redirect:/admin/categories";
    }
    
    @GetMapping("/{id}/edit")
    public String editCategoryForm(@PathVariable Long id, 
                               Model model,
                               RedirectAttributes redirectAttributes,
                               HttpSession session) {
        verifyAdminAccess(session);
        
        Optional<Category> categoryOpt = categoryService.getCategoryById(id);
        if (categoryOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Category not found");
            return "redirect:/admin/categories";
        }
        
        model.addAttribute("category", categoryOpt.get());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("editing", true);
        model.addAttribute("productService", productService); // Add productService to access countProductsByCategory
        
        return "admin/category/list";
    }
    
    @PostMapping("/{id}/update")
    public String updateCategory(@PathVariable Long id,
                              @Valid Category category,
                              BindingResult result,
                              RedirectAttributes redirectAttributes,
                              HttpSession session) {
        verifyAdminAccess(session);
        
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Invalid category data");
            return "redirect:/admin/categories";
        }
        
        try {
            Optional<Category> existingCategoryOpt = categoryService.getCategoryById(id);
            if (existingCategoryOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Category not found");
                return "redirect:/admin/categories";
            }
            
            Category existingCategory = existingCategoryOpt.get();
            existingCategory.setName(category.getName());
            existingCategory.setDescription(category.getDescription());
            
            categoryService.saveCategory(existingCategory);
            redirectAttributes.addFlashAttribute("success", "Category updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update category: " + e.getMessage());
        }
        
        return "redirect:/admin/categories";
    }
    
    @PostMapping("/{id}/delete")
    public String deleteCategory(@PathVariable Long id,
                              RedirectAttributes redirectAttributes,
                              HttpSession session) {
        verifyAdminAccess(session);
        
        try {
            // Check if the category has products
            int productCount = productService.countProductsByCategory(id);
            if (productCount > 0) {
                redirectAttributes.addFlashAttribute("error", 
                    "Cannot delete category: it has " + productCount + " products. Reassign them first.");
                return "redirect:/admin/categories";
            }
            
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("success", "Category deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete category: " + e.getMessage());
        }
        
        return "redirect:/admin/categories";
    }
    
    // Tag management
    @GetMapping("/tags")
    public String tagList(Model model, HttpSession session) {
        verifyAdminAccess(session);
        
        List<Tag> tags = productService.getAllTags();
        
        model.addAttribute("tags", tags);
        model.addAttribute("tag", new Tag());
        
        return "admin/category/tags";
    }
    
    @PostMapping("/tags/create")
    public String createTag(@Valid Tag tag, 
                         BindingResult result,
                         RedirectAttributes redirectAttributes,
                         HttpSession session) {
        verifyAdminAccess(session);
        
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Invalid tag data");
            return "redirect:/admin/categories/tags";
        }
        
        try {
            productService.saveTag(tag);
            redirectAttributes.addFlashAttribute("success", "Tag created successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create tag: " + e.getMessage());
        }
        
        return "redirect:/admin/categories/tags";
    }
    
    @PostMapping("/tags/{id}/delete")
    public String deleteTag(@PathVariable Long id,
                         RedirectAttributes redirectAttributes,
                         HttpSession session) {
        verifyAdminAccess(session);
        
        try {
            productService.deleteTag(id);
            redirectAttributes.addFlashAttribute("success", "Tag deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete tag: " + e.getMessage());
        }
        
        return "redirect:/admin/categories/tags";
    }
    
    // Handle admin access denied exceptions
    @ExceptionHandler(AccessDeniedException.class)
    public String handleAdminAccessDenied(AccessDeniedException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "You need admin privileges to access this page");
        return "redirect:/login";
    }
    
    @GetMapping("/dashboard")
    public String adminDashboard(Model model, HttpSession session) {
        verifyAdminAccess(session);
        
        // Revenue stats
        model.addAttribute("totalRevenue", "8,245.50");
        model.addAttribute("revenueChange", 12.5); // percentage change
        
        // Order stats
        model.addAttribute("orderCount", 156);
        model.addAttribute("orderChange", 8.2);
        
        return "admin/dashboard";
    }
}
