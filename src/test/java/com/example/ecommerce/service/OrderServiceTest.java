package com.example.ecommerce.service;

import com.example.ecommerce.exception.InsufficientStockException;
import com.example.ecommerce.exception.InvalidCouponException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.model.*;
import com.example.ecommerce.repository.CouponRepository;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.util.OrderNumberGenerator;
import com.example.ecommerce.util.PricingCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Tests")
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private ProductRepository productRepository;
    @Mock private CouponRepository couponRepository;
    @Mock private CustomerService customerService;
    @Mock private PricingCalculator pricingCalculator;
    @Mock private OrderNumberGenerator orderNumberGenerator;

    @InjectMocks private OrderService orderService;

    private Customer customer;
    private Product product;
    private Address address;

    @BeforeEach
    void setUp() {
        customer = Customer.builder()
                .id(1L).firstName("John").lastName("Smith")
                .email("john@example.com")
                .tier(Customer.CustomerTier.STANDARD)
                .build();

        product = Product.builder()
                .id(10L).name("Gadget").price(new BigDecimal("25.00"))
                .stockQuantity(20).category("Electronics").active(true).build();

        address = new Address("1 Main St", "Austin", "TX", "78701", "US");

        lenient().when(orderNumberGenerator.generate()).thenReturn("ORD-20240101-001001");
    }

    private void stubPricing() {
        when(pricingCalculator.applyTierDiscount(any(), any())).thenAnswer(i -> i.getArgument(0));
        when(pricingCalculator.calculateTax(any(), anyString())).thenReturn(new BigDecimal("2.00"));
        when(pricingCalculator.calculateShipping(any(), anyBoolean())).thenReturn(new BigDecimal("5.99"));
        when(pricingCalculator.calculateFinalTotal(any(), any(), any(), any()))
                .thenReturn(new BigDecimal("32.99"));
    }

    @Nested
    @DisplayName("createOrder() Tests")
    class CreateOrderTests {
        @Test
        void createOrder_validRequest_createsAndSaves() {
            when(customerService.getCustomerById(1L)).thenReturn(customer);
            when(productRepository.findById(10L)).thenReturn(Optional.of(product));
            when(productRepository.save(any())).thenReturn(product);
            when(orderRepository.save(any())).thenAnswer(i -> i.getArgument(0));
            stubPricing();

            Order result = orderService.createOrder(1L, Map.of(10L, 2), null, address);

            assertThat(result).isNotNull();
            assertThat(result.getOrderNumber()).isEqualTo("ORD-20240101-001001");
            assertThat(result.getStatus()).isEqualTo(Order.OrderStatus.CONFIRMED);
            verify(productRepository).save(product);
            assertThat(product.getStockQuantity()).isEqualTo(18);
        }

        @Test
        void createOrder_insufficientStock_throwsException() {
            when(customerService.getCustomerById(1L)).thenReturn(customer);
            product.setStockQuantity(1);
            when(productRepository.findById(10L)).thenReturn(Optional.of(product));

            assertThatThrownBy(() -> orderService.createOrder(1L, Map.of(10L, 5), null, address))
                    .isInstanceOf(InsufficientStockException.class);
        }

        @Test
        void createOrder_productNotFound_throwsException() {
            when(customerService.getCustomerById(1L)).thenReturn(customer);
            when(productRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.createOrder(1L, Map.of(99L, 1), null, address))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void createOrder_withValidCoupon_appliesDiscount() {
            Coupon coupon = Coupon.builder()
                    .code("SAVE10")
                    .discountType(Coupon.DiscountType.PERCENTAGE)
                    .discountValue(new BigDecimal("10"))
                    .active(true)
                    .usageCount(0)
                    .build();

            when(customerService.getCustomerById(1L)).thenReturn(customer);
            when(productRepository.findById(10L)).thenReturn(Optional.of(product));
            when(productRepository.save(any())).thenReturn(product);
            when(couponRepository.findByCode("SAVE10")).thenReturn(Optional.of(coupon));
            when(orderRepository.save(any())).thenAnswer(i -> i.getArgument(0));
            stubPricing();

            Order result = orderService.createOrder(1L, Map.of(10L, 1), "SAVE10", address);
            assertThat(result.getCouponCode()).isEqualTo("SAVE10");
            assertThat(coupon.getUsageCount()).isEqualTo(1);
        }

        @Test
        void createOrder_withInvalidCoupon_throwsException() {
            when(customerService.getCustomerById(1L)).thenReturn(customer);
            when(productRepository.findById(10L)).thenReturn(Optional.of(product));
            when(productRepository.save(any())).thenReturn(product);
            when(couponRepository.findByCode("BADCODE")).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    orderService.createOrder(1L, Map.of(10L, 1), "BADCODE", address))
                    .isInstanceOf(InvalidCouponException.class);
        }

        @Test
        void createOrder_withExpiredCoupon_throwsException() {
            Coupon expired = Coupon.builder()
                    .code("OLD")
                    .discountType(Coupon.DiscountType.PERCENTAGE)
                    .discountValue(new BigDecimal("10"))
                    .minimumOrderAmount(BigDecimal.ZERO)
                    .active(true)
                    .validUntil(LocalDateTime.now().minusDays(1))
                    .usageCount(0)
                    .build();

            when(customerService.getCustomerById(1L)).thenReturn(customer);
            when(productRepository.findById(10L)).thenReturn(Optional.of(product));
            when(productRepository.save(any())).thenReturn(product);
            when(couponRepository.findByCode("OLD")).thenReturn(Optional.of(expired));

            assertThatThrownBy(() ->
                    orderService.createOrder(1L, Map.of(10L, 1), "OLD", address))
                    .isInstanceOf(InvalidCouponException.class);
        }
    }

    @Nested
    @DisplayName("cancelOrder() Tests")
    class CancelOrderTests {
        @Test
        void cancelOrder_pendingOrder_cancelsAndRestoresStock() {
            OrderItem item = OrderItem.builder().product(product).quantity(3).build();
            Order order = Order.builder().id(1L).status(Order.OrderStatus.PENDING)
                    .items(new ArrayList<>(List.of(item))).build();

            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(productRepository.save(any())).thenReturn(product);
            when(orderRepository.save(any())).thenReturn(order);

            Order cancelled = orderService.cancelOrder(1L);

            assertThat(cancelled.getStatus()).isEqualTo(Order.OrderStatus.CANCELLED);
            assertThat(product.getStockQuantity()).isEqualTo(23);
        }

        @Test
        void cancelOrder_confirmedOrder_cancels() {
            Order order = Order.builder().id(1L).status(Order.OrderStatus.CONFIRMED)
                    .items(new ArrayList<>()).build();
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(orderRepository.save(any())).thenReturn(order);

            Order cancelled = orderService.cancelOrder(1L);
            assertThat(cancelled.getStatus()).isEqualTo(Order.OrderStatus.CANCELLED);
        }

        @Test
        void cancelOrder_shippedOrder_throwsException() {
            Order order = Order.builder().id(1L).status(Order.OrderStatus.SHIPPED)
                    .items(new ArrayList<>()).build();
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> orderService.cancelOrder(1L))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        void cancelOrder_deliveredOrder_throwsException() {
            Order order = Order.builder().id(1L).status(Order.OrderStatus.DELIVERED)
                    .items(new ArrayList<>()).build();
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> orderService.cancelOrder(1L))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("shipOrder() Tests")
    class ShipOrderTests {
        @Test
        void shipOrder_confirmedOrder_setsShippedStatus() {
            Order order = Order.builder().id(1L).status(Order.OrderStatus.CONFIRMED).build();
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(orderRepository.save(any())).thenReturn(order);

            Order shipped = orderService.shipOrder(1L);
            assertThat(shipped.getStatus()).isEqualTo(Order.OrderStatus.SHIPPED);
        }

        @Test
        void shipOrder_pendingOrder_throwsException() {
            Order order = Order.builder().id(1L).status(Order.OrderStatus.PENDING).build();
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> orderService.shipOrder(1L))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("deliverOrder() Tests")
    class DeliverOrderTests {
        @Test
        void deliverOrder_shippedOrder_setsDeliveredStatus() {
            Order order = Order.builder().id(1L).status(Order.OrderStatus.SHIPPED).build();
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(orderRepository.save(any())).thenReturn(order);

            Order delivered = orderService.deliverOrder(1L);
            assertThat(delivered.getStatus()).isEqualTo(Order.OrderStatus.DELIVERED);
        }

        @Test
        void deliverOrder_nonShippedOrder_throwsException() {
            Order order = Order.builder().id(1L).status(Order.OrderStatus.CONFIRMED).build();
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> orderService.deliverOrder(1L))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
