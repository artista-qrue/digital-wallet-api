package com.wallet.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.api.dto.CreateTransactionRequest;
import com.wallet.api.dto.TransactionDto;
import com.wallet.api.entity.Transaction;
import com.wallet.api.entity.Wallet;
import com.wallet.api.exception.InsufficientBalanceException;
import com.wallet.api.exception.ResourceNotFoundException;
import com.wallet.api.service.CustomerService;
import com.wallet.api.service.TransactionService;
import com.wallet.api.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class TransactionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TransactionService transactionService;

    @Mock
    private CustomerService customerService;

    @Mock
    private WalletService walletService;

    @InjectMocks
    private TransactionController transactionController;

    private ObjectMapper objectMapper;
    private TransactionDto transactionDto;
    private CreateTransactionRequest createTransactionRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(transactionController).build();
        objectMapper = new ObjectMapper();

        transactionDto = new TransactionDto(
                1L,
                BigDecimal.valueOf(100),
                Transaction.Type.DEPOSIT,
                Transaction.Status.COMPLETED,
                "Deposit transaction",
                1L,
                LocalDateTime.now()
        );

        createTransactionRequest = new CreateTransactionRequest(
                BigDecimal.valueOf(100),
                "Deposit transaction",
                1L
        );
    }

    @Test
    void deposit_ShouldReturnCreatedTransaction_WhenValidInput() throws Exception {
        // Arrange
        when(customerService.isEmployee(anyLong())).thenReturn(false);
        when(walletService.getWalletByIdAndCustomerId(anyLong(), anyLong())).thenReturn(null); // Not actually used in the test
        when(transactionService.deposit(any(CreateTransactionRequest.class))).thenReturn(transactionDto);

        // Act & Assert
        mockMvc.perform(post("/api/transactions/deposit")
                        .header("X-Customer-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTransactionRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result", is("SUCCESS")))
                .andExpect(jsonPath("$.message", is("Deposit transaction created successfully")))
                .andExpect(jsonPath("$.data.id", is(1)))
                .andExpect(jsonPath("$.data.amount", is(100)))
                .andExpect(jsonPath("$.data.type", is("DEPOSIT")))
                .andExpect(jsonPath("$.data.status", is("COMPLETED")))
                .andExpect(jsonPath("$.data.walletId", is(1)));
    }

    @Test
    void deposit_ShouldReturnForbidden_WhenCustomerDepositsToOtherWallet() throws Exception {
        // Arrange
        when(customerService.isEmployee(anyLong())).thenReturn(false);
        when(walletService.getWalletByIdAndCustomerId(anyLong(), anyLong()))
                .thenThrow(new ResourceNotFoundException("Wallet", "id", 1L));

        // Act & Assert
        mockMvc.perform(post("/api/transactions/deposit")
                        .header("X-Customer-Id", "2") // Different customer ID
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTransactionRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result", is("ERROR")))
                .andExpect(jsonPath("$.message", is("Wallet not found with id: '1'")))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void deposit_ShouldAllowDeposit_WhenEmployeeDepositsToAnyWallet() throws Exception {
        // Arrange
        when(customerService.isEmployee(anyLong())).thenReturn(true);
        when(transactionService.deposit(any(CreateTransactionRequest.class))).thenReturn(transactionDto);

        // Act & Assert
        mockMvc.perform(post("/api/transactions/deposit")
                        .header("X-Customer-Id", "2") // Employee ID
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTransactionRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result", is("SUCCESS")))
                .andExpect(jsonPath("$.message", is("Deposit transaction created successfully")))
                .andExpect(jsonPath("$.data.id", is(1)));
    }

    @Test
    void withdraw_ShouldReturnCreatedTransaction_WhenValidInput() throws Exception {
        // Arrange
        when(customerService.isEmployee(anyLong())).thenReturn(false);
        when(walletService.getWalletByIdAndCustomerId(anyLong(), anyLong())).thenReturn(null); // Not actually used in the test
        when(transactionService.withdraw(any(CreateTransactionRequest.class))).thenReturn(transactionDto);

        // Modify transactionDto for withdrawal
        transactionDto = new TransactionDto(
                1L,
                BigDecimal.valueOf(100),
                Transaction.Type.WITHDRAW,
                Transaction.Status.COMPLETED,
                "Withdraw transaction",
                1L,
                LocalDateTime.now()
        );

        // Act & Assert
        mockMvc.perform(post("/api/transactions/withdraw")
                        .header("X-Customer-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTransactionRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result", is("SUCCESS")))
                .andExpect(jsonPath("$.message", is("Withdrawal transaction created successfully")))
                .andExpect(jsonPath("$.data.id", is(1)))
                .andExpect(jsonPath("$.data.type", is("WITHDRAW")));
    }

    @Test
    void withdraw_ShouldReturnInsufficientBalance_WhenBalanceNotEnough() throws Exception {
        // Arrange
        when(customerService.isEmployee(anyLong())).thenReturn(false);
        when(walletService.getWalletByIdAndCustomerId(anyLong(), anyLong())).thenReturn(null); // Not actually used in the test
        when(transactionService.withdraw(any(CreateTransactionRequest.class)))
                .thenThrow(new InsufficientBalanceException(BigDecimal.valueOf(100), BigDecimal.valueOf(50)));

        // Act & Assert
        mockMvc.perform(post("/api/transactions/withdraw")
                        .header("X-Customer-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTransactionRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result", is("ERROR")))
                .andExpect(jsonPath("$.message", containsString("Insufficient balance")))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void approveTransaction_ShouldReturnApprovedTransaction_WhenEmployeeApprovesValidTransaction() throws Exception {
        // Arrange
        when(customerService.isEmployee(anyLong())).thenReturn(true);
        
        // Modify transactionDto for approval (was pending, now approved)
        TransactionDto pendingTransaction = new TransactionDto(
                1L,
                BigDecimal.valueOf(1000),
                Transaction.Type.WITHDRAW,
                Transaction.Status.PENDING,
                "Large withdrawal needs approval",
                1L,
                LocalDateTime.now()
        );
        
        TransactionDto approvedTransaction = new TransactionDto(
                1L,
                BigDecimal.valueOf(1000),
                Transaction.Type.WITHDRAW,
                Transaction.Status.COMPLETED,
                "Large withdrawal needs approval",
                1L,
                LocalDateTime.now()
        );
        
        when(transactionService.getTransactionById(anyLong())).thenReturn(pendingTransaction);
        when(transactionService.approveTransaction(anyLong())).thenReturn(approvedTransaction);

        // Act & Assert
        mockMvc.perform(put("/api/transactions/1/approve")
                        .header("X-Customer-Id", "2")) // Employee ID
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", is("SUCCESS")))
                .andExpect(jsonPath("$.message", is("Transaction approved successfully")))
                .andExpect(jsonPath("$.data.id", is(1)))
                .andExpect(jsonPath("$.data.status", is("COMPLETED")));
    }

    @Test
    void approveTransaction_ShouldReturnForbidden_WhenRegularCustomerAttemptsApproval() throws Exception {
        // Arrange
        when(customerService.isEmployee(anyLong())).thenReturn(false);

        // Act & Assert
        mockMvc.perform(put("/api/transactions/1/approve")
                        .header("X-Customer-Id", "1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.result", is("ERROR")))
                .andExpect(jsonPath("$.message", is("Only employees can approve transactions")))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void getTransactionsByWalletId_ShouldReturnTransactions_WhenEmployeeRequests() throws Exception {
        // Arrange
        when(customerService.isEmployee(anyLong())).thenReturn(true);
        when(transactionService.getTransactionsByWalletId(anyLong())).thenReturn(List.of(transactionDto));

        // Act & Assert
        mockMvc.perform(get("/api/transactions/wallet/1")
                        .header("X-Customer-Id", "2")) // Employee ID
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", is("SUCCESS")))
                .andExpect(jsonPath("$.message", is("Transactions retrieved successfully")))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id", is(1)));
    }

    @Test
    void getTransactionsByWalletId_ShouldReturnTransactions_WhenCustomerRequestsOwnWalletTransactions() throws Exception {
        // Arrange
        when(customerService.isEmployee(anyLong())).thenReturn(false);
        when(walletService.getWalletByIdAndCustomerId(anyLong(), anyLong())).thenReturn(null); // Not actually used in the test
        when(transactionService.getTransactionsByWalletId(anyLong())).thenReturn(List.of(transactionDto));

        // Act & Assert
        mockMvc.perform(get("/api/transactions/wallet/1")
                        .header("X-Customer-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", is("SUCCESS")))
                .andExpect(jsonPath("$.message", is("Transactions retrieved successfully")))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id", is(1)));
    }

    @Test
    void getTransactionsByWalletId_ShouldReturnForbidden_WhenCustomerRequestsOtherWalletTransactions() throws Exception {
        // Arrange
        when(customerService.isEmployee(anyLong())).thenReturn(false);
        when(walletService.getWalletByIdAndCustomerId(anyLong(), anyLong()))
                .thenThrow(new ResourceNotFoundException("Wallet", "id", 1L));

        // Act & Assert
        mockMvc.perform(get("/api/transactions/wallet/1")
                        .header("X-Customer-Id", "2")) // Different customer ID
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result", is("ERROR")))
                .andExpect(jsonPath("$.message", is("Wallet not found with id: '1'")))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void getTransactionById_ShouldReturnTransaction_WhenEmployeeRequests() throws Exception {
        // Arrange
        when(customerService.isEmployee(anyLong())).thenReturn(true);
        when(transactionService.getTransactionById(anyLong())).thenReturn(transactionDto);

        // Act & Assert
        mockMvc.perform(get("/api/transactions/1")
                        .header("X-Customer-Id", "2")) // Employee ID
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", is("SUCCESS")))
                .andExpect(jsonPath("$.message", is("Transaction retrieved successfully")))
                .andExpect(jsonPath("$.data.id", is(1)));
    }

    @Test
    void getTransactionById_ShouldReturnTransaction_WhenCustomerRequestsOwnTransaction() throws Exception {
        // Arrange
        when(customerService.isEmployee(anyLong())).thenReturn(false);
        when(transactionService.getTransactionById(anyLong())).thenReturn(transactionDto);
        when(walletService.isWalletOwnedByCustomer(anyLong(), anyLong())).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/api/transactions/1")
                        .header("X-Customer-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", is("SUCCESS")))
                .andExpect(jsonPath("$.message", is("Transaction retrieved successfully")))
                .andExpect(jsonPath("$.data.id", is(1)));
    }

    @Test
    void getTransactionById_ShouldReturnForbidden_WhenCustomerRequestsOtherTransaction() throws Exception {
        // Arrange
        when(customerService.isEmployee(anyLong())).thenReturn(false);
        when(transactionService.getTransactionById(anyLong())).thenReturn(transactionDto);
        when(walletService.isWalletOwnedByCustomer(anyLong(), anyLong())).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/transactions/1")
                        .header("X-Customer-Id", "2")) // Different customer ID
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.result", is("ERROR")))
                .andExpect(jsonPath("$.message", is("You can only view your own transactions unless you are an employee")))
                .andExpect(jsonPath("$.data").doesNotExist());
    }
} 