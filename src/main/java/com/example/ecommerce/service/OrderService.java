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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;
    private final CustomerService customerService;
    private final PricingCalculator pricingCalculator;
    private final OrderNumberGenerator orderNumberGenerator;

    public Order createOrder(Long customerId, Map<Long, Integer> productQuantities,
                             String couponCode, Address shippingAddress) {
        Customer customer = customerService.getCustomerById(customerId);

        Order order = Order.builder()
                .orderNumber(orderNumberGenerator.generate())
                .customer(customer)
                .shippingAddress(shippingAddress)
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;

        for (Map.Entry<Long, Integer> entry : productQuantities.entrySet()) {
            Product product = productRepository.findById(entry.getKey())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", entry.getKey()));
            int qty = entry.getValue();

            if (!product.isInStock(qty)) {
                throw new InsufficientStockException(product.getName(), qty, product.getStockQuantity());
            }

            product.decreaseStock(qty);
            productRepository.save(product);

            OrderItem item = OrderItem.builder()
                    .product(product)
                    .quantity(qty)
                    .unitPrice(product.getPrice())
                    .build();

            order.addItem(item);
            subtotal = subtotal.add(item.getLineTotal());
        }

        order.setSubtotal(subtotal);

        BigDecimal discount = BigDecimal.ZERO;
        if (couponCode != null && !couponCode.isBlank()) {
            Coupon coupon = couponRepository.findByCode(couponCode)
                    .orElseThrow(() -> new InvalidCouponException("Coupon not found: " + couponCode));
            if (!coupon.isValidForOrderAmount(subtotal)) {
                throw new InvalidCouponException("Coupon is not valid for this order");
            }
            discount = coupon.calculateDiscount(subtotal);
            coupon.incrementUsage();
            order.setCouponCode(couponCode);
        }

        // Apply tier discount on top
        BigDecimal afterDiscount = subtotal.subtract(discount);
        BigDecimal tierDiscounted = pricingCalculator.applyTierDiscount(afterDiscount, customer.getTier());
        BigDecimal tierDiscount = afterDiscount.subtract(tierDiscounted);

        BigDecimal totalDiscount = discount.add(tierDiscount);
        String country = shippingAddress != null ? shippingAddress.getCountry() : "US";
        BigDecimal tax = pricingCalculator.calculateTax(tierDiscounted, country);
        BigDecimal shipping = pricingCalculator.calculateShipping(tierDiscounted, false);
        BigDecimal total = pricingCalculator.calculateFinalTotal(subtotal, totalDiscount, tax, shipping);

        order.setDiscountAmount(totalDiscount);
        order.setTaxAmount(tax);
        order.setShippingCost(shipping);
        order.setTotalAmount(total);
        order.setStatus(Order.OrderStatus.CONFIRMED);

        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }

    @Transactional(readOnly = true)
    public Order getOrderByNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderNumber));
    }

    public Order cancelOrder(Long orderId) {
        Order order = getOrderById(orderId);
        if (!order.canBeCancelled()) {
            throw new IllegalStateException("Order cannot be cancelled in status: " + order.getStatus());
        }
        // Restore stock
        for (OrderItem item : order.getItems()) {
            item.getProduct().increaseStock(item.getQuantity());
            productRepository.save(item.getProduct());
        }
        order.setStatus(Order.OrderStatus.CANCELLED);
        return orderRepository.save(order);
    }

    public Order shipOrder(Long orderId) {
        Order order = getOrderById(orderId);
        if (!order.canBeShipped()) {
            throw new IllegalStateException("Order cannot be shipped in status: " + order.getStatus());
        }
        order.setStatus(Order.OrderStatus.SHIPPED);
        return orderRepository.save(order);
    }

    public Order deliverOrder(Long orderId) {
        Order order = getOrderById(orderId);
        if (order.getStatus() != Order.OrderStatus.SHIPPED) {
            throw new IllegalStateException("Order must be shipped before it can be delivered");
        }
        order.setStatus(Order.OrderStatus.DELIVERED);
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByCustomer(Long customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByStatus(Order.OrderStatus status) {
        return orderRepository.findByStatus(status);
    }
}
