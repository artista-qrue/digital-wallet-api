package com.wallet.api.controller;

import com.wallet.api.entity.Customer;
import com.wallet.api.model.ApiResponse;
import com.wallet.api.repository.CustomerRepository;
import com.wallet.api.security.JwtTokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/setup")
public class SetupController {
    private static final Logger logger = LoggerFactory.getLogger(SetupController.class);

    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @GetMapping("/init")
    public ResponseEntity<ApiResponse<?>> initializeAdmin() {
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
                data.put("message", "Admin user already exists");
                
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
                data.put("message", "Admin user created successfully");
                data.put("loginInfo", "Use TCKN 99999999999 to login at /api/auth/login");
                
                return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Admin user created", data));
            }
        } catch (Exception e) {
            logger.error("Error in initializing admin user", e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>("ERROR", "Error: " + e.getMessage(), null));
        }
    }

    @PostMapping("/customer")
    public ResponseEntity<ApiResponse<?>> createCustomer() {
        try {
            // Create a regular customer
            Customer customer = new Customer();
            customer.setName("John");
            customer.setSurname("Doe");
            customer.setTckn("12345678901");
            customer.setEmployee(false);
            
            customer = customerRepository.save(customer);
            
            Map<String, Object> data = new HashMap<>();
            data.put("customerId", customer.getId());
            data.put("tckn", customer.getTckn());
            data.put("name", customer.getName());
            data.put("surname", customer.getSurname());
            data.put("isEmployee", customer.isEmployee());
            
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Customer created", data));
        } catch (Exception e) {
            logger.error("Error creating customer", e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>("ERROR", "Error: " + e.getMessage(), null));
        }
    }

    @PostMapping("/direct-token")
    public ResponseEntity<ApiResponse<?>> generateDirectToken() {
        try {
            // Check if admin user exists
            Optional<Customer> adminOpt = customerRepository.findByTckn("99999999999");
            if (adminOpt.isEmpty()) {
                // Create admin user if not exists
                Customer admin = new Customer();
                admin.setName("Admin");
                admin.setSurname("User");
                admin.setTckn("99999999999");
                admin.setEmployee(true);
                
                admin = customerRepository.save(admin);
            }
            
            Customer admin = adminOpt.orElseGet(() -> customerRepository.findByTckn("99999999999").get());
            
            // Generate JWT token directly
            String token = jwtTokenUtil.generateToken(admin.getId(), admin.isEmployee());
            
            Map<String, Object> tokenData = new HashMap<>();
            tokenData.put("token", token);
            tokenData.put("customerId", admin.getId());
            tokenData.put("isEmployee", admin.isEmployee());
            
            logger.info("Direct token generation successful for admin");
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Token generated successfully", tokenData));
        } catch (Exception e) {
            logger.error("Error generating direct token", e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>("ERROR", "Error: " + e.getMessage(), null));
        }
    }
} 