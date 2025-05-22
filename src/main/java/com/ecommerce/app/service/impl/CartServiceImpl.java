package com.ecommerce.app.service.impl;

import com.ecommerce.app.model.Cart;
import com.ecommerce.app.model.CartItem;
import com.ecommerce.app.model.CartStatus;
import com.ecommerce.app.model.Product;
import com.ecommerce.app.model.User;
import com.ecommerce.app.repository.CartItemRepository;
import com.ecommerce.app.repository.CartRepository;
import com.ecommerce.app.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartItemRepository cartItemRepository;
    
    @Autowired
    private CartRepository cartRepository;

    @Override
    public Cart getActiveCart(User user) {
        Optional<Cart> activeCart = cartRepository.findFirstByUserAndStatusOrderByCreatedAtDesc(user, CartStatus.ACTIVE);
        
        if (activeCart.isPresent()) {
            return activeCart.get();
        } else {
            Cart newCart = new Cart();
            newCart.setUser(user);
            newCart.setStatus(CartStatus.ACTIVE);
            return cartRepository.save(newCart);
        }
    }

    @Override
    @Transactional
    public CartItem addProductToCart(User user, Product product, int quantity) {
        Cart cart = getActiveCart(user);
        return addProductToCart(cart, product, quantity);
    }

    @Override
    @Transactional
    public CartItem addProductToCart(Cart cart, Product product, int quantity) {
        // First check if the product already exists in the user's cart
        List<CartItem> cartItems = cart.getCartItems();
        Optional<CartItem> existingCartItemOpt = cartItems.stream()
            .filter(item -> item.getProduct().getId().equals(product.getId()))
            .findFirst();
        
        if (existingCartItemOpt.isPresent()) {
            CartItem existingCartItem = existingCartItemOpt.get();
            existingCartItem.setQuantity(existingCartItem.getQuantity() + quantity);
            
            // Update the price if it has changed
            if (existingCartItem.getPrice() == null || 
                !existingCartItem.getPrice().equals(product.getPrice())) {
                existingCartItem.setPrice(product.getPrice());
            }
            
            return cartItemRepository.save(existingCartItem);
        } else {
            CartItem newCartItem = new CartItem(cart, product, quantity);
            return cartItemRepository.save(newCartItem);
        }
    }

    @Override
    @Transactional
    public void removeProductFromCart(User user, Long productId) {
        Cart cart = getActiveCart(user);
        removeProductFromCart(cart, productId);
    }

    @Override
    @Transactional
    public void removeProductFromCart(Cart cart, Long productId) {
        List<CartItem> cartItems = cart.getCartItems();
        
        cartItems.stream()
            .filter(item -> item.getProduct().getId().equals(productId))
            .findFirst()
            .ifPresent(item -> {
                cart.getCartItems().remove(item);
                cartItemRepository.delete(item);
            });
    }

    @Override
    @Transactional
    public void updateCartItemQuantity(User user, Long productId, int quantity) {
        Cart cart = getActiveCart(user);
        updateCartItemQuantity(cart, productId, quantity);
    }

    @Override
    @Transactional
    public void updateCartItemQuantity(Cart cart, Long productId, int quantity) {
        List<CartItem> cartItems = cart.getCartItems();
        
        cartItems.stream()
            .filter(item -> item.getProduct().getId().equals(productId))
            .findFirst()
            .ifPresent(item -> {
                if (quantity <= 0) {
                    cart.getCartItems().remove(item);
                    cartItemRepository.delete(item);
                } else {
                    item.setQuantity(quantity);
                    cartItemRepository.save(item);
                }
            });
    }

    @Override
    public List<CartItem> getCartItems(User user) {
        Cart cart = getActiveCart(user);
        return cart.getCartItems();
    }

    @Override
    @Transactional
    public void clearCart(User user) {
        Cart cart = getActiveCart(user);
        clearCart(cart);
    }

    @Override
    @Transactional
    public void clearCart(Cart cart) {
        // Get a copy of the items to avoid concurrent modification
        List<CartItem> itemsToRemove = List.copyOf(cart.getCartItems());
        
        // Clear the cart items
        cart.getCartItems().clear();
        cartRepository.save(cart);
        
        // Delete the items from the repository
        for (CartItem item : itemsToRemove) {
            cartItemRepository.delete(item);
        }
    }

    @Override
    public BigDecimal getCartTotal(User user) {
        Cart cart = getActiveCart(user);
        return getCartTotal(cart);
    }

    @Override
    public BigDecimal getCartTotal(Cart cart) {
        List<CartItem> cartItems = cart.getCartItems();
        
        return cartItems.stream()
            .map(item -> {
                BigDecimal price = item.getPrice();
                return price.multiply(new BigDecimal(item.getQuantity()));
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public int getCartItemCount(User user) {
        Cart cart = getActiveCart(user);
        return getCartItemCount(cart);
    }

    @Override
    public int getCartItemCount(Cart cart) {
        List<CartItem> cartItems = cart.getCartItems();
        
        return cartItems.stream()
            .mapToInt(CartItem::getQuantity)
            .sum();
    }
}
