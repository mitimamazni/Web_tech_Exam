package com.ecommerce.app.controller;

import com.ecommerce.app.model.Product;
import com.ecommerce.app.model.User;
import com.ecommerce.app.service.CartService;
import com.ecommerce.app.service.CategoryService;
import com.ecommerce.app.service.ProductService;
import com.ecommerce.app.service.UserService;
import com.ecommerce.app.service.WishlistService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private UserService userService;
    
    @Autowired(required = false)
    private WishlistService wishlistService;

    @Autowired
    private CartService cartService;
    
    @GetMapping({"/", "/home"})
    public String home(Model model, 
                      @RequestParam(defaultValue = "0") int page, 
                      @RequestParam(defaultValue = "8") int size,
                      HttpSession session) {
        
        Page<Product> productPage = productService.getActiveProductsPaged(PageRequest.of(page, size));
        
        // Add cart and wishlist item count to the model
        String username = (String) session.getAttribute("username");
        if (username != null) {
            try {
                User user = userService.findByUsername(username).orElse(null);
                if (user != null) {
                    model.addAttribute("cartItemCount", cartService.getCartItemCount(user));
                    
                    // If you have a wishlistService, add wishlist count too
                    if (wishlistService != null) {
                        try {
                            model.addAttribute("wishlistItemCount", wishlistService.getWishlistItemCount(user));
                        } catch (Exception e) {
                            System.err.println("Error retrieving wishlist count: " + e.getMessage());
                            model.addAttribute("wishlistItemCount", 0);
                        }
                    }
                }
            } catch (Exception e) {
                // Log the exception, but don't break page rendering
                System.err.println("Error retrieving cart count: " + e.getMessage());
            }
        }
        
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("categories", categoryService.getAllCategories());
        
        return "home";
    }

    // About page method moved to AboutController
    
    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }
}
