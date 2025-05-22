package com.ecommerce.app.dto;

import com.ecommerce.app.model.Product;
import com.ecommerce.app.model.ProductImage;
import com.ecommerce.app.model.WishlistItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WishlistItemDTO {
    private Long id;
    private Long productId;
    private String name;
    private BigDecimal price;
    private BigDecimal salePrice;
    private String imageUrl;
    private LocalDateTime addedAt;
    
    // Constructor from WishlistItem entity
    public WishlistItemDTO(WishlistItem wishlistItem) {
        this.id = wishlistItem.getId();
        
        Product product = wishlistItem.getProduct();
        if (product != null) {
            this.productId = product.getId();
            this.name = product.getName();
            this.price = product.getPrice();
            this.salePrice = product.getSalePrice();
            
            // Get image URL from product images collection
            if (product.getImages() != null && !product.getImages().isEmpty()) {
                try {
                    // First try to find primary image
                    ProductImage primaryImage = product.getImages().stream()
                        .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                        .findFirst()
                        .orElse(null);
                        
                    if (primaryImage != null) {
                        this.imageUrl = primaryImage.getImageUrl();
                    } else {
                        // If no primary image, use the first available image safely
                        this.imageUrl = product.getImages().get(0).getImageUrl();
                    }
                } catch (Exception e) {
                    // Fallback to default in case of any exceptions
                    this.imageUrl = "/images/product-placeholder.jpg";
                }
            } else {
                // No images available, use placeholder
                this.imageUrl = "/images/product-placeholder.jpg";
            }
        }
        
        this.addedAt = wishlistItem.getAddedAt();
    }
}
