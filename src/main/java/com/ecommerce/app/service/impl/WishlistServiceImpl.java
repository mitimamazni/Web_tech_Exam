package com.ecommerce.app.service.impl;

import com.ecommerce.app.dto.WishlistItemDTO;
import com.ecommerce.app.model.Product;
import com.ecommerce.app.model.User;
import com.ecommerce.app.model.Wishlist;
import com.ecommerce.app.model.WishlistItem;
import com.ecommerce.app.repository.UserWishlistRepository;
import com.ecommerce.app.repository.WishlistRepository;
import com.ecommerce.app.service.WishlistService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WishlistServiceImpl implements WishlistService {

    @Autowired
    private WishlistRepository wishlistRepository;
    
    @Autowired
    private UserWishlistRepository userWishlistRepository;
    
    @Override
    public Wishlist getUserWishlist(User user) {
        return userWishlistRepository.findByUser(user)
                .orElseGet(() -> {
                    Wishlist wishlist = new Wishlist();
                    wishlist.setUser(user);
                    return userWishlistRepository.save(wishlist);
                });
    }
    
    @Override
    @Transactional
    public void addProductToWishlist(User user, Product product) {
        // Get or create user's wishlist
        Wishlist wishlist = getUserWishlist(user);
        
        // Check if product is already in the wishlist
        if (!wishlistRepository.existsByUserAndProduct(user, product)) {
            WishlistItem wishlistItem = new WishlistItem(wishlist, product);
            wishlistRepository.save(wishlistItem);
            
            // Update the wishlist updated timestamp
            wishlist.setUpdatedAt(java.time.LocalDateTime.now());
            userWishlistRepository.save(wishlist);
        }
    }
    
    @Override
    @Transactional
    public void removeProductFromWishlist(User user, Product product) {
        wishlistRepository.deleteByUserAndProduct(user, product);
        
        // Update the wishlist updated timestamp
        Wishlist wishlist = getUserWishlist(user);
        wishlist.setUpdatedAt(java.time.LocalDateTime.now());
        userWishlistRepository.save(wishlist);
    }
    
    @Override
    @Transactional
    public void clearWishlist(User user) {
        wishlistRepository.deleteByUser(user);
        
        // Update the wishlist updated timestamp
        Wishlist wishlist = getUserWishlist(user);
        wishlist.setUpdatedAt(java.time.LocalDateTime.now());
        userWishlistRepository.save(wishlist);
    }
    
    @Override
    public List<Product> getWishlistItems(User user) {
        return wishlistRepository.findByUser(user)
                .stream()
                .map(WishlistItem::getProduct)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<WishlistItemDTO> getWishlistItemDTOs(User user) {
        return wishlistRepository.findByUser(user)
                .stream()
                .map(WishlistItemDTO::new)
                .collect(Collectors.toList());
    }
    
    @Override
    public int getWishlistItemCount(User user) {
        return wishlistRepository.countByUser(user);
    }
    
    @Override
    public boolean isProductInWishlist(User user, Long productId) {
        return wishlistRepository.findByUser(user)
                .stream()
                .anyMatch(item -> item.getProduct().getId().equals(productId));
    }
}
