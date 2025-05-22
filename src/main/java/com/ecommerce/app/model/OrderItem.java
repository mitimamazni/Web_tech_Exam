package com.ecommerce.app.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull
    @Positive
    @Column(nullable = false)
    private Integer quantity;

    @NotNull
    @Positive
    @Column(nullable = false)
    private BigDecimal price;  // Store price at time of purchase
    
    // This method was moved to the bottom of the class with @Transient annotation

    // Constructor to create an order item from a product
    public OrderItem(Product product, Integer quantity) {
        this.product = product;
        this.quantity = quantity;
        this.price = product.getSalePrice() != null ? product.getSalePrice() : product.getPrice();
    }
    
    // Constructor to create an order item from a cart item
    public OrderItem(CartItem cartItem) {
        this.product = cartItem.getProduct();
        this.quantity = cartItem.getQuantity();
        
        // Get the price (use sale price if available)
        if (cartItem.getProduct() != null) {
            if (cartItem.getProduct().getSalePrice() != null) {
                this.price = cartItem.getProduct().getSalePrice();
            } else {
                this.price = cartItem.getProduct().getPrice();
            }
        }
        this.price = cartItem.getPrice();
    }
    
    // Calculate total for this item
    @Transient
    public BigDecimal getTotalPrice() {
        if (price == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        return price.multiply(new BigDecimal(quantity));
    }
    
    // Added getter for product name to support templates
    @Transient
    public String getProductName() {
        return product != null ? product.getName() : "Unknown Product";
    }
}
