package com.example.ecommerce.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Address Model Tests")
class AddressTest {

    private Address buildAddress(String street, String city, String state, String zip, String country) {
        return new Address(street, city, state, zip, country);
    }

    @Nested
    @DisplayName("getFormattedAddress() Tests")
    class FormattedAddressTests {
        @Test
        void getFormattedAddress_returnsCorrectFormat() {
            Address address = buildAddress("123 Main St", "Springfield", "IL", "62701", "US");
            assertThat(address.getFormattedAddress())
                    .isEqualTo("123 Main St, Springfield, IL 62701, US");
        }

        @Test
        void getFormattedAddress_withLongStreet_includesFullStreet() {
            Address address = buildAddress("9999 Longstreet Boulevard Apt 42", "Metropolis", "NY", "10001", "US");
            assertThat(address.getFormattedAddress()).contains("9999 Longstreet Boulevard Apt 42");
        }
    }

    @Nested
    @DisplayName("isUSAddress() Tests")
    class IsUSAddressTests {
        @ParameterizedTest
        @ValueSource(strings = {"US", "us", "USA", "usa", "United States", "united states"})
        void isUSAddress_withVariousUSFormats_returnsTrue(String country) {
            Address address = buildAddress("123 St", "City", "ST", "12345", country);
            assertThat(address.isUSAddress()).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {"CA", "Canada", "UK", "Germany", "FR"})
        void isUSAddress_withNonUSCountry_returnsFalse(String country) {
            Address address = buildAddress("123 St", "City", "ST", "12345", country);
            assertThat(address.isUSAddress()).isFalse();
        }
    }
}
