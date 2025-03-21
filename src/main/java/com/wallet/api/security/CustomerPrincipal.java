package com.wallet.api.security;

import com.wallet.api.entity.Customer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomerPrincipal implements UserDetails {
    private final Customer customer;

    public CustomerPrincipal(Customer customer) {
        this.customer = customer;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (customer.isEmployee()) {
            return Collections.singletonList(new SimpleGrantedAuthority("ROLE_EMPLOYEE"));
        } else {
            return Collections.singletonList(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
        }
    }

    @Override
    public String getPassword() {
        // In this system, we don't have passwords, but UserDetails requires this
        // In a real system, you would return the customer's password
        return "";
    }

    @Override
    public String getUsername() {
        // We use the customer ID as the username for JWT
        return String.valueOf(customer.getId());
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public boolean isEmployee() {
        return customer.isEmployee();
    }
    
    public Long getCustomerId() {
        return customer.getId();
    }
    
    public Customer getCustomer() {
        return customer;
    }
} 