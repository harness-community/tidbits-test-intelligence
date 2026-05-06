package com.example.ecommerce.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Product Model Tests")
class ProductTest {

    private Product product;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(1L)
                .name("Test Product")
                .price(new BigDecimal("29.99"))
                .stockQuantity(10)
                .category("Electronics")
                .sku("SKU-001")
                .active(true)
                .build();
    }

    @Nested
    @DisplayName("isInStock() Tests")
    class IsInStockTests {
        @Test
        void isInStock_whenStockIsPositive_returnsTrue() {
            assertThat(product.isInStock()).isTrue();
        }

        @Test
        void isInStock_whenStockIsZero_returnsFalse() {
            product.setStockQuantity(0);
            assertThat(product.isInStock()).isFalse();
        }

        @Test
        void isInStock_whenStockIsNull_returnsFalse() {
            product.setStockQuantity(null);
            assertThat(product.isInStock()).isFalse();
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 5, 10})
        void isInStock_withRequiredQty_whenSufficient_returnsTrue(int qty) {
            assertThat(product.isInStock(qty)).isTrue();
        }

        @Test
        void isInStock_withRequiredQty_whenInsufficient_returnsFalse() {
            assertThat(product.isInStock(11)).isFalse();
        }

        @Test
        void isInStock_withRequiredQtyEqualToStock_returnsTrue() {
            assertThat(product.isInStock(10)).isTrue();
        }
    }

    @Nested
    @DisplayName("decreaseStock() Tests")
    class DecreaseStockTests {
        @Test
        void decreaseStock_byValidAmount_decreasesCorrectly() {
            product.decreaseStock(3);
            assertThat(product.getStockQuantity()).isEqualTo(7);
        }

        @Test
        void decreaseStock_byFullStock_setsToZero() {
            product.decreaseStock(10);
            assertThat(product.getStockQuantity()).isEqualTo(0);
        }

        @Test
        void decreaseStock_byZero_throwsException() {
            assertThatThrownBy(() -> product.decreaseStock(0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void decreaseStock_byNegative_throwsException() {
            assertThatThrownBy(() -> product.decreaseStock(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void decreaseStock_exceedingStock_throwsException() {
            assertThatThrownBy(() -> product.decreaseStock(11))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("increaseStock() Tests")
    class IncreaseStockTests {
        @Test
        void increaseStock_byValidAmount_increasesCorrectly() {
            product.increaseStock(5);
            assertThat(product.getStockQuantity()).isEqualTo(15);
        }

        @Test
        void increaseStock_byZero_throwsException() {
            assertThatThrownBy(() -> product.increaseStock(0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void increaseStock_byNegative_throwsException() {
            assertThatThrownBy(() -> product.increaseStock(-5))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void increaseStock_fromZeroStock_increasesCorrectly() {
            product.setStockQuantity(0);
            product.increaseStock(10);
            assertThat(product.getStockQuantity()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {
        @Test
        void builder_createsProductWithAllFields() {
            assertThat(product.getName()).isEqualTo("Test Product");
            assertThat(product.getPrice()).isEqualByComparingTo("29.99");
            assertThat(product.getCategory()).isEqualTo("Electronics");
            assertThat(product.getSku()).isEqualTo("SKU-001");
            assertThat(product.isActive()).isTrue();
        }

        @Test
        void builder_defaultsActive_toTrue() {
            Product p = Product.builder().name("X").price(BigDecimal.TEN)
                    .stockQuantity(5).category("Cat").build();
            assertThat(p.isActive()).isTrue();
        }
    }
}
