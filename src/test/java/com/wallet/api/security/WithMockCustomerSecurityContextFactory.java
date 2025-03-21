package com.wallet.api.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.Arrays;
import java.util.Collections;

public class WithMockCustomerSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomer> {

    @Override
    public SecurityContext createSecurityContext(WithMockCustomer annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        
        // Create authorities based on whether the user is an employee
        var authorities = annotation.isEmployee() 
            ? Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"), new SimpleGrantedAuthority("ROLE_EMPLOYEE"))
            : Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        
        // Create a TestCustomerPrincipal
        TestCustomerPrincipal principal = new TestCustomerPrincipal(
            annotation.customerId(),
            annotation.tckn(),
            "password", // Password is not used in our tests
            annotation.isEmployee(),
            authorities
        );
        
        // Create an Authentication object with the principal
        Authentication auth = new UsernamePasswordAuthenticationToken(
            principal, 
            null, 
            authorities
        );
        
        // Set the authentication in the security context
        context.setAuthentication(auth);
        
        return context;
    }
} 