package com.wallet.api.controller;

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
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.api.dto.CreateWalletRequest;
import com.wallet.api.dto.WalletDto;
import com.wallet.api.entity.Wallet;
import com.wallet.api.exception.ResourceNotFoundException;
import com.wallet.api.security.WithMockCustomer;
import com.wallet.api.service.CustomerService;
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
public class WalletControllerTest {

    @Autowired
    private WebApplicationContext context;
    
    private MockMvc mockMvc;

    @MockBean
    private WalletService walletService;

    @MockBean
    private CustomerService customerService;

    private ObjectMapper objectMapper;
    private WalletDto walletDto;
    private CreateWalletRequest createWalletRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(SecurityMockMvcConfigurers.springSecurity())
            .build();
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
    @WithMockCustomer(customerId = 1L, tckn = "12345678901", isEmployee = true)
    void createWallet_ShouldReturnCreatedWallet_WhenEmployeeCreatesWallet() throws Exception {
        // Arrange
        when(customerService.isEmployee(anyLong())).thenReturn(true);
        when(walletService.createWallet(any(CreateWalletRequest.class))).thenReturn(walletDto);

        // Act & Assert
        mockMvc.perform(post("/api/wallets")
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
    @WithMockCustomer(customerId = 1L, tckn = "12345678901", isEmployee = false)
    void createWallet_ShouldReturnCreatedWallet_WhenCustomerCreatesOwnWallet() throws Exception {
        // Arrange
        when(customerService.isEmployee(anyLong())).thenReturn(false);
        when(walletService.createWallet(any(CreateWalletRequest.class))).thenReturn(walletDto);

        // Act & Assert
        mockMvc.perform(post("/api/wallets")
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
    @WithMockCustomer(customerId = 1L, tckn = "12345678901", isEmployee = false)
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
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(otherCustomerRequest)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.result", is("ERROR")))
            .andExpect(jsonPath("$.message", is("You can only create wallets for yourself unless you are an employee")))
            .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @WithMockCustomer(customerId = 1L, tckn = "12345678901", isEmployee = true)
    void getWalletsByCustomerId_ShouldReturnWallets_WhenEmployeeRequests() throws Exception {
        // Arrange
        when(customerService.isEmployee(anyLong())).thenReturn(true);
        when(walletService.getWalletsByCustomerId(anyLong())).thenReturn(List.of(walletDto));

        // Act & Assert
        mockMvc.perform(get("/api/wallets/customer/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result", is("SUCCESS")))
            .andExpect(jsonPath("$.message", is("Wallets retrieved successfully")))
            .andExpect(jsonPath("$.data", hasSize(1)))
            .andExpect(jsonPath("$.data[0].id", is(1)))
            .andExpect(jsonPath("$.data[0].walletName", is("My TRY Wallet")));
    }

    @Test
    @WithMockCustomer(customerId = 1L, tckn = "12345678901", isEmployee = false)
    void getWalletsByCustomerId_ShouldReturnWallets_WhenCustomerRequestsOwnWallets() throws Exception {
        // Arrange
        when(customerService.isEmployee(anyLong())).thenReturn(false);
        when(walletService.getWalletsByCustomerId(anyLong())).thenReturn(List.of(walletDto));

        // Act & Assert
        mockMvc.perform(get("/api/wallets/customer/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result", is("SUCCESS")))
            .andExpect(jsonPath("$.message", is("Wallets retrieved successfully")))
            .andExpect(jsonPath("$.data", hasSize(1)))
            .andExpect(jsonPath("$.data[0].id", is(1)));
    }

    @Test
    @WithMockCustomer(customerId = 1L, tckn = "12345678901", isEmployee = false)
    void getWalletsByCustomerId_ShouldReturnForbidden_WhenCustomerRequestsOtherWallets() throws Exception {
        // Arrange
        when(customerService.isEmployee(anyLong())).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/wallets/customer/2"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.result", is("ERROR")))
            .andExpect(jsonPath("$.message", is("You can only view your own wallets unless you are an employee")))
            .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @WithMockCustomer(customerId = 1L, tckn = "12345678901", isEmployee = true)
    void getWalletsByCustomerIdAndCurrency_ShouldReturnWallets_WhenValidRequest() throws Exception {
        // Arrange
        when(customerService.isEmployee(anyLong())).thenReturn(true);
        when(walletService.getWalletsByCustomerIdAndCurrency(anyLong(), any(Wallet.Currency.class)))
            .thenReturn(List.of(walletDto));

        // Act & Assert
        mockMvc.perform(get("/api/wallets/customer/1/currency/TRY"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result", is("SUCCESS")))
            .andExpect(jsonPath("$.message", is("Wallets retrieved successfully")))
            .andExpect(jsonPath("$.data", hasSize(1)))
            .andExpect(jsonPath("$.data[0].id", is(1)))
            .andExpect(jsonPath("$.data[0].currency", is("TRY")));
    }

    @Test
    @WithMockCustomer(customerId = 2L, tckn = "98765432109", isEmployee = true)
    void getWalletById_ShouldReturnWallet_WhenEmployeeRequests() throws Exception {
        // Arrange
        when(customerService.isEmployee(anyLong())).thenReturn(true);
        when(walletService.getWalletById(anyLong())).thenReturn(walletDto);

        // Act & Assert
        mockMvc.perform(get("/api/wallets/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result", is("SUCCESS")))
            .andExpect(jsonPath("$.message", is("Wallet retrieved successfully")))
            .andExpect(jsonPath("$.data.id", is(1)))
            .andExpect(jsonPath("$.data.walletName", is("My TRY Wallet")));
    }

    @Test
    @WithMockCustomer(customerId = 1L, tckn = "12345678901", isEmployee = false)
    void getWalletById_ShouldReturnWallet_WhenCustomerRequestsOwnWallet() throws Exception {
        // Arrange
        when(customerService.isEmployee(anyLong())).thenReturn(false);
        when(walletService.getWalletById(anyLong())).thenReturn(walletDto);

        // Act & Assert
        mockMvc.perform(get("/api/wallets/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result", is("SUCCESS")))
            .andExpect(jsonPath("$.message", is("Wallet retrieved successfully")))
            .andExpect(jsonPath("$.data.id", is(1)));
    }

    @Test
    @WithMockCustomer(customerId = 2L, tckn = "98765432109", isEmployee = false)
    void getWalletById_ShouldReturnForbidden_WhenCustomerRequestsOtherWallet() throws Exception {
        // Arrange
        when(customerService.isEmployee(anyLong())).thenReturn(false);
        when(walletService.getWalletById(anyLong())).thenReturn(walletDto);

        // Act & Assert
        mockMvc.perform(get("/api/wallets/1"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.result", is("ERROR")))
            .andExpect(jsonPath("$.message", is("You can only view your own wallets unless you are an employee")))
            .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @WithMockCustomer(customerId = 1L, tckn = "12345678901", isEmployee = false)
    void getWalletById_ShouldReturnNotFound_WhenWalletDoesNotExist() throws Exception {
        // Arrange
        when(customerService.isEmployee(anyLong())).thenReturn(false);
        when(walletService.getWalletById(anyLong()))
            .thenThrow(new ResourceNotFoundException("Wallet", "id", 1L));

        // Act & Assert
        mockMvc.perform(get("/api/wallets/1"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.result", is("ERROR")))
            .andExpect(jsonPath("$.message", is("Wallet not found with id: '1'")))
            .andExpect(jsonPath("$.data").doesNotExist());
    }
} 