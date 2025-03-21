package com.wallet.api.service.impl;

import com.wallet.api.dto.CustomerDto;
import com.wallet.api.entity.Customer;
import com.wallet.api.exception.ResourceNotFoundException;
import com.wallet.api.repository.CustomerRepository;
import com.wallet.api.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerServiceImpl implements CustomerService {
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Override
    public CustomerDto createCustomer(CustomerDto customerDto) {
        if (customerRepository.existsByTckn(customerDto.tckn())) {
            throw new IllegalArgumentException("Customer with TCKN " + customerDto.tckn() + " already exists");
        }
        
        Customer customer = new Customer();
        customer.setName(customerDto.name());
        customer.setSurname(customerDto.surname());
        customer.setTckn(customerDto.tckn());
        customer.setEmployee(customerDto.isEmployee());
        
        Customer savedCustomer = customerRepository.save(customer);
        return mapToDto(savedCustomer);
    }
    
    @Override
    public CustomerDto getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
        return mapToDto(customer);
    }
    
    @Override
    public CustomerDto getCustomerByTckn(String tckn) {
        Customer customer = customerRepository.findByTckn(tckn)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "tckn", tckn));
        return mapToDto(customer);
    }
    
    @Override
    public List<CustomerDto> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean isEmployee(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));
        return customer.isEmployee();
    }
    
    private CustomerDto mapToDto(Customer customer) {
        return new CustomerDto(
                customer.getId(),
                customer.getName(),
                customer.getSurname(),
                customer.getTckn(),
                customer.isEmployee()
        );
    }
} 