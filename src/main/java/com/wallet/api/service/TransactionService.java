package com.wallet.api.service;

import com.wallet.api.dto.*;
import com.wallet.api.entity.Transaction;

import java.util.List;

public interface TransactionService {
    
    TransactionDto deposit(DepositRequest request);
    
    TransactionDto withdraw(WithdrawRequest request);
    
    TransactionDto approveTransaction(ApproveTransactionRequest request);
    
    List<TransactionDto> getTransactionsByWalletId(Long walletId);
    
    List<TransactionDto> getTransactionsByWalletIdAndType(Long walletId, Transaction.TransactionType type);
    
    List<TransactionDto> getTransactionsByWalletIdAndStatus(Long walletId, Transaction.TransactionStatus status);
    
    TransactionDto getTransactionById(Long id);
} 