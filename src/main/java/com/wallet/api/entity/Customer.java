package com.wallet.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Entity
@Data
@Table(name = "customers")
public class Customer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotEmpty(message = "Name cannot be empty")
    private String name;
    
    @NotEmpty(message = "Surname cannot be empty")
    private String surname;
    
    @NotEmpty(message = "TCKN cannot be empty")
    @Column(unique = true)
    private String tckn;
    
    private boolean isEmployee = false;
    
    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    private List<Wallet> wallets;
} 