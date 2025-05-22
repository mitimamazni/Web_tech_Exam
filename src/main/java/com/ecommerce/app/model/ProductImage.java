package com.ecommerce.app.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "product_images")
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotBlank
    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "is_primary")
    private Boolean isPrimary = false;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    // Custom getter for isPrimary with proper Boolean name
    public Boolean getIsPrimary() {
        return isPrimary;
    }

    // Custom setter for isPrimary with proper Boolean name
    public void setIsPrimary(Boolean isPrimary) {
        this.isPrimary = isPrimary;
    }
}
