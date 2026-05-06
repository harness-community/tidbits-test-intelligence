package com.example.ecommerce.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull
    @Min(1)
    private Integer quantity;

    @NotNull
    private BigDecimal unitPrice;

    private BigDecimal discountPercent;

    public BigDecimal getLineTotal() {
        BigDecimal base = unitPrice.multiply(BigDecimal.valueOf(quantity));
        if (discountPercent != null && discountPercent.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discount = base.multiply(discountPercent).divide(BigDecimal.valueOf(100));
            return base.subtract(discount);
        }
        return base;
    }

    public BigDecimal getDiscountAmount() {
        if (discountPercent == null || discountPercent.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal base = unitPrice.multiply(BigDecimal.valueOf(quantity));
        return base.multiply(discountPercent).divide(BigDecimal.valueOf(100));
    }
}
