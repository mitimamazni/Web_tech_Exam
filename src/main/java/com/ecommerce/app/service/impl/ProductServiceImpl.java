package com.ecommerce.app.service.impl;

import com.ecommerce.app.model.Product;
import com.ecommerce.app.model.ProductImage;
import com.ecommerce.app.model.Review;
import com.ecommerce.app.model.Tag;
import com.ecommerce.app.repository.ProductImageRepository;
import com.ecommerce.app.repository.ProductRepository;
import com.ecommerce.app.repository.ReviewRepository;
import com.ecommerce.app.repository.TagRepository;
import com.ecommerce.app.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
    
    @Override
    public List<Product> getAllProductsSorted(Sort sort) {
        return productRepository.findAll(sort);
    }

    @Override
    public Page<Product> getAllProductsPaged(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Override
    public Page<Product> getProductsByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable);
    }

    @Override
    public Page<Product> searchProducts(String keyword, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCase(keyword, pageable);
    }

    @Override
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    @Transactional
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void updateProductStock(Long productId, int quantity) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            int newStock = product.getStockQuantity() - quantity;

            // Check if there's enough stock available
            if (newStock < 0) {
                throw new RuntimeException("Not enough stock available for product: " + product.getName());
            }

            product.setStockQuantity(newStock);
            productRepository.save(product);
        } else {
            throw new RuntimeException("Product not found with ID: " + productId);
        }
    }

    @Override
    public List<Product> getActiveProducts() {
        return productRepository.findByActiveTrue();
    }

    @Override
    public Page<Product> getActiveProductsPaged(Pageable pageable) {
        return productRepository.findByActiveTrue(pageable);
    }

    @Override
    public int countProductsByCategory(Long categoryId) {
        return productRepository.countByCategoryId(categoryId);
    }

    @Override
    @Transactional
    public void addProductImages(Product product, List<MultipartFile> imageFiles, boolean setFirstAsPrimary) {
        if (imageFiles == null || imageFiles.isEmpty()) {
            return;
        }

        // Create upload directory if it doesn't exist
        Path uploadDir = Paths.get("src/main/resources/static/images/products/" + product.getId());
        try {
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            boolean hasPrimary = product.getImages().stream().anyMatch(ProductImage::getIsPrimary);
            boolean isFirst = true;

            for (MultipartFile file : imageFiles) {
                if (file.isEmpty()) {
                    continue;
                }

                // Generate unique filename
                String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                Path filePath = uploadDir.resolve(filename);

                // Save file to disk
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                // Create image entity
                ProductImage image = new ProductImage();
                image.setProduct(product);
                image.setImageUrl("/images/products/" + product.getId() + "/" + filename);
                image.setDisplayOrder(product.getImages().size() + 1);

                // Set as primary if needed
                if ((setFirstAsPrimary && isFirst && !hasPrimary) || (!hasPrimary && product.getImages().isEmpty())) {
                    image.setIsPrimary(true);

                    // Ensure no other image is primary
                    product.getImages().forEach(img -> img.setIsPrimary(false));

                    hasPrimary = true;
                }

                product.getImages().add(image);
                isFirst = false;
            }

            productRepository.save(product);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store image files", e);
        }
    }

    @Override
    @Transactional
    public void deleteProductImages(Product product, List<Long> imageIds) {
        if (imageIds == null || imageIds.isEmpty()) {
            return;
        }

        // Filter images to delete
        List<ProductImage> imagesToDelete = product.getImages().stream()
                .filter(image -> imageIds.contains(image.getId()))
                .collect(Collectors.toList());

        // Check if we're deleting the primary image
        boolean deletingPrimary = imagesToDelete.stream().anyMatch(ProductImage::getIsPrimary);

        // Remove images from product
        product.getImages().removeAll(imagesToDelete);

        // If we deleted the primary image, set a new one if available
        if (deletingPrimary && !product.getImages().isEmpty()) {
            product.getImages().get(0).setIsPrimary(true);
        }

        productRepository.save(product);

        // Delete physical files (async in a real app)
        for (ProductImage image : imagesToDelete) {
            try {
                String relativePath = image.getImageUrl();
                if (relativePath.startsWith("/")) {
                    relativePath = relativePath.substring(1);
                }
                Path filePath = Paths.get("src/main/resources/static/" + relativePath);
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                // Log error but continue
                System.err.println("Failed to delete image file: " + e.getMessage());
            }
        }
    }

    @Override
    @Transactional
    public void setPrimaryProductImage(Product product, Long imageId) {
        // Find the image to set as primary
        Optional<ProductImage> newPrimaryOpt = product.getImages().stream()
                .filter(image -> image.getId().equals(imageId))
                .findFirst();

        if (newPrimaryOpt.isPresent()) {
            // Set all images to non-primary
            product.getImages().forEach(image -> image.setIsPrimary(false));

            // Set the selected image as primary
            newPrimaryOpt.get().setIsPrimary(true);

            productRepository.save(product);
        }
    }

    @Override
    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    @Override
    @Transactional
    public void setProductTags(Product product, List<Long> tagIds) {
        // Clear existing tags
        product.getTags().clear();

        // Add selected tags
        if (tagIds != null && !tagIds.isEmpty()) {
            List<Tag> tags = tagRepository.findAllById(tagIds);
            product.setTags(new HashSet<>(tags));
        }

        productRepository.save(product);
    }

    @Override
    public Optional<Review> getReviewById(Long id) {
        return reviewRepository.findById(id);
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId) {
        reviewRepository.deleteById(reviewId);
    }

    @Override
    @Transactional
    public Tag saveTag(Tag tag) {
        return tagRepository.save(tag);
    }

    @Override
    @Transactional
    public void deleteTag(Long tagId) {
        // Get the tag
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("Tag not found"));

        // Remove this tag from all products that have it
        List<Product> productsWithTag = productRepository.findAll().stream()
                .filter(p -> p.getTags() != null && p.getTags().contains(tag))
                .collect(Collectors.toList());

        for (Product product : productsWithTag) {
            product.getTags().remove(tag);
            productRepository.save(product);
        }

        // Delete the tag
        tagRepository.deleteById(tagId);
    }
}
