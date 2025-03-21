package com.wallet.api.service;

import com.wallet.api.dto.CreateWalletRequest;
import com.wallet.api.dto.WalletDto;
import com.wallet.api.entity.Wallet;

import java.util.List;

public interface WalletService {
    
    WalletDto createWallet(CreateWalletRequest request);
    
    List<WalletDto> getWalletsByCustomerId(Long customerId);
    
    List<WalletDto> getWalletsByCustomerIdAndCurrency(Long customerId, Wallet.Currency currency);
    
    WalletDto getWalletById(Long id);
    
    WalletDto getWalletByIdAndCustomerId(Long id, Long customerId);
} 