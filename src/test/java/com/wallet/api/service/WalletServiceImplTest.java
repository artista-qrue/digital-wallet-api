package com.wallet.api.service;

import com.wallet.api.dto.CreateWalletRequest;
import com.wallet.api.dto.WalletDto;
import com.wallet.api.entity.Customer;
import com.wallet.api.entity.Wallet;
import com.wallet.api.exception.ResourceNotFoundException;
import com.wallet.api.repository.CustomerRepository;
import com.wallet.api.repository.WalletRepository;
import com.wallet.api.service.impl.WalletServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WalletServiceImplTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private WalletServiceImpl walletService;

    private Customer customer;
    private Wallet wallet;
    private CreateWalletRequest createWalletRequest;
    private WalletDto walletDto;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setName("John");
        customer.setSurname("Doe");
        customer.setTckn("12345678901");
        customer.setEmployee(true);

        wallet = new Wallet();
        wallet.setId(1L);
        wallet.setWalletName("My TRY Wallet");
        wallet.setCurrency(Wallet.Currency.TRY);
        wallet.setActiveForShopping(true);
        wallet.setActiveForWithdraw(true);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setUsableBalance(BigDecimal.ZERO);
        wallet.setCustomer(customer);

        createWalletRequest = new CreateWalletRequest(
                "My TRY Wallet",
                Wallet.Currency.TRY,
                true,
                true,
                1L
        );

        walletDto = new WalletDto(
                1L,
                "My TRY Wallet",
                Wallet.Currency.TRY,
                true,
                true,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                1L
        );
    }

    @Test
    void createWallet_ShouldCreateWallet_WhenValidInput() {
        // Arrange
        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(customer));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        // Act
        WalletDto result = walletService.createWallet(createWalletRequest);

        // Assert
        assertNotNull(result);
        assertEquals(walletDto.id(), result.id());
        assertEquals(walletDto.walletName(), result.walletName());
        assertEquals(walletDto.currency(), result.currency());
        assertEquals(walletDto.activeForShopping(), result.activeForShopping());
        assertEquals(walletDto.activeForWithdraw(), result.activeForWithdraw());
        assertEquals(walletDto.balance(), result.balance());
        assertEquals(walletDto.usableBalance(), result.usableBalance());
        assertEquals(walletDto.customerId(), result.customerId());

        verify(customerRepository, times(1)).findById(createWalletRequest.customerId());
        verify(walletRepository, times(1)).save(any(Wallet.class));
    }

    @Test
    void createWallet_ShouldThrowResourceNotFoundException_WhenCustomerDoesNotExist() {
        // Arrange
        when(customerRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> walletService.createWallet(createWalletRequest));

        assertEquals("Customer not found with id: '1'", exception.getMessage());
        verify(customerRepository, times(1)).findById(createWalletRequest.customerId());
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    void getWalletsByCustomerId_ShouldReturnWallets_WhenCustomerExists() {
        // Arrange
        when(customerRepository.existsById(anyLong())).thenReturn(true);
        when(walletRepository.findByCustomerId(anyLong())).thenReturn(List.of(wallet));

        // Act
        List<WalletDto> results = walletService.getWalletsByCustomerId(1L);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(walletDto.id(), results.get(0).id());
        assertEquals(walletDto.walletName(), results.get(0).walletName());

        verify(customerRepository, times(1)).existsById(1L);
        verify(walletRepository, times(1)).findByCustomerId(1L);
    }

    @Test
    void getWalletsByCustomerId_ShouldThrowResourceNotFoundException_WhenCustomerDoesNotExist() {
        // Arrange
        when(customerRepository.existsById(anyLong())).thenReturn(false);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> walletService.getWalletsByCustomerId(1L));

        assertEquals("Customer not found with id: '1'", exception.getMessage());
        verify(customerRepository, times(1)).existsById(1L);
        verify(walletRepository, never()).findByCustomerId(anyLong());
    }

    @Test
    void getWalletsByCustomerIdAndCurrency_ShouldReturnWallets_WhenCustomerExists() {
        // Arrange
        when(customerRepository.existsById(anyLong())).thenReturn(true);
        when(walletRepository.findByCustomerIdAndCurrency(anyLong(), any(Wallet.Currency.class)))
                .thenReturn(List.of(wallet));

        // Act
        List<WalletDto> results = walletService.getWalletsByCustomerIdAndCurrency(1L, Wallet.Currency.TRY);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(walletDto.id(), results.get(0).id());
        assertEquals(walletDto.walletName(), results.get(0).walletName());
        assertEquals(walletDto.currency(), results.get(0).currency());

        verify(customerRepository, times(1)).existsById(1L);
        verify(walletRepository, times(1)).findByCustomerIdAndCurrency(1L, Wallet.Currency.TRY);
    }

    @Test
    void getWalletsByCustomerIdAndCurrency_ShouldThrowResourceNotFoundException_WhenCustomerDoesNotExist() {
        // Arrange
        when(customerRepository.existsById(anyLong())).thenReturn(false);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> walletService.getWalletsByCustomerIdAndCurrency(1L, Wallet.Currency.TRY));

        assertEquals("Customer not found with id: '1'", exception.getMessage());
        verify(customerRepository, times(1)).existsById(1L);
        verify(walletRepository, never()).findByCustomerIdAndCurrency(anyLong(), any(Wallet.Currency.class));
    }

    @Test
    void getWalletById_ShouldReturnWallet_WhenWalletExists() {
        // Arrange
        when(walletRepository.findById(anyLong())).thenReturn(Optional.of(wallet));

        // Act
        WalletDto result = walletService.getWalletById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(walletDto.id(), result.id());
        assertEquals(walletDto.walletName(), result.walletName());
        assertEquals(walletDto.currency(), result.currency());
        assertEquals(walletDto.activeForShopping(), result.activeForShopping());
        assertEquals(walletDto.activeForWithdraw(), result.activeForWithdraw());
        assertEquals(walletDto.balance(), result.balance());
        assertEquals(walletDto.usableBalance(), result.usableBalance());
        assertEquals(walletDto.customerId(), result.customerId());

        verify(walletRepository, times(1)).findById(1L);
    }

    @Test
    void getWalletById_ShouldThrowResourceNotFoundException_WhenWalletDoesNotExist() {
        // Arrange
        when(walletRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> walletService.getWalletById(1L));

        assertEquals("Wallet not found with id: '1'", exception.getMessage());
        verify(walletRepository, times(1)).findById(1L);
    }

    @Test
    void getWalletByIdAndCustomerId_ShouldReturnWallet_WhenWalletExists() {
        // Arrange
        when(walletRepository.findByIdAndCustomerId(anyLong(), anyLong())).thenReturn(Optional.of(wallet));

        // Act
        WalletDto result = walletService.getWalletByIdAndCustomerId(1L, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(walletDto.id(), result.id());
        assertEquals(walletDto.walletName(), result.walletName());
        assertEquals(walletDto.currency(), result.currency());
        assertEquals(walletDto.activeForShopping(), result.activeForShopping());
        assertEquals(walletDto.activeForWithdraw(), result.activeForWithdraw());
        assertEquals(walletDto.balance(), result.balance());
        assertEquals(walletDto.usableBalance(), result.usableBalance());
        assertEquals(walletDto.customerId(), result.customerId());

        verify(walletRepository, times(1)).findByIdAndCustomerId(1L, 1L);
    }

    @Test
    void getWalletByIdAndCustomerId_ShouldThrowResourceNotFoundException_WhenWalletDoesNotExist() {
        // Arrange
        when(walletRepository.findByIdAndCustomerId(anyLong(), anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> walletService.getWalletByIdAndCustomerId(1L, 1L));

        assertEquals("Wallet not found with id: '1 for customer 1'", exception.getMessage());
        verify(walletRepository, times(1)).findByIdAndCustomerId(1L, 1L);
    }
} 