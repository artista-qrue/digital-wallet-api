package com.wallet.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "transactions")
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;
    
    @NotNull(message = "Amount cannot be null")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    @NotNull(message = "Transaction type cannot be null")
    @Enumerated(EnumType.STRING)
    private TransactionType type;
    
    @NotNull(message = "Opposite party type cannot be null")
    @Enumerated(EnumType.STRING)
    private OppositePartyType oppositePartyType;
    
    @NotNull(message = "Opposite party cannot be null")
    private String oppositeParty;
    
    @NotNull(message = "Status cannot be null")
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;
    
    @NotNull(message = "Transaction date cannot be null")
    private LocalDateTime transactionDate = LocalDateTime.now();
    
    public enum TransactionType {
        DEPOSIT, WITHDRAW
    }
    
    public enum OppositePartyType {
        IBAN, PAYMENT
    }
    
    public enum TransactionStatus {
        PENDING, APPROVED, DENIED
    }
} 