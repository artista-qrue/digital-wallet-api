package com.wallet.api.service;

import com.wallet.api.dto.CustomerDto;
import com.wallet.api.entity.Customer;
import com.wallet.api.exception.ResourceNotFoundException;
import com.wallet.api.repository.CustomerRepository;
import com.wallet.api.service.impl.CustomerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private Customer customer;
    private CustomerDto customerDto;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setName("John");
        customer.setSurname("Doe");
        customer.setTckn("12345678901");
        customer.setEmployee(true);

        customerDto = new CustomerDto(
                1L,
                "John",
                "Doe",
                "12345678901",
                true
        );
    }

    @Test
    void createCustomer_ShouldCreateCustomer_WhenValidInput() {
        // Arrange
        when(customerRepository.existsByTckn(anyString())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        // Act
        CustomerDto result = customerService.createCustomer(customerDto);

        // Assert
        assertNotNull(result);
        assertEquals(customerDto.id(), result.id());
        assertEquals(customerDto.name(), result.name());
        assertEquals(customerDto.surname(), result.surname());
        assertEquals(customerDto.tckn(), result.tckn());
        assertEquals(customerDto.isEmployee(), result.isEmployee());

        verify(customerRepository, times(1)).existsByTckn(customerDto.tckn());
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    void createCustomer_ShouldThrowIllegalArgumentException_WhenTcknExists() {
        // Arrange
        when(customerRepository.existsByTckn(anyString())).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> customerService.createCustomer(customerDto));

        assertEquals("Customer with TCKN " + customerDto.tckn() + " already exists", exception.getMessage());
        verify(customerRepository, times(1)).existsByTckn(customerDto.tckn());
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void getCustomerById_ShouldReturnCustomer_WhenIdExists() {
        // Arrange
        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(customer));

        // Act
        CustomerDto result = customerService.getCustomerById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(customerDto.id(), result.id());
        assertEquals(customerDto.name(), result.name());
        assertEquals(customerDto.surname(), result.surname());
        assertEquals(customerDto.tckn(), result.tckn());
        assertEquals(customerDto.isEmployee(), result.isEmployee());

        verify(customerRepository, times(1)).findById(1L);
    }

    @Test
    void getCustomerById_ShouldThrowResourceNotFoundException_WhenIdDoesNotExist() {
        // Arrange
        when(customerRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> customerService.getCustomerById(1L));

        assertEquals("Customer not found with id: '1'", exception.getMessage());
        verify(customerRepository, times(1)).findById(1L);
    }

    @Test
    void getCustomerByTckn_ShouldReturnCustomer_WhenTcknExists() {
        // Arrange
        when(customerRepository.findByTckn(anyString())).thenReturn(Optional.of(customer));

        // Act
        CustomerDto result = customerService.getCustomerByTckn("12345678901");

        // Assert
        assertNotNull(result);
        assertEquals(customerDto.id(), result.id());
        assertEquals(customerDto.name(), result.name());
        assertEquals(customerDto.surname(), result.surname());
        assertEquals(customerDto.tckn(), result.tckn());
        assertEquals(customerDto.isEmployee(), result.isEmployee());

        verify(customerRepository, times(1)).findByTckn("12345678901");
    }

    @Test
    void getCustomerByTckn_ShouldThrowResourceNotFoundException_WhenTcknDoesNotExist() {
        // Arrange
        when(customerRepository.findByTckn(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> customerService.getCustomerByTckn("12345678901"));

        assertEquals("Customer not found with tckn: '12345678901'", exception.getMessage());
        verify(customerRepository, times(1)).findByTckn("12345678901");
    }

    @Test
    void getAllCustomers_ShouldReturnAllCustomers() {
        // Arrange
        Customer customer2 = new Customer();
        customer2.setId(2L);
        customer2.setName("Jane");
        customer2.setSurname("Doe");
        customer2.setTckn("98765432109");
        customer2.setEmployee(false);

        when(customerRepository.findAll()).thenReturn(Arrays.asList(customer, customer2));

        // Act
        List<CustomerDto> results = customerService.getAllCustomers();

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals(customer.getId(), results.get(0).id());
        assertEquals(customer2.getId(), results.get(1).id());

        verify(customerRepository, times(1)).findAll();
    }

    @Test
    void isEmployee_ShouldReturnTrue_WhenCustomerIsEmployee() {
        // Arrange
        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(customer));

        // Act
        boolean result = customerService.isEmployee(1L);

        // Assert
        assertTrue(result);
        verify(customerRepository, times(1)).findById(1L);
    }

    @Test
    void isEmployee_ShouldReturnFalse_WhenCustomerIsNotEmployee() {
        // Arrange
        customer.setEmployee(false);
        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(customer));

        // Act
        boolean result = customerService.isEmployee(1L);

        // Assert
        assertFalse(result);
        verify(customerRepository, times(1)).findById(1L);
    }

    @Test
    void isEmployee_ShouldThrowResourceNotFoundException_WhenCustomerDoesNotExist() {
        // Arrange
        when(customerRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> customerService.isEmployee(1L));

        assertEquals("Customer not found with id: '1'", exception.getMessage());
        verify(customerRepository, times(1)).findById(1L);
    }
} 