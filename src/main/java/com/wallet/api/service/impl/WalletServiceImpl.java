package com.wallet.api.service.impl;

import com.wallet.api.dto.CreateWalletRequest;
import com.wallet.api.dto.WalletDto;
import com.wallet.api.entity.Customer;
import com.wallet.api.entity.Wallet;
import com.wallet.api.exception.ResourceNotFoundException;
import com.wallet.api.repository.CustomerRepository;
import com.wallet.api.repository.WalletRepository;
import com.wallet.api.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WalletServiceImpl implements WalletService {
    
    @Autowired
    private WalletRepository walletRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Override
    public WalletDto createWallet(CreateWalletRequest request) {
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", request.customerId()));
        
        Wallet wallet = new Wallet();
        wallet.setWalletName(request.walletName());
        wallet.setCurrency(request.currency());
        wallet.setActiveForShopping(request.activeForShopping());
        wallet.setActiveForWithdraw(request.activeForWithdraw());
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setUsableBalance(BigDecimal.ZERO);
        wallet.setCustomer(customer);
        
        Wallet savedWallet = walletRepository.save(wallet);
        return mapToDto(savedWallet);
    }
    
    @Override
    public List<WalletDto> getWalletsByCustomerId(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer", "id", customerId);
        }
        
        return walletRepository.findByCustomerId(customerId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<WalletDto> getWalletsByCustomerIdAndCurrency(Long customerId, Wallet.Currency currency) {
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer", "id", customerId);
        }
        
        return walletRepository.findByCustomerIdAndCurrency(customerId, currency).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public WalletDto getWalletById(Long id) {
        Wallet wallet = walletRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet", "id", id));
        return mapToDto(wallet);
    }
    
    @Override
    public WalletDto getWalletByIdAndCustomerId(Long id, Long customerId) {
        Wallet wallet = walletRepository.findByIdAndCustomerId(id, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet", "id", id + " for customer " + customerId));
        return mapToDto(wallet);
    }
    
    private WalletDto mapToDto(Wallet wallet) {
        return new WalletDto(
                wallet.getId(),
                wallet.getWalletName(),
                wallet.getCurrency(),
                wallet.getActiveForShopping(),
                wallet.getActiveForWithdraw(),
                wallet.getBalance(),
                wallet.getUsableBalance(),
                wallet.getCustomer().getId()
        );
    }
} 