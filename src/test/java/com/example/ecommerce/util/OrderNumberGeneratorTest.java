package com.example.ecommerce.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.*;

@DisplayName("OrderNumberGenerator Tests")
class OrderNumberGeneratorTest {

    private OrderNumberGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new OrderNumberGenerator();
    }

    @Nested
    @DisplayName("generate() Tests")
    class GenerateTests {
        @Test
        void generate_returnsNonNull() {
            assertThat(generator.generate()).isNotNull();
        }

        @Test
        void generate_matchesExpectedPattern() {
            String orderNumber = generator.generate();
            assertThat(orderNumber).matches("ORD-\\d{8}-\\d{6}");
        }

        @Test
        void generate_containsTodayDate() {
            String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String orderNumber = generator.generate();
            assertThat(orderNumber).contains(today);
        }

        @Test
        void generate_producesUniqueNumbers() {
            String first = generator.generate();
            String second = generator.generate();
            assertThat(first).isNotEqualTo(second);
        }

        @Test
        void generate_sequenceIncreases() {
            String first = generator.generate();
            String second = generator.generate();
            long seq1 = Long.parseLong(first.split("-")[2]);
            long seq2 = Long.parseLong(second.split("-")[2]);
            assertThat(seq2).isGreaterThan(seq1);
        }
    }

    @Nested
    @DisplayName("isValidFormat() Tests")
    class IsValidFormatTests {
        @Test
        void isValidFormat_withValidNumber_returnsTrue() {
            assertThat(generator.isValidFormat("ORD-20240101-001001")).isTrue();
        }

        @Test
        void isValidFormat_withNull_returnsFalse() {
            assertThat(generator.isValidFormat(null)).isFalse();
        }

        @Test
        void isValidFormat_withBlank_returnsFalse() {
            assertThat(generator.isValidFormat("   ")).isFalse();
        }

        @ParameterizedTest
        @ValueSource(strings = {"ORD-2024-001", "ORDER-20240101-001001", "20240101-001001", "ORD-ABCD0101-001001"})
        void isValidFormat_withInvalidFormats_returnsFalse(String orderNumber) {
            assertThat(generator.isValidFormat(orderNumber)).isFalse();
        }
    }

    @Nested
    @DisplayName("extractDate() Tests")
    class ExtractDateTests {
        @Test
        void extractDate_validOrderNumber_returnsDatePart() {
            assertThat(generator.extractDate("ORD-20240315-001001")).isEqualTo("20240315");
        }

        @Test
        void extractDate_invalidFormat_throwsException() {
            assertThatThrownBy(() -> generator.extractDate("INVALID"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
