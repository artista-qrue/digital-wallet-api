package com.wallet.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.api.dto.CreateWalletRequest;
import com.wallet.api.dto.WalletDto;
import com.wallet.api.entity.Wallet;
import com.wallet.api.exception.ResourceNotFoundException;
import com.wallet.api.service.CustomerService;
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
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class WalletControllerTest {

    private MockMvc mockMvc;

    @Mock
    private WalletService walletService;

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private WalletController walletController;

    private ObjectMapper objectMapper;
    private WalletDto walletDto;
    private CreateWalletRequest createWalletRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(walletController).build();
        objectMapper = new ObjectMapper();

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

        createWalletRequest = new CreateWalletRequest(
                "My TRY Wallet",
                Wallet.Currency.TRY,
                true,
                true,
                1L
        );
    }

    @Test
    void createWallet_ShouldReturnCreatedWallet_WhenEmployeeCreatesWallet() throws Exception {
        // Arrange
        when(customerService.isEmployee(anyLong())).thenReturn(true);
        when(walletService.createWallet(any(CreateWalletRequest.class))).thenReturn(walletDto);

        // Act & Assert
        mockMvc.perform(post("/api/wallets")
                        .header("X-Customer-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createWalletRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result", is("SUCCESS")))
                .andExpect(jsonPath("$.message", is("Wallet created successfully")))
                .andExpect(jsonPath("$.data.id", is(1)))
                .andExpect(jsonPath("$.data.walletName", is("My TRY Wallet")))
                .andExpect(jsonPath("$.data.currency", is("TRY")))
                .andExpect(jsonPath("$.data.activeForShopping", is(true)))
                .andExpect(jsonPath("$.data.activeForWithdraw", is(true)))
                .andExpect(jsonPath("$.data.balance", is(0)))
                .andExpect(jsonPath("$.data.usableBalance", is(0)))
                .andExpect(jsonPath("$.data.customerId", is(1)));
    }

    @Test
    void createWallet_ShouldReturnCreatedWallet_WhenCustomerCreatesOwnWallet() throws Exception {
        // Arrange
        when(customerService.isEmployee(anyLong())).thenReturn(false);
        when(walletService.createWallet(any(CreateWalletRequest.class))).thenReturn(walletDto);

        // Act & Assert
        mockMvc.perform(post("/api/wallets")
                        .header("X-Customer-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createWalletRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result", is("SUCCESS")))
                .andExpect(jsonPath("$.message", is("Wallet created successfully")))
                .andExpect(jsonPath("$.data.id", is(1)))
                .andExpect(jsonPath("$.data.walletName", is("My TRY Wallet")))
                .andExpect(jsonPath("$.data.customerId", is(1)));
    }

    @Test
    void createWallet_ShouldReturnForbidden_WhenCustomerCreatesWalletForOthers() throws Exception {
        // Arrange
        when(customerService.isEmployee(anyLong())).thenReturn(false);
        CreateWalletRequest otherCustomerRequest = new CreateWalletRequest(
                "Other Customer Wallet",
                Wallet.Currency.TRY,
                true,
                true,
                2L // Different customer ID
        );

        // Act & Assert
        mockMvc.perform(post("/api/wallets")
                        .header("X-Customer-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(otherCustomerRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.result", is("ERROR")))
                .andExpect(jsonPath("$.message", is("You can only create wallets for yourself unless you are an employee")))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void getWalletsByCustomerId_ShouldReturnWallets_WhenEmployeeRequests() throws Exception {
        // Arrange
        when(customerService.isEmployee(anyLong())).thenReturn(true);
        when(walletService.getWalletsByCustomerId(anyLong())).thenReturn(List.of(walletDto));

        // Act & Assert
        mockMvc.perform(get("/api/wallets/customer/1")
                        .header("X-Customer-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", is("SUCCESS")))
                .andExpect(jsonPath("$.message", is("Wallets retrieved successfully")))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id", is(1)))
                .andExpect(jsonPath("$.data[0].walletName", is("My TRY Wallet")));
    }

    @Test
    void getWalletsByCustomerId_ShouldReturnWallets_WhenCustomerRequestsOwnWallets() throws Exception {
        // Arrange
        when(customerService.isEmployee(anyLong())).thenReturn(false);
        when(walletService.getWalletsByCustomerId(anyLong())).thenReturn(List.of(walletDto));

        // Act & Assert
        mockMvc.perform(get("/api/wallets/customer/1")
                        .header("X-Customer-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", is("SUCCESS")))
                .andExpect(jsonPath("$.message", is("Wallets retrieved successfully")))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id", is(1)));
    }

    @Test
    void getWalletsByCustomerId_ShouldReturnForbidden_WhenCustomerRequestsOtherWallets() throws Exception {
        // Arrange
        when(customerService.isEmployee(anyLong())).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/wallets/customer/2")
                        .header("X-Customer-Id", "1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.result", is("ERROR")))
                .andExpect(jsonPath("$.message", is("You can only view your own wallets unless you are an employee")))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void getWalletsByCustomerIdAndCurrency_ShouldReturnWallets_WhenValidRequest() throws Exception {
        // Arrange
        when(customerService.isEmployee(anyLong())).thenReturn(true);
        when(walletService.getWalletsByCustomerIdAndCurrency(anyLong(), any(Wallet.Currency.class)))
                .thenReturn(List.of(walletDto));

        // Act & Assert
        mockMvc.perform(get("/api/wallets/customer/1/currency/TRY")
                        .header("X-Customer-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", is("SUCCESS")))
                .andExpect(jsonPath("$.message", is("Wallets retrieved successfully")))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id", is(1)))
                .andExpect(jsonPath("$.data[0].currency", is("TRY")));
    }

    @Test
    void getWalletById_ShouldReturnWallet_WhenEmployeeRequests() throws Exception {
        // Arrange
        when(customerService.isEmployee(anyLong())).thenReturn(true);
        when(walletService.getWalletById(anyLong())).thenReturn(walletDto);

        // Act & Assert
        mockMvc.perform(get("/api/wallets/1")
                        .header("X-Customer-Id", "2")) // Different customer ID
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", is("SUCCESS")))
                .andExpect(jsonPath("$.message", is("Wallet retrieved successfully")))
                .andExpect(jsonPath("$.data.id", is(1)))
                .andExpect(jsonPath("$.data.walletName", is("My TRY Wallet")));
    }

    @Test
    void getWalletById_ShouldReturnWallet_WhenCustomerRequestsOwnWallet() throws Exception {
        // Arrange
        when(customerService.isEmployee(anyLong())).thenReturn(false);
        when(walletService.getWalletById(anyLong())).thenReturn(walletDto);

        // Act & Assert
        mockMvc.perform(get("/api/wallets/1")
                        .header("X-Customer-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", is("SUCCESS")))
                .andExpect(jsonPath("$.message", is("Wallet retrieved successfully")))
                .andExpect(jsonPath("$.data.id", is(1)));
    }

    @Test
    void getWalletById_ShouldReturnForbidden_WhenCustomerRequestsOtherWallet() throws Exception {
        // Arrange
        when(customerService.isEmployee(anyLong())).thenReturn(false);
        when(walletService.getWalletById(anyLong())).thenReturn(walletDto);

        // Act & Assert
        mockMvc.perform(get("/api/wallets/1")
                        .header("X-Customer-Id", "2")) // Different customer ID
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.result", is("ERROR")))
                .andExpect(jsonPath("$.message", is("You can only view your own wallets unless you are an employee")))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void getWalletById_ShouldReturnNotFound_WhenWalletDoesNotExist() throws Exception {
        // Arrange
        when(walletService.getWalletById(anyLong()))
                .thenThrow(new ResourceNotFoundException("Wallet", "id", 1L));

        // Act & Assert
        mockMvc.perform(get("/api/wallets/1")
                        .header("X-Customer-Id", "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result", is("ERROR")))
                .andExpect(jsonPath("$.message", is("Wallet not found with id: '1'")))
                .andExpect(jsonPath("$.data").doesNotExist());
    }
} 