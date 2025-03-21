package com.wallet.api.controller;

import com.wallet.api.dto.*;
import com.wallet.api.entity.Transaction;
import com.wallet.api.entity.Wallet;
import com.wallet.api.model.ApiResponse;
import com.wallet.api.security.CustomerPrincipal;
import com.wallet.api.service.CustomerService;
import com.wallet.api.service.TransactionService;
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
@RequestMapping("/api/transactions")
public class TransactionController {
    
    @Autowired
    private TransactionService transactionService;
    
    @Autowired
    private WalletService walletService;
    
    @Autowired
    private CustomerService customerService;
    
    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse<TransactionDto>> deposit(
            @Valid @RequestBody DepositRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomerPrincipal customerPrincipal = (CustomerPrincipal) authentication.getPrincipal();
            Long requestingCustomerId = customerPrincipal.getCustomerId();
            
            WalletDto wallet = walletService.getWalletById(request.walletId());
            
            // Check if the requesting customer is an employee or owns the wallet
            if (!customerService.isEmployee(requestingCustomerId) && !requestingCustomerId.equals(wallet.customerId())) {
                return new ResponseEntity<>(
                        ApiResponse.error("You can only deposit to your own wallets unless you are an employee"),
                        HttpStatus.FORBIDDEN);
            }
            
            TransactionDto transaction = transactionService.deposit(request);
            return new ResponseEntity<>(
                    ApiResponse.success("Deposit created successfully", transaction),
                    HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(
                    ApiResponse.error(e.getMessage()),
                    HttpStatus.BAD_REQUEST);
        }
    }
    
    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<TransactionDto>> withdraw(
            @Valid @RequestBody WithdrawRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomerPrincipal customerPrincipal = (CustomerPrincipal) authentication.getPrincipal();
            Long requestingCustomerId = customerPrincipal.getCustomerId();
            
            WalletDto wallet = walletService.getWalletById(request.walletId());
            
            // Check if the requesting customer is an employee or owns the wallet
            if (!customerService.isEmployee(requestingCustomerId) && !requestingCustomerId.equals(wallet.customerId())) {
                return new ResponseEntity<>(
                        ApiResponse.error("You can only withdraw from your own wallets unless you are an employee"),
                        HttpStatus.FORBIDDEN);
            }
            
            TransactionDto transaction = transactionService.withdraw(request);
            return new ResponseEntity<>(
                    ApiResponse.success("Withdrawal created successfully", transaction),
                    HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(
                    ApiResponse.error(e.getMessage()),
                    HttpStatus.BAD_REQUEST);
        }
    }
    
    @PostMapping("/approve")
    public ResponseEntity<ApiResponse<TransactionDto>> approveTransaction(
            @Valid @RequestBody ApproveTransactionRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomerPrincipal customerPrincipal = (CustomerPrincipal) authentication.getPrincipal();
            Long requestingCustomerId = customerPrincipal.getCustomerId();
            
            // Only employees can approve transactions
            if (!customerService.isEmployee(requestingCustomerId)) {
                return new ResponseEntity<>(
                        ApiResponse.error("Only employees can approve transactions"),
                        HttpStatus.FORBIDDEN);
            }
            
            TransactionDto transaction = transactionService.approveTransaction(request);
            return ResponseEntity.ok(ApiResponse.success("Transaction status updated successfully", transaction));
        } catch (Exception e) {
            return new ResponseEntity<>(
                    ApiResponse.error(e.getMessage()),
                    HttpStatus.BAD_REQUEST);
        }
    }
    
    @GetMapping("/wallet/{walletId}")
    public ResponseEntity<ApiResponse<List<TransactionDto>>> getTransactionsByWalletId(
            @PathVariable Long walletId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomerPrincipal customerPrincipal = (CustomerPrincipal) authentication.getPrincipal();
            Long requestingCustomerId = customerPrincipal.getCustomerId();
            
            WalletDto wallet = walletService.getWalletById(walletId);
            
            // Check if the requesting customer is an employee or owns the wallet
            if (!customerService.isEmployee(requestingCustomerId) && !requestingCustomerId.equals(wallet.customerId())) {
                return new ResponseEntity<>(
                        ApiResponse.error("You can only view transactions for your own wallets unless you are an employee"),
                        HttpStatus.FORBIDDEN);
            }
            
            List<TransactionDto> transactions = transactionService.getTransactionsByWalletId(walletId);
            return ResponseEntity.ok(ApiResponse.success("Transactions retrieved successfully", transactions));
        } catch (Exception e) {
            return new ResponseEntity<>(
                    ApiResponse.error(e.getMessage()),
                    HttpStatus.NOT_FOUND);
        }
    }
    
    @GetMapping("/wallet/{walletId}/type/{type}")
    public ResponseEntity<ApiResponse<List<TransactionDto>>> getTransactionsByWalletIdAndType(
            @PathVariable Long walletId,
            @PathVariable Transaction.TransactionType type) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomerPrincipal customerPrincipal = (CustomerPrincipal) authentication.getPrincipal();
            Long requestingCustomerId = customerPrincipal.getCustomerId();
            
            WalletDto wallet = walletService.getWalletById(walletId);
            
            // Check if the requesting customer is an employee or owns the wallet
            if (!customerService.isEmployee(requestingCustomerId) && !requestingCustomerId.equals(wallet.customerId())) {
                return new ResponseEntity<>(
                        ApiResponse.error("You can only view transactions for your own wallets unless you are an employee"),
                        HttpStatus.FORBIDDEN);
            }
            
            List<TransactionDto> transactions = transactionService.getTransactionsByWalletIdAndType(walletId, type);
            return ResponseEntity.ok(ApiResponse.success("Transactions retrieved successfully", transactions));
        } catch (Exception e) {
            return new ResponseEntity<>(
                    ApiResponse.error(e.getMessage()),
                    HttpStatus.NOT_FOUND);
        }
    }
    
    @GetMapping("/wallet/{walletId}/status/{status}")
    public ResponseEntity<ApiResponse<List<TransactionDto>>> getTransactionsByWalletIdAndStatus(
            @PathVariable Long walletId,
            @PathVariable Transaction.TransactionStatus status) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomerPrincipal customerPrincipal = (CustomerPrincipal) authentication.getPrincipal();
            Long requestingCustomerId = customerPrincipal.getCustomerId();
            
            WalletDto wallet = walletService.getWalletById(walletId);
            
            // Check if the requesting customer is an employee or owns the wallet
            if (!customerService.isEmployee(requestingCustomerId) && !requestingCustomerId.equals(wallet.customerId())) {
                return new ResponseEntity<>(
                        ApiResponse.error("You can only view transactions for your own wallets unless you are an employee"),
                        HttpStatus.FORBIDDEN);
            }
            
            List<TransactionDto> transactions = transactionService.getTransactionsByWalletIdAndStatus(walletId, status);
            return ResponseEntity.ok(ApiResponse.success("Transactions retrieved successfully", transactions));
        } catch (Exception e) {
            return new ResponseEntity<>(
                    ApiResponse.error(e.getMessage()),
                    HttpStatus.NOT_FOUND);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionDto>> getTransactionById(@PathVariable Long id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomerPrincipal customerPrincipal = (CustomerPrincipal) authentication.getPrincipal();
            Long requestingCustomerId = customerPrincipal.getCustomerId();
            
            TransactionDto transaction = transactionService.getTransactionById(id);
            WalletDto wallet = walletService.getWalletById(transaction.walletId());
            
            // Check if the requesting customer is an employee or owns the wallet
            if (!customerService.isEmployee(requestingCustomerId) && !requestingCustomerId.equals(wallet.customerId())) {
                return new ResponseEntity<>(
                        ApiResponse.error("You can only view transactions for your own wallets unless you are an employee"),
                        HttpStatus.FORBIDDEN);
            }
            
            return ResponseEntity.ok(ApiResponse.success("Transaction retrieved successfully", transaction));
        } catch (Exception e) {
            return new ResponseEntity<>(
                    ApiResponse.error(e.getMessage()),
                    HttpStatus.NOT_FOUND);
        }
    }
} 