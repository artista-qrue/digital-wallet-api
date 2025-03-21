package com.wallet.api.dto;

import com.wallet.api.entity.Transaction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record WithdrawRequest(
        @Positive(message = "Amount must be positive") @NotNull(message = "Amount cannot be null") BigDecimal amount,
        @NotNull(message = "Wallet ID cannot be null") Long walletId,
        @NotBlank(message = "Destination cannot be blank") String destination,
        @NotNull(message = "Opposite party type cannot be null") Transaction.OppositePartyType destinationType
) {
    public WithdrawRequest {
        if (destination != null) {
            destination = destination.trim();
        }
    }
} 