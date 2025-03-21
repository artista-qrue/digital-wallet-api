package com.wallet.api.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthRequest(
    @NotBlank(message = "TCKN is required")
    String tckn
) {
    public AuthRequest {
        if (tckn != null) {
            tckn = tckn.trim();
        }
    }
} 