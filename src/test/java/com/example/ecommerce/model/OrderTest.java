package com.example.ecommerce.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Order Model Tests")
class OrderTest {

    private Order order;
    private Product product;

    @BeforeEach
    void setUp() {
        order = Order.builder()
                .id(1L)
                .orderNumber("ORD-20240101-001000")
                .status(Order.OrderStatus.PENDING)
                .build();

        product = Product.builder()
                .id(1L)
                .name("Widget")
                .stockQuantity(100)
                .build();
    }

    @Nested
    @DisplayName("addItem() / removeItem() Tests")
    class ItemManagementTests {
        @Test
        void addItem_setsOrderReference() {
            OrderItem item = OrderItem.builder().product(product).quantity(2).build();
            order.addItem(item);
            assertThat(item.getOrder()).isEqualTo(order);
            assertThat(order.getItems()).contains(item);
        }

        @Test
        void removeItem_clearsOrderReference() {
            OrderItem item = OrderItem.builder().product(product).quantity(2).build();
            order.addItem(item);
            order.removeItem(item);
            assertThat(item.getOrder()).isNull();
            assertThat(order.getItems()).doesNotContain(item);
        }

        @Test
        void getTotalItemCount_sumsQuantities() {
            order.addItem(OrderItem.builder().product(product).quantity(3).build());
            order.addItem(OrderItem.builder().product(product).quantity(2).build());
            assertThat(order.getTotalItemCount()).isEqualTo(5);
        }

        @Test
        void getTotalItemCount_emptyOrder_returnsZero() {
            assertThat(order.getTotalItemCount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("canBeCancelled() Tests")
    class CancellationTests {
        @Test
        void canBeCancelled_whenPending_returnsTrue() {
            order.setStatus(Order.OrderStatus.PENDING);
            assertThat(order.canBeCancelled()).isTrue();
        }

        @Test
        void canBeCancelled_whenConfirmed_returnsTrue() {
            order.setStatus(Order.OrderStatus.CONFIRMED);
            assertThat(order.canBeCancelled()).isTrue();
        }

        @ParameterizedTest
        @EnumSource(value = Order.OrderStatus.class, names = {"PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED", "REFUNDED"})
        void canBeCancelled_whenNotCancellable_returnsFalse(Order.OrderStatus status) {
            order.setStatus(status);
            assertThat(order.canBeCancelled()).isFalse();
        }
    }

    @Nested
    @DisplayName("canBeShipped() Tests")
    class ShippingTests {
        @Test
        void canBeShipped_whenConfirmed_returnsTrue() {
            order.setStatus(Order.OrderStatus.CONFIRMED);
            assertThat(order.canBeShipped()).isTrue();
        }

        @ParameterizedTest
        @EnumSource(value = Order.OrderStatus.class, names = {"PENDING", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED", "REFUNDED"})
        void canBeShipped_whenNotConfirmed_returnsFalse(Order.OrderStatus status) {
            order.setStatus(status);
            assertThat(order.canBeShipped()).isFalse();
        }
    }
}
