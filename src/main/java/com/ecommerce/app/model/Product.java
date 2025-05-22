package com.ecommerce.app.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull
    @Positive
    @Column(nullable = false)
    private BigDecimal price;

    @Column(name = "sale_price")
    private BigDecimal salePrice;

    @Column(name = "stock_quantity")
    private Integer stockQuantity = 0;

    @Column(name = "is_subscription")
    private Boolean isSubscription = false;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<ProductImage> images = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "product_tags",
        joinColumns = @JoinColumn(name = "product_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @OneToMany(mappedBy = "product")
    private List<Review> reviews = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    private boolean active = true;
    
    @Transient
    public String getImageUrl() {
        if (images == null || images.isEmpty()) {
            return null;
        }
        // First try to find the primary image
        return images.stream()
                .filter(ProductImage::getIsPrimary)
                .findFirst()
                .map(ProductImage::getImageUrl)
                .orElse(
                    // If no primary image is found, return the first image URL
                    images.get(0).getImageUrl()
                );
    }
    
    @Transient
    public Double getAverageRating() {
        if (reviews.isEmpty()) {
            return 0.0;
        }
        return reviews.stream()
            .mapToInt(Review::getRating)
            .average()
            .orElse(0.0);
    }

    // Custom getter for isSubscription with proper Boolean name
    public Boolean getIsSubscription() {
        return isSubscription;
    }
    
    // Custom setter for isSubscription with proper Boolean name
    public void setIsSubscription(Boolean isSubscription) {
        this.isSubscription = isSubscription;
    }
}
