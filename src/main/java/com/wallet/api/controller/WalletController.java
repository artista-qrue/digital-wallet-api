package com.wallet.api.controller;

import com.wallet.api.dto.CreateWalletRequest;
import com.wallet.api.dto.WalletDto;
import com.wallet.api.entity.Wallet;
import com.wallet.api.model.ApiResponse;
import com.wallet.api.security.CustomerPrincipal;
import com.wallet.api.service.CustomerService;
import com.wallet.api.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {
    
    @Autowired
    private WalletService walletService;
    
    @Autowired
    private CustomerService customerService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<WalletDto>> createWallet(@Valid @RequestBody CreateWalletRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomerPrincipal customerPrincipal = (CustomerPrincipal) authentication.getPrincipal();
            Long requestingCustomerId = customerPrincipal.getCustomerId();
            
            Long targetCustomerId = request.customerId();
            
            // Check if the requesting customer is an employee or is creating their own wallet
            if (!customerService.isEmployee(requestingCustomerId) && !requestingCustomerId.equals(targetCustomerId)) {
                return new ResponseEntity<>(
                        ApiResponse.error("You can only create wallets for yourself unless you are an employee"),
                        HttpStatus.FORBIDDEN);
            }
            
            WalletDto createdWallet = walletService.createWallet(request);
            return new ResponseEntity<>(
                    ApiResponse.success("Wallet created successfully", createdWallet),
                    HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(
                    ApiResponse.error(e.getMessage()),
                    HttpStatus.BAD_REQUEST);
        }
    }
    
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<WalletDto>>> getWalletsByCustomerId(@PathVariable Long customerId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomerPrincipal customerPrincipal = (CustomerPrincipal) authentication.getPrincipal();
            Long requestingCustomerId = customerPrincipal.getCustomerId();
            
            // Check if the requesting customer is an employee or is querying their own wallets
            if (!customerService.isEmployee(requestingCustomerId) && !requestingCustomerId.equals(customerId)) {
                return new ResponseEntity<>(
                        ApiResponse.error("You can only view your own wallets unless you are an employee"),
                        HttpStatus.FORBIDDEN);
            }
            
            List<WalletDto> wallets = walletService.getWalletsByCustomerId(customerId);
            return ResponseEntity.ok(ApiResponse.success("Wallets retrieved successfully", wallets));
        } catch (Exception e) {
            return new ResponseEntity<>(
                    ApiResponse.error(e.getMessage()),
                    HttpStatus.NOT_FOUND);
        }
    }
    
    @GetMapping("/customer/{customerId}/currency/{currency}")
    public ResponseEntity<ApiResponse<List<WalletDto>>> getWalletsByCustomerIdAndCurrency(
            @PathVariable Long customerId,
            @PathVariable Wallet.Currency currency) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomerPrincipal customerPrincipal = (CustomerPrincipal) authentication.getPrincipal();
            Long requestingCustomerId = customerPrincipal.getCustomerId();
            
            // Check if the requesting customer is an employee or is querying their own wallets
            if (!customerService.isEmployee(requestingCustomerId) && !requestingCustomerId.equals(customerId)) {
                return new ResponseEntity<>(
                        ApiResponse.error("You can only view your own wallets unless you are an employee"),
                        HttpStatus.FORBIDDEN);
            }
            
            List<WalletDto> wallets = walletService.getWalletsByCustomerIdAndCurrency(customerId, currency);
            return ResponseEntity.ok(ApiResponse.success("Wallets retrieved successfully", wallets));
        } catch (Exception e) {
            return new ResponseEntity<>(
                    ApiResponse.error(e.getMessage()),
                    HttpStatus.NOT_FOUND);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WalletDto>> getWalletById(@PathVariable Long id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomerPrincipal customerPrincipal = (CustomerPrincipal) authentication.getPrincipal();
            Long requestingCustomerId = customerPrincipal.getCustomerId();
            
            WalletDto wallet = walletService.getWalletById(id);
            
            // Check if the requesting customer is an employee or is querying their own wallet
            if (!customerService.isEmployee(requestingCustomerId) && !requestingCustomerId.equals(wallet.customerId())) {
                return new ResponseEntity<>(
                        ApiResponse.error("You can only view your own wallets unless you are an employee"),
                        HttpStatus.FORBIDDEN);
            }
            
            return ResponseEntity.ok(ApiResponse.success("Wallet retrieved successfully", wallet));
        } catch (Exception e) {
            return new ResponseEntity<>(
                    ApiResponse.error(e.getMessage()),
                    HttpStatus.NOT_FOUND);
        }
    }
} 