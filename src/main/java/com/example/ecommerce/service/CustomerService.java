package com.example.ecommerce.service;

import com.example.ecommerce.exception.DuplicateEmailException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.model.Customer;
import com.example.ecommerce.repository.CustomerRepository;
import com.example.ecommerce.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;

    public Customer registerCustomer(Customer customer) {
        if (customerRepository.existsByEmail(customer.getEmail())) {
            throw new DuplicateEmailException(customer.getEmail());
        }
        return customerRepository.save(customer);
    }

    @Transactional(readOnly = true)
    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
    }

    @Transactional(readOnly = true)
    public Customer getCustomerByEmail(String email) {
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with email: " + email));
    }

    public Customer updateCustomer(Long id, Customer updated) {
        Customer existing = getCustomerById(id);
        if (!existing.getEmail().equals(updated.getEmail())
                && customerRepository.existsByEmail(updated.getEmail())) {
            throw new DuplicateEmailException(updated.getEmail());
        }
        existing.setFirstName(updated.getFirstName());
        existing.setLastName(updated.getLastName());
        existing.setEmail(updated.getEmail());
        existing.setPhoneNumber(updated.getPhoneNumber());
        existing.setShippingAddress(updated.getShippingAddress());
        existing.setBillingAddress(updated.getBillingAddress());
        return customerRepository.save(existing);
    }

    public Customer upgradeTier(Long customerId, Customer.CustomerTier newTier) {
        Customer customer = getCustomerById(customerId);
        customer.setTier(newTier);
        return customerRepository.save(customer);
    }

    public void evaluateAndUpgradeTier(Long customerId) {
        Customer customer = getCustomerById(customerId);
        long orderCount = orderRepository.findByCustomerId(customerId).size();
        Customer.CustomerTier newTier;
        if (orderCount >= 50) {
            newTier = Customer.CustomerTier.PLATINUM;
        } else if (orderCount >= 20) {
            newTier = Customer.CustomerTier.GOLD;
        } else if (orderCount >= 5) {
            newTier = Customer.CustomerTier.SILVER;
        } else {
            newTier = Customer.CustomerTier.STANDARD;
        }
        customer.setTier(newTier);
        customerRepository.save(customer);
    }

    public void deactivateCustomer(Long id) {
        Customer customer = getCustomerById(id);
        customer.setActive(false);
        customerRepository.save(customer);
    }

    @Transactional(readOnly = true)
    public List<Customer> getAllActiveCustomers() {
        return customerRepository.findByActiveTrue();
    }
}
