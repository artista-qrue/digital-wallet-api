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
        customer.setName("John");
        customer.setSurname("Doe");
        customer.setTckn("12345678901");
        customer.setEmployee(false);
        entityManager.persist(customer);
        entityManager.flush();

        // Act
        Optional<Customer> found = customerRepository.findByTckn("12345678901");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("John");
        assertThat(found.get().getSurname()).isEqualTo("Doe");
        assertThat(found.get().getTckn()).isEqualTo("12345678901");
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
        customer.setName("Jane");
        customer.setSurname("Smith");
        customer.setTckn("98765432109");
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
        customer1.setName("John");
        customer1.setSurname("Doe");
        customer1.setTckn("12345678901");
        customer1.setEmployee(false);
        entityManager.persist(customer1);

        Customer customer2 = new Customer();
        customer2.setName("Jane");
        customer2.setSurname("Smith");
        customer2.setTckn("98765432109");
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