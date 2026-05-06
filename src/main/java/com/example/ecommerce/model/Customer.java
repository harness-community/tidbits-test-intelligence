package com.example.ecommerce.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 2, max = 100)
    private String firstName;

    @NotBlank
    @Size(min = 2, max = 100)
    private String lastName;

    @NotBlank
    @Email
    @Column(unique = true)
    private String email;

    @Pattern(regexp = "^[+]?[0-9]{10,15}$")
    private String phoneNumber;

    @Embedded
    private Address shippingAddress;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "street", column = @Column(name = "billing_street")),
        @AttributeOverride(name = "city", column = @Column(name = "billing_city")),
        @AttributeOverride(name = "state", column = @Column(name = "billing_state")),
        @AttributeOverride(name = "zipCode", column = @Column(name = "billing_zip_code")),
        @AttributeOverride(name = "country", column = @Column(name = "billing_country"))
    })
    private Address billingAddress;

    private boolean active = true;

    @Enumerated(EnumType.STRING)
    private CustomerTier tier = CustomerTier.STANDARD;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public enum CustomerTier {
        STANDARD, SILVER, GOLD, PLATINUM
    }
}
