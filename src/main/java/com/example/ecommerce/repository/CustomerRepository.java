package com.example.ecommerce.repository;

import com.example.ecommerce.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Customer> findByActiveTrue();
    List<Customer> findByTier(Customer.CustomerTier tier);
    List<Customer> findByFirstNameIgnoreCaseAndLastNameIgnoreCase(String firstName, String lastName);
}
