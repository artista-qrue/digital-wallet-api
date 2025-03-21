package com.wallet.api.service;

import com.wallet.api.dto.ApproveTransactionRequest;
import com.wallet.api.dto.DepositRequest;
import com.wallet.api.dto.TransactionDto;
import com.wallet.api.dto.WithdrawRequest;
import com.wallet.api.entity.Customer;
import com.wallet.api.entity.Transaction;
import com.wallet.api.entity.Wallet;
import com.wallet.api.exception.InsufficientBalanceException;
import com.wallet.api.exception.ResourceNotFoundException;
import com.wallet.api.repository.TransactionRepository;
import com.wallet.api.repository.WalletRepository;
import com.wallet.api.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private Customer customer;
    private Wallet wallet;
    private Transaction transaction;
    private DepositRequest depositRequest;
    private WithdrawRequest withdrawRequest;
    private ApproveTransactionRequest approveRequest;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        
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
        wallet.setBalance(new BigDecimal("1000"));
        wallet.setUsableBalance(new BigDecimal("1000"));
        wallet.setCustomer(customer);

        transaction = new Transaction();
        transaction.setId(1L);
        transaction.setWallet(wallet);
        transaction.setAmount(new BigDecimal("500"));
        transaction.setType(Transaction.TransactionType.DEPOSIT);
        transaction.setOppositePartyType(Transaction.OppositePartyType.IBAN);
        transaction.setOppositeParty("TR123456789");
        transaction.setStatus(Transaction.TransactionStatus.APPROVED);
        transaction.setTransactionDate(now);

        depositRequest = new DepositRequest(
                new BigDecimal("500"),
                1L,
                "TR123456789",
                Transaction.OppositePartyType.IBAN
        );

        withdrawRequest = new WithdrawRequest(
                new BigDecimal("500"),
                1L,
                "TR987654321",
                Transaction.OppositePartyType.IBAN
        );

        approveRequest = new ApproveTransactionRequest(
                1L,
                Transaction.TransactionStatus.APPROVED
        );
    }

    @Test
    void deposit_ShouldCreateDepositTransaction_WhenValidInput() {
        // Arrange
        when(walletRepository.findById(anyLong())).thenReturn(Optional.of(wallet));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        // Act
        TransactionDto result = transactionService.deposit(depositRequest);

        // Assert
        assertNotNull(result);
        assertEquals(transaction.getId(), result.id());
        assertEquals(transaction.getWallet().getId(), result.walletId());
        assertEquals(transaction.getAmount(), result.amount());
        assertEquals(transaction.getType(), result.type());
        assertEquals(transaction.getOppositePartyType(), result.oppositePartyType());
        assertEquals(transaction.getOppositeParty(), result.oppositeParty());
        assertEquals(transaction.getStatus(), result.status());

        verify(walletRepository, times(1)).findById(depositRequest.walletId());
        verify(walletRepository, times(1)).save(wallet);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void deposit_ShouldThrowResourceNotFoundException_WhenWalletDoesNotExist() {
        // Arrange
        when(walletRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> transactionService.deposit(depositRequest));

        assertEquals("Wallet not found with id: '1'", exception.getMessage());
        verify(walletRepository, times(1)).findById(depositRequest.walletId());
        verify(walletRepository, never()).save(any(Wallet.class));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void withdraw_ShouldCreateWithdrawTransaction_WhenValidInput() {
        // Arrange
        when(walletRepository.findById(anyLong())).thenReturn(Optional.of(wallet));
        transaction.setType(Transaction.TransactionType.WITHDRAW);
        transaction.setOppositeParty("TR987654321");
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        // Act
        TransactionDto result = transactionService.withdraw(withdrawRequest);

        // Assert
        assertNotNull(result);
        assertEquals(transaction.getId(), result.id());
        assertEquals(transaction.getWallet().getId(), result.walletId());
        assertEquals(transaction.getAmount(), result.amount());
        assertEquals(transaction.getType(), result.type());
        assertEquals(transaction.getOppositePartyType(), result.oppositePartyType());
        assertEquals(transaction.getOppositeParty(), result.oppositeParty());
        assertEquals(transaction.getStatus(), result.status());

        verify(walletRepository, times(1)).findById(withdrawRequest.walletId());
        verify(walletRepository, times(1)).save(wallet);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void withdraw_ShouldThrowResourceNotFoundException_WhenWalletDoesNotExist() {
        // Arrange
        when(walletRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> transactionService.withdraw(withdrawRequest));

        assertEquals("Wallet not found with id: '1'", exception.getMessage());
        verify(walletRepository, times(1)).findById(withdrawRequest.walletId());
        verify(walletRepository, never()).save(any(Wallet.class));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void withdraw_ShouldThrowIllegalArgumentException_WhenWalletNotActiveForWithdraw() {
        // Arrange
        wallet.setActiveForWithdraw(false);
        when(walletRepository.findById(anyLong())).thenReturn(Optional.of(wallet));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> transactionService.withdraw(withdrawRequest));

        assertEquals("Wallet is not active for withdrawals", exception.getMessage());
        verify(walletRepository, times(1)).findById(withdrawRequest.walletId());
        verify(walletRepository, never()).save(any(Wallet.class));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void withdraw_ShouldThrowInsufficientBalanceException_WhenWalletHasInsufficientBalance() {
        // Arrange
        wallet.setUsableBalance(new BigDecimal("400"));
        when(walletRepository.findById(anyLong())).thenReturn(Optional.of(wallet));

        // Act & Assert
        InsufficientBalanceException exception = assertThrows(InsufficientBalanceException.class,
                () -> transactionService.withdraw(withdrawRequest));

        assertEquals("Wallet with ID 1 has insufficient balance. Required: 500.00, Available: 400.00", exception.getMessage());
        verify(walletRepository, times(1)).findById(withdrawRequest.walletId());
        verify(walletRepository, never()).save(any(Wallet.class));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void approveTransaction_ShouldApproveTransaction_WhenTransactionIsPending() {
        // Arrange
        transaction.setStatus(Transaction.TransactionStatus.PENDING);
        when(transactionRepository.findById(anyLong())).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        // Act
        TransactionDto result = transactionService.approveTransaction(approveRequest);

        // Assert
        assertNotNull(result);
        assertEquals(approveRequest.status(), result.status());

        verify(transactionRepository, times(1)).findById(approveRequest.transactionId());
        verify(walletRepository, times(1)).save(wallet);
        verify(transactionRepository, times(1)).save(transaction);
    }

    @Test
    void approveTransaction_ShouldThrowResourceNotFoundException_WhenTransactionDoesNotExist() {
        // Arrange
        when(transactionRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> transactionService.approveTransaction(approveRequest));

        assertEquals("Transaction not found with id: '1'", exception.getMessage());
        verify(transactionRepository, times(1)).findById(approveRequest.transactionId());
        verify(walletRepository, never()).save(any(Wallet.class));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void approveTransaction_ShouldThrowIllegalArgumentException_WhenTransactionIsNotPending() {
        // Arrange
        when(transactionRepository.findById(anyLong())).thenReturn(Optional.of(transaction));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> transactionService.approveTransaction(approveRequest));

        assertEquals("Only pending transactions can be approved or denied", exception.getMessage());
        verify(transactionRepository, times(1)).findById(approveRequest.transactionId());
        verify(walletRepository, never()).save(any(Wallet.class));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void getTransactionsByWalletId_ShouldReturnTransactions_WhenWalletExists() {
        // Arrange
        when(walletRepository.existsById(anyLong())).thenReturn(true);
        when(transactionRepository.findByWalletId(anyLong())).thenReturn(List.of(transaction));

        // Act
        List<TransactionDto> results = transactionService.getTransactionsByWalletId(1L);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(transaction.getId(), results.get(0).id());
        assertEquals(transaction.getWallet().getId(), results.get(0).walletId());

        verify(walletRepository, times(1)).existsById(1L);
        verify(transactionRepository, times(1)).findByWalletId(1L);
    }

    @Test
    void getTransactionsByWalletId_ShouldThrowResourceNotFoundException_WhenWalletDoesNotExist() {
        // Arrange
        when(walletRepository.existsById(anyLong())).thenReturn(false);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> transactionService.getTransactionsByWalletId(1L));

        assertEquals("Wallet not found with id: '1'", exception.getMessage());
        verify(walletRepository, times(1)).existsById(1L);
        verify(transactionRepository, never()).findByWalletId(anyLong());
    }

    @Test
    void getTransactionById_ShouldReturnTransaction_WhenTransactionExists() {
        // Arrange
        when(transactionRepository.findById(anyLong())).thenReturn(Optional.of(transaction));

        // Act
        TransactionDto result = transactionService.getTransactionById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(transaction.getId(), result.id());
        assertEquals(transaction.getWallet().getId(), result.walletId());
        assertEquals(transaction.getAmount(), result.amount());
        assertEquals(transaction.getType(), result.type());
        assertEquals(transaction.getOppositePartyType(), result.oppositePartyType());
        assertEquals(transaction.getOppositeParty(), result.oppositeParty());
        assertEquals(transaction.getStatus(), result.status());

        verify(transactionRepository, times(1)).findById(1L);
    }

    @Test
    void getTransactionById_ShouldThrowResourceNotFoundException_WhenTransactionDoesNotExist() {
        // Arrange
        when(transactionRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> transactionService.getTransactionById(1L));

        assertEquals("Transaction not found with id: '1'", exception.getMessage());
        verify(transactionRepository, times(1)).findById(1L);
    }
} 