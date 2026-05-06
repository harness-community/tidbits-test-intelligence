package com.example.ecommerce.util;

import com.example.ecommerce.model.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("PricingCalculator Tests")
class PricingCalculatorTest {

    private PricingCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new PricingCalculator();
    }

    @Nested
    @DisplayName("calculateTax() Tests")
    class CalculateTaxTests {
        @ParameterizedTest
        @CsvSource({
                "100.00, US, 8.00",
                "100.00, USA, 8.00",
                "100.00, CA, 13.00",
                "100.00, CANADA, 13.00",
                "100.00, UK, 10.00",
                "200.00, US, 16.00",
                "50.00, CA, 6.50"
        })
        void calculateTax_forKnownCountries(String amount, String country, String expectedTax) {
            BigDecimal tax = calculator.calculateTax(new BigDecimal(amount), country);
            assertThat(tax).isEqualByComparingTo(expectedTax);
        }

        @Test
        void calculateTax_withNull_throwsException() {
            assertThatThrownBy(() -> calculator.calculateTax(null, "US"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void calculateTax_withNegativeAmount_throwsException() {
            assertThatThrownBy(() -> calculator.calculateTax(new BigDecimal("-10.00"), "US"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void calculateTax_withZeroAmount_returnsZero() {
            BigDecimal tax = calculator.calculateTax(BigDecimal.ZERO, "US");
            assertThat(tax).isEqualByComparingTo("0.00");
        }

        @Test
        void calculateTax_roundsToTwoDecimals() {
            BigDecimal tax = calculator.calculateTax(new BigDecimal("33.33"), "US");
            assertThat(tax.scale()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("calculateShipping() Tests")
    class CalculateShippingTests {
        @Test
        void calculateShipping_standardBelowThreshold_chargesShipping() {
            BigDecimal shipping = calculator.calculateShipping(new BigDecimal("30.00"), false);
            assertThat(shipping).isEqualByComparingTo("5.99");
        }

        @Test
        void calculateShipping_standardAtThreshold_isFree() {
            BigDecimal shipping = calculator.calculateShipping(new BigDecimal("50.00"), false);
            assertThat(shipping).isEqualByComparingTo("0.00");
        }

        @Test
        void calculateShipping_standardAboveThreshold_isFree() {
            BigDecimal shipping = calculator.calculateShipping(new BigDecimal("100.00"), false);
            assertThat(shipping).isEqualByComparingTo("0.00");
        }

        @Test
        void calculateShipping_expressAlwaysCharged() {
            BigDecimal shipping = calculator.calculateShipping(new BigDecimal("200.00"), true);
            assertThat(shipping).isEqualByComparingTo("14.99");
        }

        @Test
        void calculateShipping_expressLowOrder_chargesExpressRate() {
            BigDecimal shipping = calculator.calculateShipping(new BigDecimal("10.00"), true);
            assertThat(shipping).isEqualByComparingTo("14.99");
        }

        @Test
        void calculateShipping_withNull_throwsException() {
            assertThatThrownBy(() -> calculator.calculateShipping(null, false))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void calculateShipping_withNegativeAmount_throwsException() {
            assertThatThrownBy(() -> calculator.calculateShipping(new BigDecimal("-1.00"), false))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("applyTierDiscount() Tests")
    class ApplyTierDiscountTests {
        @Test
        void applyTierDiscount_standardTier_noDiscount() {
            BigDecimal result = calculator.applyTierDiscount(new BigDecimal("100.00"), Customer.CustomerTier.STANDARD);
            assertThat(result).isEqualByComparingTo("100.00");
        }

        @Test
        void applyTierDiscount_silverTier_5PercentDiscount() {
            BigDecimal result = calculator.applyTierDiscount(new BigDecimal("100.00"), Customer.CustomerTier.SILVER);
            assertThat(result).isEqualByComparingTo("95.00");
        }

        @Test
        void applyTierDiscount_goldTier_10PercentDiscount() {
            BigDecimal result = calculator.applyTierDiscount(new BigDecimal("100.00"), Customer.CustomerTier.GOLD);
            assertThat(result).isEqualByComparingTo("90.00");
        }

        @Test
        void applyTierDiscount_platinumTier_15PercentDiscount() {
            BigDecimal result = calculator.applyTierDiscount(new BigDecimal("100.00"), Customer.CustomerTier.PLATINUM);
            assertThat(result).isEqualByComparingTo("85.00");
        }

        @Test
        void applyTierDiscount_withNullAmount_throwsException() {
            assertThatThrownBy(() -> calculator.applyTierDiscount(null, Customer.CustomerTier.GOLD))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void applyTierDiscount_withNegativeAmount_throwsException() {
            assertThatThrownBy(() -> calculator.applyTierDiscount(new BigDecimal("-10"), Customer.CustomerTier.GOLD))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("calculateFinalTotal() Tests")
    class CalculateFinalTotalTests {
        @Test
        void calculateFinalTotal_basicScenario() {
            BigDecimal total = calculator.calculateFinalTotal(
                    new BigDecimal("100.00"),
                    new BigDecimal("10.00"),
                    new BigDecimal("7.20"),
                    new BigDecimal("5.99")
            );
            assertThat(total).isEqualByComparingTo("103.19");
        }

        @Test
        void calculateFinalTotal_withNullDiscount_treatsAsZero() {
            BigDecimal total = calculator.calculateFinalTotal(
                    new BigDecimal("100.00"),
                    null,
                    new BigDecimal("8.00"),
                    new BigDecimal("0.00")
            );
            assertThat(total).isEqualByComparingTo("108.00");
        }

        @Test
        void calculateFinalTotal_whenDiscountExceedsSubtotal_returnsZeroOrPositive() {
            BigDecimal total = calculator.calculateFinalTotal(
                    new BigDecimal("10.00"),
                    new BigDecimal("50.00"),
                    BigDecimal.ZERO,
                    BigDecimal.ZERO
            );
            assertThat(total).isGreaterThanOrEqualTo(BigDecimal.ZERO);
        }

        @Test
        void calculateFinalTotal_withFreeShippingAndNoDiscount() {
            BigDecimal total = calculator.calculateFinalTotal(
                    new BigDecimal("50.00"),
                    BigDecimal.ZERO,
                    new BigDecimal("4.00"),
                    BigDecimal.ZERO
            );
            assertThat(total).isEqualByComparingTo("54.00");
        }
    }
}
