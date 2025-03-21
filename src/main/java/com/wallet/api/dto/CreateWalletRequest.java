package com.wallet.api.dto;

import com.wallet.api.entity.Wallet;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateWalletRequest(
        @NotBlank(message = "Wallet name cannot be blank") String walletName,
        @NotNull(message = "Currency cannot be null") Wallet.Currency currency,
        @NotNull(message = "Active for shopping flag cannot be null") Boolean activeForShopping,
        @NotNull(message = "Active for withdraw flag cannot be null") Boolean activeForWithdraw,
        @NotNull(message = "Customer ID cannot be null") Long customerId
) {
    public CreateWalletRequest {
        if (walletName != null) {
            walletName = walletName.trim();
        }
    }
} 