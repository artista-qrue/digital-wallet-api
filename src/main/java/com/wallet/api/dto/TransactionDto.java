package com.wallet.api.dto;

import com.wallet.api.entity.Transaction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionDto(
        Long id,
        @NotNull(message = "Wallet ID cannot be null") Long walletId,
        @Positive(message = "Amount must be positive") @NotNull(message = "Amount cannot be null") BigDecimal amount,
        @NotNull(message = "Transaction type cannot be null") Transaction.TransactionType type,
        @NotNull(message = "Opposite party type cannot be null") Transaction.OppositePartyType oppositePartyType,
        @NotBlank(message = "Opposite party cannot be blank") String oppositeParty,
        Transaction.TransactionStatus status,
        LocalDateTime transactionDate
) {
    public TransactionDto {
        if (oppositeParty != null) {
            oppositeParty = oppositeParty.trim();
        }
        if (status == null) {
            status = Transaction.TransactionStatus.PENDING;
        }
        if (transactionDate == null) {
            transactionDate = LocalDateTime.now();
        }
    }
} 