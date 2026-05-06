package com.example.ecommerce.model;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @NotBlank
    private String street;

    @NotBlank
    private String city;

    @NotBlank
    private String state;

    @NotBlank
    @Pattern(regexp = "^[0-9]{5}(-[0-9]{4})?$")
    private String zipCode;

    @NotBlank
    private String country;

    public String getFormattedAddress() {
        return street + ", " + city + ", " + state + " " + zipCode + ", " + country;
    }

    public boolean isUSAddress() {
        return "US".equalsIgnoreCase(country) || "USA".equalsIgnoreCase(country)
                || "United States".equalsIgnoreCase(country);
    }
}
