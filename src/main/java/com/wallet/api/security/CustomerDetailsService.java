package com.wallet.api.security;

import com.wallet.api.entity.Customer;
import com.wallet.api.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomerDetailsService implements UserDetailsService {

    @Autowired
    private CustomerRepository customerRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // In our case, username is the customer ID
        Long customerId;
        try {
            customerId = Long.parseLong(username);
        } catch (NumberFormatException e) {
            throw new UsernameNotFoundException("Invalid customer ID format");
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new UsernameNotFoundException("Customer not found with ID: " + customerId));

        return new CustomerPrincipal(customer);
    }
    
    public UserDetails loadUserByTckn(String tckn) throws UsernameNotFoundException {
        Customer customer = customerRepository.findByTckn(tckn)
                .orElseThrow(() -> new UsernameNotFoundException("Customer not found with TCKN: " + tckn));

        return new CustomerPrincipal(customer);
    }
} 