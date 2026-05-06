package com.example.ecommerce.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 200)
    private String name;

    @Size(max = 1000)
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 10, fraction = 2)
    private BigDecimal price;

    @NotNull
    @Min(0)
    private Integer stockQuantity;

    @NotBlank
    private String category;

    private String sku;

    private boolean active = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean isInStock() {
        return stockQuantity != null && stockQuantity > 0;
    }

    public boolean isInStock(int requiredQuantity) {
        return stockQuantity != null && stockQuantity >= requiredQuantity;
    }

    public void decreaseStock(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        if (!isInStock(quantity)) throw new IllegalStateException("Insufficient stock");
        this.stockQuantity -= quantity;
    }

    public void increaseStock(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        this.stockQuantity += quantity;
    }
}
