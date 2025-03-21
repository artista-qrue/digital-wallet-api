package com.wallet.api.controller;

import com.wallet.api.dto.CustomerDto;
import com.wallet.api.model.ApiResponse;
import com.wallet.api.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    
    @Autowired
    private CustomerService customerService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<CustomerDto>> createCustomer(@Valid @RequestBody CustomerDto customerDto) {
        try {
            CustomerDto createdCustomer = customerService.createCustomer(customerDto);
            return new ResponseEntity<>(
                    ApiResponse.success("Customer created successfully", createdCustomer),
                    HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(
                    ApiResponse.error(e.getMessage()),
                    HttpStatus.BAD_REQUEST);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerDto>> getCustomerById(@PathVariable Long id) {
        try {
            CustomerDto customer = customerService.getCustomerById(id);
            return ResponseEntity.ok(ApiResponse.success("Customer retrieved successfully", customer));
        } catch (Exception e) {
            return new ResponseEntity<>(
                    ApiResponse.error(e.getMessage()),
                    HttpStatus.NOT_FOUND);
        }
    }
    
    @GetMapping("/tckn/{tckn}")
    public ResponseEntity<ApiResponse<CustomerDto>> getCustomerByTckn(@PathVariable String tckn) {
        try {
            CustomerDto customer = customerService.getCustomerByTckn(tckn);
            return ResponseEntity.ok(ApiResponse.success("Customer retrieved successfully", customer));
        } catch (Exception e) {
            return new ResponseEntity<>(
                    ApiResponse.error(e.getMessage()),
                    HttpStatus.NOT_FOUND);
        }
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerDto>>> getAllCustomers() {
        try {
            List<CustomerDto> customers = customerService.getAllCustomers();
            return ResponseEntity.ok(ApiResponse.success("Customers retrieved successfully", customers));
        } catch (Exception e) {
            return new ResponseEntity<>(
                    ApiResponse.error(e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
} 