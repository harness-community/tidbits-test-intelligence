package com.example.ecommerce.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Coupon Model Tests")
class CouponTest {

    private Coupon percentageCoupon;
    private Coupon fixedCoupon;

    @BeforeEach
    void setUp() {
        percentageCoupon = Coupon.builder()
                .code("SAVE10")
                .discountType(Coupon.DiscountType.PERCENTAGE)
                .discountValue(new BigDecimal("10"))
                .minimumOrderAmount(new BigDecimal("20.00"))
                .usageLimit(100)
                .usageCount(0)
                .active(true)
                .validFrom(LocalDateTime.now().minusDays(1))
                .validUntil(LocalDateTime.now().plusDays(30))
                .build();

        fixedCoupon = Coupon.builder()
                .code("FIXED5")
                .discountType(Coupon.DiscountType.FIXED_AMOUNT)
                .discountValue(new BigDecimal("5.00"))
                .minimumOrderAmount(new BigDecimal("10.00"))
                .active(true)
                .build();
    }

    @Nested
    @DisplayName("isValid() Tests")
    class IsValidTests {
        @Test
        void isValid_whenAllConditionsMet_returnsTrue() {
            assertThat(percentageCoupon.isValid()).isTrue();
        }

        @Test
        void isValid_whenInactive_returnsFalse() {
            percentageCoupon.setActive(false);
            assertThat(percentageCoupon.isValid()).isFalse();
        }

        @Test
        void isValid_whenExpired_returnsFalse() {
            percentageCoupon.setValidUntil(LocalDateTime.now().minusHours(1));
            assertThat(percentageCoupon.isValid()).isFalse();
        }

        @Test
        void isValid_whenNotYetStarted_returnsFalse() {
            percentageCoupon.setValidFrom(LocalDateTime.now().plusDays(1));
            assertThat(percentageCoupon.isValid()).isFalse();
        }

        @Test
        void isValid_whenUsageLimitReached_returnsFalse() {
            percentageCoupon.setUsageCount(100);
            assertThat(percentageCoupon.isValid()).isFalse();
        }

        @Test
        void isValid_whenUsageLimitIsNull_noLimitEnforced() {
            percentageCoupon.setUsageLimit(null);
            percentageCoupon.setUsageCount(9999);
            assertThat(percentageCoupon.isValid()).isTrue();
        }

        @Test
        void isValid_whenNoDateRange_returnsTrue() {
            fixedCoupon.setValidFrom(null);
            fixedCoupon.setValidUntil(null);
            assertThat(fixedCoupon.isValid()).isTrue();
        }
    }

    @Nested
    @DisplayName("isValidForOrderAmount() Tests")
    class IsValidForOrderAmountTests {
        @Test
        void isValidForOrderAmount_whenOrderMeetsMinimum_returnsTrue() {
            assertThat(percentageCoupon.isValidForOrderAmount(new BigDecimal("50.00"))).isTrue();
        }

        @Test
        void isValidForOrderAmount_whenOrderBelowMinimum_returnsFalse() {
            assertThat(percentageCoupon.isValidForOrderAmount(new BigDecimal("10.00"))).isFalse();
        }

        @Test
        void isValidForOrderAmount_whenOrderEqualsMinimum_returnsTrue() {
            assertThat(percentageCoupon.isValidForOrderAmount(new BigDecimal("20.00"))).isTrue();
        }

        @Test
        void isValidForOrderAmount_whenMinimumIsNull_noMinimumEnforced() {
            percentageCoupon.setMinimumOrderAmount(null);
            assertThat(percentageCoupon.isValidForOrderAmount(new BigDecimal("1.00"))).isTrue();
        }
    }

    @Nested
    @DisplayName("calculateDiscount() Tests")
    class CalculateDiscountTests {
        @Test
        void calculateDiscount_percentage_calculatesCorrectly() {
            BigDecimal discount = percentageCoupon.calculateDiscount(new BigDecimal("100.00"));
            assertThat(discount).isEqualByComparingTo("10.00");
        }

        @Test
        void calculateDiscount_fixed_returnsFixedAmount() {
            BigDecimal discount = fixedCoupon.calculateDiscount(new BigDecimal("50.00"));
            assertThat(discount).isEqualByComparingTo("5.00");
        }

        @Test
        void calculateDiscount_whenMaxDiscountApplied_capsAtMax() {
            percentageCoupon.setMaximumDiscountAmount(new BigDecimal("8.00"));
            BigDecimal discount = percentageCoupon.calculateDiscount(new BigDecimal("100.00"));
            assertThat(discount).isEqualByComparingTo("8.00");
        }

        @Test
        void calculateDiscount_fixedExceedingOrderTotal_capsAtOrderTotal() {
            fixedCoupon.setDiscountValue(new BigDecimal("100.00"));
            BigDecimal discount = fixedCoupon.calculateDiscount(new BigDecimal("30.00"));
            assertThat(discount).isEqualByComparingTo("30.00");
        }

        @Test
        void calculateDiscount_whenNotValid_returnsZero() {
            percentageCoupon.setActive(false);
            BigDecimal discount = percentageCoupon.calculateDiscount(new BigDecimal("100.00"));
            assertThat(discount).isEqualByComparingTo("0.00");
        }
    }

    @Nested
    @DisplayName("incrementUsage() Tests")
    class IncrementUsageTests {
        @Test
        void incrementUsage_increasesUsageCountByOne() {
            int initial = percentageCoupon.getUsageCount();
            percentageCoupon.incrementUsage();
            assertThat(percentageCoupon.getUsageCount()).isEqualTo(initial + 1);
        }

        @Test
        void incrementUsage_multipleTimes_incrementsCorrectly() {
            percentageCoupon.incrementUsage();
            percentageCoupon.incrementUsage();
            percentageCoupon.incrementUsage();
            assertThat(percentageCoupon.getUsageCount()).isEqualTo(3);
        }
    }
}
