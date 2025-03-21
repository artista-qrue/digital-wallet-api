package com.wallet.api.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.api.dto.CustomerDto;
import com.wallet.api.exception.ResourceNotFoundException;
import com.wallet.api.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
public class CustomerControllerTest {

    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    @Autowired
    private WebApplicationContext context;

    private ObjectMapper objectMapper;
    private CustomerDto customerDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        objectMapper = new ObjectMapper();

        customerDto = new CustomerDto(
            1L,
            "John",
            "Doe",
            "12345678901",
            true
        );
    }

    @Test
    void createCustomer_ShouldReturnCreatedCustomer_WhenValidInput() throws Exception {
        when(customerService.createCustomer(any(CustomerDto.class))).thenReturn(customerDto);

        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customerDto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.result", is("SUCCESS")))
            .andExpect(jsonPath("$.message", is("Customer created successfully")))
            .andExpect(jsonPath("$.data.id", is(1)))
            .andExpect(jsonPath("$.data.name", is("John")))
            .andExpect(jsonPath("$.data.surname", is("Doe")))
            .andExpect(jsonPath("$.data.tckn", is("12345678901")))
            .andExpect(jsonPath("$.data.isEmployee", is(true)));
    }

    @Test
    void createCustomer_ShouldReturnBadRequest_WhenTcknExists() throws Exception {
        when(customerService.createCustomer(any(CustomerDto.class)))
            .thenThrow(new IllegalArgumentException("Customer with TCKN 12345678901 already exists"));
        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customerDto)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.result", is("ERROR")))
            .andExpect(jsonPath("$.message", is("Customer with TCKN 12345678901 already exists")))
            .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void getCustomerById_ShouldReturnCustomer_WhenIdExists() throws Exception {
        when(customerService.getCustomerById(anyLong())).thenReturn(customerDto);

        mockMvc.perform(get("/api/customers/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result", is("SUCCESS")))
            .andExpect(jsonPath("$.message", is("Customer retrieved successfully")))
            .andExpect(jsonPath("$.data.id", is(1)))
            .andExpect(jsonPath("$.data.name", is("John")))
            .andExpect(jsonPath("$.data.surname", is("Doe")))
            .andExpect(jsonPath("$.data.tckn", is("12345678901")))
            .andExpect(jsonPath("$.data.isEmployee", is(true)));
    }

    @Test
    void getCustomerById_ShouldReturnNotFound_WhenIdDoesNotExist() throws Exception {
        when(customerService.getCustomerById(anyLong()))
            .thenThrow(new ResourceNotFoundException("Customer", "id", 1L));

        mockMvc.perform(get("/api/customers/1"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.result", is("ERROR")))
            .andExpect(jsonPath("$.message", is("Customer not found with id: '1'")))
            .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void getCustomerByTckn_ShouldReturnCustomer_WhenTcknExists() throws Exception {
        when(customerService.getCustomerByTckn(anyString())).thenReturn(customerDto);

        mockMvc.perform(get("/api/customers/tckn/12345678901"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result", is("SUCCESS")))
            .andExpect(jsonPath("$.message", is("Customer retrieved successfully")))
            .andExpect(jsonPath("$.data.id", is(1)))
            .andExpect(jsonPath("$.data.name", is("John")))
            .andExpect(jsonPath("$.data.surname", is("Doe")))
            .andExpect(jsonPath("$.data.tckn", is("12345678901")))
            .andExpect(jsonPath("$.data.isEmployee", is(true)));
    }

    @Test
    void getCustomerByTckn_ShouldReturnNotFound_WhenTcknDoesNotExist() throws Exception {
        when(customerService.getCustomerByTckn(anyString()))
            .thenThrow(new ResourceNotFoundException("Customer", "tckn", "12345678901"));

        mockMvc.perform(get("/api/customers/tckn/12345678901"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.result", is("ERROR")))
            .andExpect(jsonPath("$.message", is("Customer not found with tckn: '12345678901'")))
            .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void getAllCustomers_ShouldReturnAllCustomers() throws Exception {
        CustomerDto customer2 = new CustomerDto(
            2L,
            "Jane",
            "Doe",
            "98765432109",
            false
        );
        when(customerService.getAllCustomers()).thenReturn(List.of(customerDto, customer2));

        mockMvc.perform(get("/api/customers"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result", is("SUCCESS")))
            .andExpect(jsonPath("$.message", is("Customers retrieved successfully")))
            .andExpect(jsonPath("$.data", hasSize(2)))
            .andExpect(jsonPath("$.data[0].id", is(1)))
            .andExpect(jsonPath("$.data[1].id", is(2)));
    }
} 