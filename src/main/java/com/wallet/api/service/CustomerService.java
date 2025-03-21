package com.wallet.api.service;

import com.wallet.api.dto.CustomerDto;

import java.util.List;

public interface CustomerService {
    
    CustomerDto createCustomer(CustomerDto customerDto);
    
    CustomerDto getCustomerById(Long id);
    
    CustomerDto getCustomerByTckn(String tckn);
    
    List<CustomerDto> getAllCustomers();
    
    boolean isEmployee(Long customerId);
} 