package com.wallet.api.config;

import com.wallet.api.entity.Customer;
import com.wallet.api.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class DataInitializer {

    @Autowired
    private CustomerRepository customerRepository;

    @Bean
    @Profile("!test") // Don't run during tests
    public CommandLineRunner initDatabase() {
        return args -> {
            // Check if we already have the admin customer
            if (customerRepository.findByTckn("99999999999").isEmpty()) {
                // Create admin customer
                Customer admin = new Customer();
                admin.setName("Admin");
                admin.setSurname("User");
                admin.setTckn("99999999999");
                admin.setEmployee(true);
                
                customerRepository.save(admin);
                
                System.out.println("Initial admin user created with TCKN: 99999999999");
            }
        };
    }
} 