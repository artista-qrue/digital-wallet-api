package com.wallet.api.dto;

import jakarta.validation.constraints.NotBlank;

public record CustomerDto(
        Long id,
        @NotBlank(message = "Name cannot be blank") String name,
        @NotBlank(message = "Surname cannot be blank") String surname,
        @NotBlank(message = "TCKN cannot be blank") String tckn,
        Boolean isEmployee
) {
    public CustomerDto {
        if (name != null) {
            name = name.trim();
        }
        if (surname != null) {
            surname = surname.trim();
        }
        if (tckn != null) {
            tckn = tckn.trim();
        }
        if (isEmployee == null) {
            isEmployee = false;
        }
    }
} 