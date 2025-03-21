package com.wallet.api.security;

import com.wallet.api.entity.Customer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

public class TestCustomerPrincipal extends CustomerPrincipal {
    private final Long customerId;
    private final String tckn;
    private final boolean employee;
    private final Collection<? extends GrantedAuthority> authorities;

    public TestCustomerPrincipal(Long customerId, String tckn, String password, boolean employee, 
                                Collection<? extends GrantedAuthority> authorities) {
        super(createMockCustomer(customerId, tckn, employee));
        this.customerId = customerId;
        this.tckn = tckn;
        this.employee = employee;
        this.authorities = authorities;
    }

    private static Customer createMockCustomer(Long customerId, String tckn, boolean employee) {
        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setTckn(tckn);
        customer.setEmployee(employee);
        customer.setName("Test User");
        customer.setSurname("Test Surname");
        return customer;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getUsername() {
        return String.valueOf(customerId);
    }

    @Override
    public boolean isEmployee() {
        return employee;
    }

    @Override
    public Long getCustomerId() {
        return customerId;
    }
} 