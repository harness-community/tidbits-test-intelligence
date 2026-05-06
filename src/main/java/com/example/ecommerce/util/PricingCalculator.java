package com.example.ecommerce.util;

import com.example.ecommerce.model.Customer;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class PricingCalculator {

    private static final BigDecimal TAX_RATE_US = new BigDecimal("0.08");
    private static final BigDecimal TAX_RATE_CA = new BigDecimal("0.13");
    private static final BigDecimal TAX_RATE_DEFAULT = new BigDecimal("0.10");

    private static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("50.00");
    private static final BigDecimal STANDARD_SHIPPING = new BigDecimal("5.99");
    private static final BigDecimal EXPRESS_SHIPPING = new BigDecimal("14.99");

    public BigDecimal calculateTax(BigDecimal amount, String country) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be null or negative");
        }
        BigDecimal rate = switch (country.toUpperCase()) {
            case "US", "USA" -> TAX_RATE_US;
            case "CA", "CANADA" -> TAX_RATE_CA;
            default -> TAX_RATE_DEFAULT;
        };
        return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateShipping(BigDecimal orderTotal, boolean isExpress) {
        if (orderTotal == null || orderTotal.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Order total cannot be null or negative");
        }
        if (isExpress) return EXPRESS_SHIPPING;
        if (orderTotal.compareTo(FREE_SHIPPING_THRESHOLD) >= 0) return BigDecimal.ZERO;
        return STANDARD_SHIPPING;
    }

    public BigDecimal applyTierDiscount(BigDecimal amount, Customer.CustomerTier tier) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be null or negative");
        }
        BigDecimal discountRate = switch (tier) {
            case STANDARD -> BigDecimal.ZERO;
            case SILVER -> new BigDecimal("0.05");
            case GOLD -> new BigDecimal("0.10");
            case PLATINUM -> new BigDecimal("0.15");
        };
        BigDecimal discount = amount.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
        return amount.subtract(discount);
    }

    public BigDecimal calculateFinalTotal(BigDecimal subtotal, BigDecimal discount,
                                         BigDecimal tax, BigDecimal shipping) {
        BigDecimal total = subtotal
                .subtract(discount != null ? discount : BigDecimal.ZERO)
                .add(tax != null ? tax : BigDecimal.ZERO)
                .add(shipping != null ? shipping : BigDecimal.ZERO);
        return total.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal roundToTwoDecimals(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
