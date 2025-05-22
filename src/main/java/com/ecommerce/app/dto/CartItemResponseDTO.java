package com.ecommerce.app.dto;

import lombok.Data;

@Data
public class CartItemResponseDTO {
    private Long productId;
    private String name;
    private Double price;
    private Double salePrice;
    private Integer quantity;
    private String imageUrl;
    private Integer stockQuantity;
}
