package com.example.ecommerce.service;

import com.example.ecommerce.exception.DuplicateEmailException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.model.Customer;
import com.example.ecommerce.model.Order;
import com.example.ecommerce.repository.CustomerRepository;
import com.example.ecommerce.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerService Tests")
class CustomerServiceTest {

    @Mock private CustomerRepository customerRepository;
    @Mock private OrderRepository orderRepository;

    @InjectMocks private CustomerService customerService;

    private Customer sampleCustomer;

    @BeforeEach
    void setUp() {
        sampleCustomer = Customer.builder()
                .id(1L)
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@example.com")
                .tier(Customer.CustomerTier.STANDARD)
                .active(true)
                .build();
    }

    @Nested
    @DisplayName("registerCustomer() Tests")
    class RegisterCustomerTests {
        @Test
        void registerCustomer_newEmail_savesAndReturns() {
            when(customerRepository.existsByEmail(anyString())).thenReturn(false);
            when(customerRepository.save(any())).thenReturn(sampleCustomer);
            Customer registered = customerService.registerCustomer(sampleCustomer);
            assertThat(registered).isEqualTo(sampleCustomer);
        }

        @Test
        void registerCustomer_duplicateEmail_throwsException() {
            when(customerRepository.existsByEmail("jane.doe@example.com")).thenReturn(true);
            assertThatThrownBy(() -> customerService.registerCustomer(sampleCustomer))
                    .isInstanceOf(DuplicateEmailException.class)
                    .hasMessageContaining("jane.doe@example.com");
        }
    }

    @Nested
    @DisplayName("getCustomerById() Tests")
    class GetCustomerByIdTests {
        @Test
        void getCustomerById_exists_returnsCustomer() {
            when(customerRepository.findById(1L)).thenReturn(Optional.of(sampleCustomer));
            assertThat(customerService.getCustomerById(1L)).isEqualTo(sampleCustomer);
        }

        @Test
        void getCustomerById_notFound_throwsException() {
            when(customerRepository.findById(99L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> customerService.getCustomerById(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getCustomerByEmail() Tests")
    class GetCustomerByEmailTests {
        @Test
        void getCustomerByEmail_found_returnsCustomer() {
            when(customerRepository.findByEmail("jane.doe@example.com"))
                    .thenReturn(Optional.of(sampleCustomer));
            assertThat(customerService.getCustomerByEmail("jane.doe@example.com"))
                    .isEqualTo(sampleCustomer);
        }

        @Test
        void getCustomerByEmail_notFound_throwsException() {
            when(customerRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());
            assertThatThrownBy(() -> customerService.getCustomerByEmail("unknown@example.com"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("evaluateAndUpgradeTier() Tests")
    class EvaluateAndUpgradeTierTests {
        @Test
        void evaluateAndUpgradeTier_fewOrders_remainsStandard() {
            when(customerRepository.findById(1L)).thenReturn(Optional.of(sampleCustomer));
            when(orderRepository.findByCustomerId(1L)).thenReturn(List.of());
            when(customerRepository.save(any())).thenReturn(sampleCustomer);
            customerService.evaluateAndUpgradeTier(1L);
            assertThat(sampleCustomer.getTier()).isEqualTo(Customer.CustomerTier.STANDARD);
        }

        @Test
        void evaluateAndUpgradeTier_fiveOrders_upgradestoSilver() {
            when(customerRepository.findById(1L)).thenReturn(Optional.of(sampleCustomer));
            List<Order> orders = List.of(new Order(), new Order(), new Order(), new Order(), new Order());
            when(orderRepository.findByCustomerId(1L)).thenReturn(orders);
            when(customerRepository.save(any())).thenReturn(sampleCustomer);
            customerService.evaluateAndUpgradeTier(1L);
            assertThat(sampleCustomer.getTier()).isEqualTo(Customer.CustomerTier.SILVER);
        }

        @Test
        void evaluateAndUpgradeTier_twentyOrders_upgradesToGold() {
            when(customerRepository.findById(1L)).thenReturn(Optional.of(sampleCustomer));
            List<Order> orders = java.util.Collections.nCopies(20, new Order());
            when(orderRepository.findByCustomerId(1L)).thenReturn(orders);
            when(customerRepository.save(any())).thenReturn(sampleCustomer);
            customerService.evaluateAndUpgradeTier(1L);
            assertThat(sampleCustomer.getTier()).isEqualTo(Customer.CustomerTier.GOLD);
        }

        @Test
        void evaluateAndUpgradeTier_fiftyOrders_upgradesToPlatinum() {
            when(customerRepository.findById(1L)).thenReturn(Optional.of(sampleCustomer));
            List<Order> orders = java.util.Collections.nCopies(50, new Order());
            when(orderRepository.findByCustomerId(1L)).thenReturn(orders);
            when(customerRepository.save(any())).thenReturn(sampleCustomer);
            customerService.evaluateAndUpgradeTier(1L);
            assertThat(sampleCustomer.getTier()).isEqualTo(Customer.CustomerTier.PLATINUM);
        }
    }

    @Nested
    @DisplayName("deactivateCustomer() Tests")
    class DeactivateCustomerTests {
        @Test
        void deactivateCustomer_setsActiveFalse() {
            when(customerRepository.findById(1L)).thenReturn(Optional.of(sampleCustomer));
            when(customerRepository.save(any())).thenReturn(sampleCustomer);
            customerService.deactivateCustomer(1L);
            assertThat(sampleCustomer.isActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("updateCustomer() Tests")
    class UpdateCustomerTests {
        @Test
        void updateCustomer_sameEmail_updatesSuccessfully() {
            Customer updated = Customer.builder()
                    .firstName("Janet")
                    .lastName("Doe")
                    .email("jane.doe@example.com")
                    .build();
            when(customerRepository.findById(1L)).thenReturn(Optional.of(sampleCustomer));
            when(customerRepository.save(any())).thenReturn(sampleCustomer);
            assertThatNoException().isThrownBy(() -> customerService.updateCustomer(1L, updated));
        }

        @Test
        void updateCustomer_newEmailAlreadyTaken_throwsException() {
            Customer updated = Customer.builder()
                    .firstName("Janet")
                    .lastName("Doe")
                    .email("other@example.com")
                    .build();
            when(customerRepository.findById(1L)).thenReturn(Optional.of(sampleCustomer));
            when(customerRepository.existsByEmail("other@example.com")).thenReturn(true);
            assertThatThrownBy(() -> customerService.updateCustomer(1L, updated))
                    .isInstanceOf(DuplicateEmailException.class);
        }
    }
}
