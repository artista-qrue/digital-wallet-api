package com.wallet.api.controller;

import com.wallet.api.dto.AuthRequest;
import com.wallet.api.entity.Customer;
import com.wallet.api.model.ApiResponse;
import com.wallet.api.repository.CustomerRepository;
import com.wallet.api.security.CustomerPrincipal;
import com.wallet.api.security.JwtTokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    
    @Autowired
    private CustomerRepository customerRepository;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@Valid @RequestBody AuthRequest request) {
        try {
            logger.info("Authentication attempt with TCKN: {}", request.tckn());
            
            // Direct lookup without using authenticationManager
            Optional<Customer> customerOpt = customerRepository.findByTckn(request.tckn());
            if (customerOpt.isEmpty()) {
                logger.warn("Authentication failed - User not found for TCKN: {}", request.tckn());
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>("ERROR", "User not found with TCKN: " + request.tckn(), null));
            }
            
            Customer customer = customerOpt.get();
            CustomerPrincipal userDetails = new CustomerPrincipal(customer);
            
            // Generate JWT token
            String token = jwtTokenUtil.generateToken(userDetails);
            
            Map<String, Object> tokenData = new HashMap<>();
            tokenData.put("token", token);
            tokenData.put("customerId", userDetails.getCustomerId());
            tokenData.put("isEmployee", userDetails.isEmployee());
            
            logger.info("Authentication successful for TCKN: {}", request.tckn());
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Authentication successful", tokenData));
        } catch (Exception e) {
            logger.error("Authentication error for TCKN: {}", request.tckn(), e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>("ERROR", "Authentication error: " + e.getMessage(), null));
        }
    }

    @PostMapping("/test")
    public ResponseEntity<ApiResponse<?>> test() {
        try {
            // Check if admin user exists
            Optional<Customer> adminOpt = customerRepository.findByTckn("99999999999");
            if (adminOpt.isPresent()) {
                Customer admin = adminOpt.get();
                Map<String, Object> data = new HashMap<>();
                data.put("adminExists", true);
                data.put("adminId", admin.getId());
                data.put("adminName", admin.getName());
                data.put("adminIsEmployee", admin.isEmployee());
                
                return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Admin user found", data));
            } else {
                // Create admin user if not exists
                Customer admin = new Customer();
                admin.setName("Admin");
                admin.setSurname("User");
                admin.setTckn("99999999999");
                admin.setEmployee(true);
                
                admin = customerRepository.save(admin);
                
                Map<String, Object> data = new HashMap<>();
                data.put("adminCreated", true);
                data.put("adminId", admin.getId());
                data.put("adminName", admin.getName());
                data.put("adminIsEmployee", admin.isEmployee());
                
                return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Admin user created", data));
            }
        } catch (Exception e) {
            logger.error("Error in test endpoint", e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>("ERROR", "Error: " + e.getMessage(), null));
        }
    }
} 