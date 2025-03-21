package com.wallet.api.exception;

public class InsufficientBalanceException extends RuntimeException {
    
    public InsufficientBalanceException(String message) {
        super(message);
    }
    
    public InsufficientBalanceException(Long walletId, Double required, Double available) {
        super(String.format("Wallet with ID %d has insufficient balance. Required: %.2f, Available: %.2f", 
                walletId, required, available));
    }
} 