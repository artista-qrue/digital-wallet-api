package com.wallet.api.service.impl;

import com.wallet.api.dto.*;
import com.wallet.api.entity.Transaction;
import com.wallet.api.entity.Wallet;
import com.wallet.api.exception.InsufficientBalanceException;
import com.wallet.api.exception.ResourceNotFoundException;
import com.wallet.api.repository.TransactionRepository;
import com.wallet.api.repository.WalletRepository;
import com.wallet.api.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionServiceImpl implements TransactionService {

    private static final BigDecimal THRESHOLD_AMOUNT = new BigDecimal("1000");
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private WalletRepository walletRepository;
    
    @Override
    @Transactional
    public TransactionDto deposit(DepositRequest request) {
        Wallet wallet = walletRepository.findById(request.walletId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet", "id", request.walletId()));
        
        Transaction transaction = new Transaction();
        transaction.setWallet(wallet);
        transaction.setAmount(request.amount());
        transaction.setType(Transaction.TransactionType.DEPOSIT);
        transaction.setOppositePartyType(request.sourceType());
        transaction.setOppositeParty(request.source());
        transaction.setTransactionDate(LocalDateTime.now());
        
        // Determine transaction status based on amount
        Transaction.TransactionStatus status = request.amount().compareTo(THRESHOLD_AMOUNT) > 0
                ? Transaction.TransactionStatus.PENDING
                : Transaction.TransactionStatus.APPROVED;
        transaction.setStatus(status);
        
        // Update wallet balances
        updateWalletBalancesForDeposit(wallet, request.amount(), status);
        
        Transaction savedTransaction = transactionRepository.save(transaction);
        return mapToDto(savedTransaction);
    }
    
    @Override
    @Transactional
    public TransactionDto withdraw(WithdrawRequest request) {
        Wallet wallet = walletRepository.findById(request.walletId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet", "id", request.walletId()));
        
        // Check if wallet is active for withdraw
        if (!wallet.getActiveForWithdraw()) {
            throw new IllegalArgumentException("Wallet is not active for withdrawals");
        }
        
        // Check if wallet has enough usable balance
        if (wallet.getUsableBalance().compareTo(request.amount()) < 0) {
            throw new InsufficientBalanceException(wallet.getId(),
                    request.amount().doubleValue(), wallet.getUsableBalance().doubleValue());
        }
        
        Transaction transaction = new Transaction();
        transaction.setWallet(wallet);
        transaction.setAmount(request.amount());
        transaction.setType(Transaction.TransactionType.WITHDRAW);
        transaction.setOppositePartyType(request.destinationType());
        transaction.setOppositeParty(request.destination());
        transaction.setTransactionDate(LocalDateTime.now());
        
        // Determine transaction status based on amount
        Transaction.TransactionStatus status = request.amount().compareTo(THRESHOLD_AMOUNT) > 0
                ? Transaction.TransactionStatus.PENDING
                : Transaction.TransactionStatus.APPROVED;
        transaction.setStatus(status);
        
        // Update wallet balances
        updateWalletBalancesForWithdraw(wallet, request.amount(), status);
        
        Transaction savedTransaction = transactionRepository.save(transaction);
        return mapToDto(savedTransaction);
    }
    
    @Override
    @Transactional
    public TransactionDto approveTransaction(ApproveTransactionRequest request) {
        Transaction transaction = transactionRepository.findById(request.transactionId())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", request.transactionId()));
        
        // Validate the transaction can be approved/denied
        if (transaction.getStatus() != Transaction.TransactionStatus.PENDING) {
            throw new IllegalArgumentException("Only pending transactions can be approved or denied");
        }
        
        // Update transaction status
        Transaction.TransactionStatus oldStatus = transaction.getStatus();
        transaction.setStatus(request.status());
        
        // Update wallet balances based on new status
        Wallet wallet = transaction.getWallet();
        if (transaction.getType() == Transaction.TransactionType.DEPOSIT) {
            handleDepositApproval(wallet, transaction.getAmount(), oldStatus, request.status());
        } else {
            handleWithdrawApproval(wallet, transaction.getAmount(), oldStatus, request.status());
        }
        
        Transaction updatedTransaction = transactionRepository.save(transaction);
        return mapToDto(updatedTransaction);
    }
    
    @Override
    public List<TransactionDto> getTransactionsByWalletId(Long walletId) {
        if (!walletRepository.existsById(walletId)) {
            throw new ResourceNotFoundException("Wallet", "id", walletId);
        }
        
        return transactionRepository.findByWalletId(walletId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<TransactionDto> getTransactionsByWalletIdAndType(Long walletId, Transaction.TransactionType type) {
        if (!walletRepository.existsById(walletId)) {
            throw new ResourceNotFoundException("Wallet", "id", walletId);
        }
        
        return transactionRepository.findByWalletIdAndType(walletId, type).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<TransactionDto> getTransactionsByWalletIdAndStatus(Long walletId, Transaction.TransactionStatus status) {
        if (!walletRepository.existsById(walletId)) {
            throw new ResourceNotFoundException("Wallet", "id", walletId);
        }
        
        return transactionRepository.findByWalletIdAndStatus(walletId, status).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public TransactionDto getTransactionById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));
        return mapToDto(transaction);
    }
    
    private void updateWalletBalancesForDeposit(Wallet wallet, BigDecimal amount, Transaction.TransactionStatus status) {
        // Always update the balance regardless of status
        wallet.setBalance(wallet.getBalance().add(amount));
        
        // Only update usable balance if transaction is approved
        if (status == Transaction.TransactionStatus.APPROVED) {
            wallet.setUsableBalance(wallet.getUsableBalance().add(amount));
        }
        
        walletRepository.save(wallet);
    }
    
    private void updateWalletBalancesForWithdraw(Wallet wallet, BigDecimal amount, Transaction.TransactionStatus status) {
        // Always reduce usable balance
        wallet.setUsableBalance(wallet.getUsableBalance().subtract(amount));
        
        // If approved, reduce balance as well
        if (status == Transaction.TransactionStatus.APPROVED) {
            wallet.setBalance(wallet.getBalance().subtract(amount));
        }
        
        walletRepository.save(wallet);
    }
    
    private void handleDepositApproval(Wallet wallet, BigDecimal amount, 
                                     Transaction.TransactionStatus oldStatus, 
                                     Transaction.TransactionStatus newStatus) {
        if (oldStatus == Transaction.TransactionStatus.PENDING) {
            if (newStatus == Transaction.TransactionStatus.APPROVED) {
                // Pending -> Approved: Update usable balance
                wallet.setUsableBalance(wallet.getUsableBalance().add(amount));
            } else if (newStatus == Transaction.TransactionStatus.DENIED) {
                // Pending -> Denied: Revert balance change
                wallet.setBalance(wallet.getBalance().subtract(amount));
            }
        }
        walletRepository.save(wallet);
    }
    
    private void handleWithdrawApproval(Wallet wallet, BigDecimal amount, 
                                      Transaction.TransactionStatus oldStatus, 
                                      Transaction.TransactionStatus newStatus) {
        if (oldStatus == Transaction.TransactionStatus.PENDING) {
            if (newStatus == Transaction.TransactionStatus.APPROVED) {
                // Pending -> Approved: Update balance (usable already reduced)
                wallet.setBalance(wallet.getBalance().subtract(amount));
            } else if (newStatus == Transaction.TransactionStatus.DENIED) {
                // Pending -> Denied: Restore usable balance
                wallet.setUsableBalance(wallet.getUsableBalance().add(amount));
            }
        }
        walletRepository.save(wallet);
    }
    
    private TransactionDto mapToDto(Transaction transaction) {
        return new TransactionDto(
                transaction.getId(),
                transaction.getWallet().getId(),
                transaction.getAmount(),
                transaction.getType(),
                transaction.getOppositePartyType(),
                transaction.getOppositeParty(),
                transaction.getStatus(),
                transaction.getTransactionDate()
        );
    }
} 