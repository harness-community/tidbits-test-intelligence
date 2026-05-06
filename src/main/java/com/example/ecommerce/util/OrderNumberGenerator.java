package com.example.ecommerce.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class OrderNumberGenerator {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final AtomicLong counter = new AtomicLong(1000);

    public String generate() {
        String datePart = LocalDateTime.now().format(FORMATTER);
        long seq = counter.incrementAndGet();
        return "ORD-" + datePart + "-" + String.format("%06d", seq);
    }

    public boolean isValidFormat(String orderNumber) {
        if (orderNumber == null || orderNumber.isBlank()) return false;
        return orderNumber.matches("ORD-\\d{8}-\\d{6}");
    }

    public String extractDate(String orderNumber) {
        if (!isValidFormat(orderNumber)) {
            throw new IllegalArgumentException("Invalid order number format: " + orderNumber);
        }
        return orderNumber.split("-")[1];
    }
}
