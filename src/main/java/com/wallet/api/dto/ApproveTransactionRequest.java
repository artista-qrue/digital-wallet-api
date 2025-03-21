package com.wallet.api.dto;

import com.wallet.api.entity.Transaction;
import jakarta.validation.constraints.NotNull;

public record ApproveTransactionRequest(
        @NotNull(message = "Transaction ID cannot be null") Long transactionId,
        @NotNull(message = "Status cannot be null") Transaction.TransactionStatus status
) {
    public ApproveTransactionRequest {
        if (status == null) {
            status = Transaction.TransactionStatus.PENDING;
        } else if (status == Transaction.TransactionStatus.PENDING) {
            throw new IllegalArgumentException("Status cannot be PENDING for approval");
        }
    }
} 