package com.ecommerce.app.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cart_items")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull
    @Positive
    @Column(nullable = false)
    private Integer quantity = 1;
    
    @NotNull
    @Column(nullable = false)
    private BigDecimal price;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructor for easy creation of cart items
    public CartItem(Cart cart, Product product, Integer quantity) {
        this.cart = cart;
        this.product = product;
        this.quantity = quantity;
        
        if (product != null) {
            this.price = product.getSalePrice() != null ? product.getSalePrice() : product.getPrice();
        }
    }
    
    // Calculate total price for this cart item
    @Transient
    public BigDecimal getTotalPrice() {
        return price.multiply(new BigDecimal(quantity));
    }
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        // Ensure price is set
        if (this.price == null && this.product != null) {
            this.price = this.product.getSalePrice() != null ? 
                this.product.getSalePrice() : this.product.getPrice();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
