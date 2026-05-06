package com.example.ecommerce.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("OrderItem Model Tests")
class OrderItemTest {

    private OrderItem buildItem(String unitPrice, int qty, String discountPercent) {
        return OrderItem.builder()
                .unitPrice(new BigDecimal(unitPrice))
                .quantity(qty)
                .discountPercent(discountPercent != null ? new BigDecimal(discountPercent) : null)
                .build();
    }

    @Nested
    @DisplayName("getLineTotal() Tests")
    class LineTotalTests {
        @Test
        void getLineTotal_withNoDiscount_returnsUnitPriceTimesQty() {
            OrderItem item = buildItem("10.00", 3, null);
            assertThat(item.getLineTotal()).isEqualByComparingTo("30.00");
        }

        @Test
        void getLineTotal_withZeroDiscount_returnsFullPrice() {
            OrderItem item = buildItem("10.00", 3, "0");
            assertThat(item.getLineTotal()).isEqualByComparingTo("30.00");
        }

        @Test
        void getLineTotal_with10PercentDiscount_returnsDiscountedPrice() {
            OrderItem item = buildItem("100.00", 2, "10");
            assertThat(item.getLineTotal()).isEqualByComparingTo("180.00");
        }

        @Test
        void getLineTotal_with50PercentDiscount_returnsHalfPrice() {
            OrderItem item = buildItem("20.00", 1, "50");
            assertThat(item.getLineTotal()).isEqualByComparingTo("10.00");
        }

        @ParameterizedTest
        @CsvSource({
                "10.00, 1, 10, 9.00",
                "10.00, 2, 10, 18.00",
                "50.00, 3, 20, 120.00",
                "100.00, 1, 100, 0.00"
        })
        void getLineTotal_parameterized(String price, int qty, String discount, String expected) {
            OrderItem item = buildItem(price, qty, discount);
            assertThat(item.getLineTotal()).isEqualByComparingTo(expected);
        }
    }

    @Nested
    @DisplayName("getDiscountAmount() Tests")
    class DiscountAmountTests {
        @Test
        void getDiscountAmount_withNoDiscount_returnsZero() {
            OrderItem item = buildItem("100.00", 1, null);
            assertThat(item.getDiscountAmount()).isEqualByComparingTo("0.00");
        }

        @Test
        void getDiscountAmount_with25Percent_returnsCorrectAmount() {
            OrderItem item = buildItem("100.00", 2, "25");
            assertThat(item.getDiscountAmount()).isEqualByComparingTo("50.00");
        }

        @Test
        void getDiscountAmount_withZeroDiscount_returnsZero() {
            OrderItem item = buildItem("100.00", 1, "0");
            assertThat(item.getDiscountAmount()).isEqualByComparingTo("0.00");
        }
    }
}
