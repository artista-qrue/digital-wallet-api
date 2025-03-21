package com.wallet.api.integration;

import com.wallet.api.entity.Customer;
import com.wallet.api.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class CustomerRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void findByTckn_ShouldReturnCustomer_WhenTcknExists() {
        // Arrange
        Customer customer = new Customer();
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setTckn("12345678901");
        customer.setEmail("john.doe@example.com");
        customer.setPhoneNumber("1234567890");
        customer.setEmployee(false);
        entityManager.persist(customer);
        entityManager.flush();

        // Act
        Optional<Customer> found = customerRepository.findByTckn("12345678901");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("John");
        assertThat(found.get().getLastName()).isEqualTo("Doe");
        assertThat(found.get().getTckn()).isEqualTo("12345678901");
        assertThat(found.get().getEmail()).isEqualTo("john.doe@example.com");
        assertThat(found.get().getPhoneNumber()).isEqualTo("1234567890");
        assertThat(found.get().isEmployee()).isFalse();
    }

    @Test
    void findByTckn_ShouldReturnEmptyOptional_WhenTcknDoesNotExist() {
        // Act
        Optional<Customer> found = customerRepository.findByTckn("nonexistent");

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    void existsByTckn_ShouldReturnTrue_WhenTcknExists() {
        // Arrange
        Customer customer = new Customer();
        customer.setFirstName("Jane");
        customer.setLastName("Smith");
        customer.setTckn("98765432109");
        customer.setEmail("jane.smith@example.com");
        customer.setPhoneNumber("9876543210");
        customer.setEmployee(true);
        entityManager.persist(customer);
        entityManager.flush();

        // Act
        boolean exists = customerRepository.existsByTckn("98765432109");

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void existsByTckn_ShouldReturnFalse_WhenTcknDoesNotExist() {
        // Act
        boolean exists = customerRepository.existsByTckn("nonexistent");

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    void findAll_ShouldReturnAllCustomers() {
        // Arrange
        Customer customer1 = new Customer();
        customer1.setFirstName("John");
        customer1.setLastName("Doe");
        customer1.setTckn("12345678901");
        customer1.setEmail("john.doe@example.com");
        customer1.setPhoneNumber("1234567890");
        customer1.setEmployee(false);
        entityManager.persist(customer1);

        Customer customer2 = new Customer();
        customer2.setFirstName("Jane");
        customer2.setLastName("Smith");
        customer2.setTckn("98765432109");
        customer2.setEmail("jane.smith@example.com");
        customer2.setPhoneNumber("9876543210");
        customer2.setEmployee(true);
        entityManager.persist(customer2);
        
        entityManager.flush();

        // Act
        List<Customer> customers = customerRepository.findAll();

        // Assert
        assertThat(customers).hasSize(2);
        assertThat(customers).extracting(Customer::getTckn)
                .containsExactlyInAnyOrder("12345678901", "98765432109");
    }
} 