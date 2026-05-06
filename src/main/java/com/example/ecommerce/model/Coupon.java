package com.example.ecommerce.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    @Enumerated(EnumType.STRING)
    private DiscountType discountType;

    private BigDecimal discountValue;

    private BigDecimal minimumOrderAmount;

    private BigDecimal maximumDiscountAmount;

    private Integer usageLimit;

    private Integer usageCount = 0;

    private LocalDateTime validFrom;
    private LocalDateTime validUntil;

    private boolean active = true;

    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        return active
                && (validFrom == null || !now.isBefore(validFrom))
                && (validUntil == null || !now.isAfter(validUntil))
                && (usageLimit == null || usageCount < usageLimit);
    }

    public boolean isValidForOrderAmount(BigDecimal orderAmount) {
        return isValid() && (minimumOrderAmount == null
                || orderAmount.compareTo(minimumOrderAmount) >= 0);
    }

    public BigDecimal calculateDiscount(BigDecimal orderAmount) {
        if (!isValidForOrderAmount(orderAmount)) return BigDecimal.ZERO;
        BigDecimal discount;
        if (discountType == DiscountType.PERCENTAGE) {
            discount = orderAmount.multiply(discountValue).divide(BigDecimal.valueOf(100));
        } else {
            discount = discountValue;
        }
        if (maximumDiscountAmount != null && discount.compareTo(maximumDiscountAmount) > 0) {
            discount = maximumDiscountAmount;
        }
        return discount.min(orderAmount);
    }

    public void incrementUsage() {
        this.usageCount++;
    }

    public enum DiscountType {
        PERCENTAGE, FIXED_AMOUNT
    }
}
