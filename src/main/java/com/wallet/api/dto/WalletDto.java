package com.wallet.api.dto;

import com.wallet.api.entity.Wallet;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record WalletDto(
        Long id,
        @NotBlank(message = "Wallet name cannot be blank") String walletName,
        @NotNull(message = "Currency cannot be null") Wallet.Currency currency,
        @NotNull(message = "Active for shopping flag cannot be null") Boolean activeForShopping,
        @NotNull(message = "Active for withdraw flag cannot be null") Boolean activeForWithdraw,
        BigDecimal balance,
        BigDecimal usableBalance,
        Long customerId
) {
    public WalletDto {
        if (walletName != null) {
            walletName = walletName.trim();
        }
        if (balance == null) {
            balance = BigDecimal.ZERO;
        }
        if (usableBalance == null) {
            usableBalance = BigDecimal.ZERO;
        }
    }
} 