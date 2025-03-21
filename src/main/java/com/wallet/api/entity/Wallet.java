package com.wallet.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Data
@Table(name = "wallets")
public class Wallet {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotEmpty(message = "Wallet name cannot be empty")
    private String walletName;
    
    @NotNull(message = "Currency cannot be null")
    @Enumerated(EnumType.STRING)
    private Currency currency;
    
    @NotNull(message = "Active for shopping flag cannot be null")
    private Boolean activeForShopping;
    
    @NotNull(message = "Active for withdraw flag cannot be null")
    private Boolean activeForWithdraw;
    
    @NotNull(message = "Balance cannot be null")
    private BigDecimal balance = BigDecimal.ZERO;
    
    @NotNull(message = "Usable balance cannot be null")
    private BigDecimal usableBalance = BigDecimal.ZERO;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    @OneToMany(mappedBy = "wallet", fetch = FetchType.LAZY)
    private List<Transaction> transactions;
    
    public enum Currency {
        TRY, USD, EUR
    }
} 