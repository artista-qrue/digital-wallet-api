package com.wallet.api.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.api.dto.ApproveTransactionRequest;
import com.wallet.api.dto.DepositRequest;
import com.wallet.api.dto.TransactionDto;
import com.wallet.api.dto.WalletDto;
import com.wallet.api.dto.WithdrawRequest;
import com.wallet.api.entity.Transaction;
import com.wallet.api.entity.Wallet;
import com.wallet.api.exception.InsufficientBalanceException;
import com.wallet.api.security.WithMockCustomer;
import com.wallet.api.service.CustomerService;
import com.wallet.api.service.TransactionService;
import com.wallet.api.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
public class TransactionControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private CustomerService customerService;

    @MockBean
    private WalletService walletService;

    private ObjectMapper objectMapper;
    private TransactionDto transactionDto;
    private DepositRequest depositRequest;
    private WithdrawRequest withdrawRequest;
    private WalletDto walletDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(SecurityMockMvcConfigurers.springSecurity())
            .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        transactionDto = new TransactionDto(
            1L,
            1L,
            BigDecimal.valueOf(100),
            Transaction.TransactionType.DEPOSIT,
            Transaction.OppositePartyType.IBAN,
            "Bank account",
            Transaction.TransactionStatus.APPROVED,
            LocalDateTime.now()
        );

        walletDto = new WalletDto(
            1L,
            "My Wallet",
            Wallet.Currency.USD,
            true,
            true,
            BigDecimal.valueOf(1000),
            BigDecimal.valueOf(1000),
            1L
        );

        depositRequest = new DepositRequest(
            BigDecimal.valueOf(100),
            1L,
            "Bank account",
            Transaction.OppositePartyType.IBAN
        );

        withdrawRequest = new WithdrawRequest(
            BigDecimal.valueOf(100),
            1L,
            "Bank account",
            Transaction.OppositePartyType.IBAN
        );
    }

    @Test
    @WithMockCustomer(customerId = 1L, tckn = "12345678901", isEmployee = false)
    void deposit_ShouldReturnCreatedTransaction_WhenValidInput() throws Exception {
        when(customerService.isEmployee(anyLong())).thenReturn(false);
        when(walletService.getWalletById(anyLong())).thenReturn(walletDto);
        when(transactionService.deposit(any(DepositRequest.class))).thenReturn(transactionDto);

        mockMvc.perform(post("/api/transactions/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(depositRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.result", is("SUCCESS")))
            .andExpect(jsonPath("$.message", is("Deposit created successfully")))
            .andExpect(jsonPath("$.data.id", is(1)))
            .andExpect(jsonPath("$.data.amount", is(100)))
            .andExpect(jsonPath("$.data.type", is("DEPOSIT")))
            .andExpect(jsonPath("$.data.status", is("APPROVED")))
            .andExpect(jsonPath("$.data.walletId", is(1)));
    }

    @Test
    @WithMockCustomer(customerId = 1L, tckn = "12345678901", isEmployee = false)
    void deposit_ShouldReturnForbidden_WhenCustomerDepositsToOtherWallet() throws Exception {
        when(customerService.isEmployee(anyLong())).thenReturn(false);

        WalletDto otherWallet = new WalletDto(
            1L,
            "Other Wallet",
            Wallet.Currency.USD,
            true,
            true,
            BigDecimal.valueOf(1000),
            BigDecimal.valueOf(1000),
            2L
        );

        when(walletService.getWalletById(anyLong())).thenReturn(otherWallet);

        mockMvc.perform(post("/api/transactions/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(depositRequest)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.result", is("ERROR")))
            .andExpect(jsonPath("$.message", is("You can only deposit to your own wallets unless you are an employee")))
            .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @WithMockCustomer(customerId = 1L, tckn = "12345678901", isEmployee = true)
    void deposit_ShouldAllowDeposit_WhenEmployeeDepositsToAnyWallet() throws Exception {
        when(customerService.isEmployee(anyLong())).thenReturn(true);

        WalletDto customerWallet = new WalletDto(
            1L,
            "Customer Wallet",
            Wallet.Currency.USD,
            true,
            true,
            BigDecimal.valueOf(1000),
            BigDecimal.valueOf(1000),
            2L
        );

        when(walletService.getWalletById(anyLong())).thenReturn(customerWallet);
        when(transactionService.deposit(any(DepositRequest.class))).thenReturn(transactionDto);

        // Act & Assert
        mockMvc.perform(post("/api/transactions/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(depositRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.result", is("SUCCESS")))
            .andExpect(jsonPath("$.message", is("Deposit created successfully")))
            .andExpect(jsonPath("$.data.id", is(1)));
    }

    @Test
    @WithMockCustomer(customerId = 1L, tckn = "12345678901", isEmployee = false)
    void withdraw_ShouldReturnCreatedTransaction_WhenValidInput() throws Exception {
        when(customerService.isEmployee(anyLong())).thenReturn(false);
        when(walletService.getWalletById(anyLong())).thenReturn(walletDto);

        TransactionDto withdrawalDto = new TransactionDto(
            1L,
            1L,
            BigDecimal.valueOf(100),
            Transaction.TransactionType.WITHDRAW,
            Transaction.OppositePartyType.IBAN,
            "Bank account",
            Transaction.TransactionStatus.APPROVED,
            LocalDateTime.now()
        );

        when(transactionService.withdraw(any(WithdrawRequest.class))).thenReturn(withdrawalDto);

        mockMvc.perform(post("/api/transactions/withdraw")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(withdrawRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.result", is("SUCCESS")))
            .andExpect(jsonPath("$.message", is("Withdrawal created successfully")))
            .andExpect(jsonPath("$.data.id", is(1)))
            .andExpect(jsonPath("$.data.type", is("WITHDRAW")));
    }

    @Test
    @WithMockCustomer(customerId = 1L, tckn = "12345678901", isEmployee = false)
    void withdraw_ShouldReturnInsufficientBalance_WhenBalanceNotEnough() throws Exception {
        when(customerService.isEmployee(anyLong())).thenReturn(false);
        when(walletService.getWalletById(anyLong())).thenReturn(walletDto);

        when(transactionService.withdraw(any(WithdrawRequest.class)))
            .thenThrow(new InsufficientBalanceException("Insufficient balance: Required: 100.00, Available: 50.00"));

        mockMvc.perform(post("/api/transactions/withdraw")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(withdrawRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.result", is("ERROR")))
            .andExpect(jsonPath("$.message", containsString("Insufficient balance")))
            .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @WithMockCustomer(customerId = 2L, tckn = "98765432109", isEmployee = true)
    void approveTransaction_ShouldReturnApprovedTransaction_WhenEmployeeApprovesValidTransaction() throws Exception {
        when(customerService.isEmployee(anyLong())).thenReturn(true);

        ApproveTransactionRequest approveRequest = new ApproveTransactionRequest(1L, Transaction.TransactionStatus.APPROVED);

        TransactionDto pendingTransaction = new TransactionDto(
            1L,
            1L,
            BigDecimal.valueOf(1000),
            Transaction.TransactionType.WITHDRAW,
            Transaction.OppositePartyType.IBAN,
            "Large withdrawal",
            Transaction.TransactionStatus.PENDING,
            LocalDateTime.now()
        );

        TransactionDto approvedTransaction = new TransactionDto(
            1L,
            1L,
            BigDecimal.valueOf(1000),
            Transaction.TransactionType.WITHDRAW,
            Transaction.OppositePartyType.IBAN,
            "Large withdrawal",
            Transaction.TransactionStatus.APPROVED,
            LocalDateTime.now()
        );

        when(transactionService.getTransactionById(anyLong())).thenReturn(pendingTransaction);
        when(transactionService.approveTransaction(any(ApproveTransactionRequest.class))).thenReturn(approvedTransaction);

        mockMvc.perform(post("/api/transactions/approve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(approveRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result", is("SUCCESS")))
            .andExpect(jsonPath("$.message", is("Transaction status updated successfully")))
            .andExpect(jsonPath("$.data.id", is(1)))
            .andExpect(jsonPath("$.data.status", is("APPROVED")));
    }

    @Test
    @WithMockCustomer(customerId = 1L, tckn = "12345678901", isEmployee = false)
    void approveTransaction_ShouldReturnForbidden_WhenRegularCustomerAttemptsApproval() throws Exception {
        when(customerService.isEmployee(anyLong())).thenReturn(false);
        ApproveTransactionRequest approveRequest = new ApproveTransactionRequest(1L, Transaction.TransactionStatus.APPROVED);

        mockMvc.perform(post("/api/transactions/approve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(approveRequest)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.result", is("ERROR")))
            .andExpect(jsonPath("$.message", is("Only employees can approve transactions")))
            .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @WithMockCustomer(customerId = 2L, tckn = "98765432109", isEmployee = true)
    void getTransactionsByWalletId_ShouldReturnTransactions_WhenEmployeeRequests() throws Exception {
        when(customerService.isEmployee(anyLong())).thenReturn(true);
        when(walletService.getWalletById(anyLong())).thenReturn(walletDto);
        when(transactionService.getTransactionsByWalletId(anyLong())).thenReturn(List.of(transactionDto));

        mockMvc.perform(get("/api/transactions/wallet/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result", is("SUCCESS")))
            .andExpect(jsonPath("$.message", is("Transactions retrieved successfully")))
            .andExpect(jsonPath("$.data", hasSize(1)))
            .andExpect(jsonPath("$.data[0].id", is(1)));
    }

    @Test
    @WithMockCustomer(customerId = 1L, tckn = "12345678901", isEmployee = false)
    void getTransactionsByWalletId_ShouldReturnTransactions_WhenCustomerRequestsOwnWalletTransactions() throws Exception {
        when(customerService.isEmployee(anyLong())).thenReturn(false);
        when(walletService.getWalletById(anyLong())).thenReturn(walletDto); // Wallet belongs to customer 1
        when(transactionService.getTransactionsByWalletId(anyLong())).thenReturn(List.of(transactionDto));

        mockMvc.perform(get("/api/transactions/wallet/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result", is("SUCCESS")))
            .andExpect(jsonPath("$.message", is("Transactions retrieved successfully")))
            .andExpect(jsonPath("$.data", hasSize(1)));
    }

    @Test
    @WithMockCustomer(customerId = 1L, tckn = "12345678901", isEmployee = false)
    void getTransactionsByWalletId_ShouldReturnForbidden_WhenCustomerRequestsOtherWalletTransactions() throws Exception {
        when(customerService.isEmployee(anyLong())).thenReturn(false);

        WalletDto otherCustomerWallet = new WalletDto(
            1L,
            "Other Customer Wallet",
            Wallet.Currency.USD,
            true,
            true,
            BigDecimal.valueOf(1000),
            BigDecimal.valueOf(1000),
            2L
        );

        when(walletService.getWalletById(anyLong())).thenReturn(otherCustomerWallet);

        mockMvc.perform(get("/api/transactions/wallet/1"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.result", is("ERROR")))
            .andExpect(jsonPath("$.message", is("You can only view transactions for your own wallets unless you are an employee")));
    }

    @Test
    @WithMockCustomer(customerId = 2L, tckn = "98765432109", isEmployee = true)
    void getTransactionById_ShouldReturnTransaction_WhenEmployeeRequests() throws Exception {
        when(customerService.isEmployee(anyLong())).thenReturn(true);
        when(transactionService.getTransactionById(anyLong())).thenReturn(transactionDto);
        when(walletService.getWalletById(anyLong())).thenReturn(walletDto);

        mockMvc.perform(get("/api/transactions/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result", is("SUCCESS")))
            .andExpect(jsonPath("$.message", is("Transaction retrieved successfully")))
            .andExpect(jsonPath("$.data.id", is(1)));
    }

    @Test
    @WithMockCustomer(customerId = 1L, tckn = "12345678901", isEmployee = false)
    void getTransactionById_ShouldReturnTransaction_WhenCustomerRequestsOwnTransaction() throws Exception {
        when(customerService.isEmployee(anyLong())).thenReturn(false);
        when(transactionService.getTransactionById(anyLong())).thenReturn(transactionDto);
        when(walletService.getWalletById(anyLong())).thenReturn(walletDto);

        mockMvc.perform(get("/api/transactions/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result", is("SUCCESS")))
            .andExpect(jsonPath("$.message", is("Transaction retrieved successfully")))
            .andExpect(jsonPath("$.data.id", is(1)));
    }

    @Test
    @WithMockCustomer(customerId = 1L, tckn = "12345678901", isEmployee = false)
    void getTransactionById_ShouldReturnForbidden_WhenCustomerRequestsOtherTransaction() throws Exception {
        when(customerService.isEmployee(anyLong())).thenReturn(false);
        when(transactionService.getTransactionById(anyLong())).thenReturn(transactionDto);

        WalletDto otherCustomerWallet = new WalletDto(
            1L,
            "Other Customer Wallet",
            Wallet.Currency.USD,
            true,
            true,
            BigDecimal.valueOf(1000),
            BigDecimal.valueOf(1000),
            2L
        );

        when(walletService.getWalletById(anyLong())).thenReturn(otherCustomerWallet);

        mockMvc.perform(get("/api/transactions/1"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.result", is("ERROR")))
            .andExpect(jsonPath("$.message", is("You can only view transactions for your own wallets unless you are an employee")));
    }
} 