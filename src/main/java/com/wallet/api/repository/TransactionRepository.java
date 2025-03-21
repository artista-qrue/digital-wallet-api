package com.wallet.api.repository;

import com.wallet.api.entity.Transaction;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    @EntityGraph(attributePaths = {"wallet"})
    List<Transaction> findByWalletId(Long walletId);
    
    @EntityGraph(attributePaths = {"wallet"})
    List<Transaction> findByWalletIdAndType(Long walletId, Transaction.TransactionType type);
    
    @EntityGraph(attributePaths = {"wallet"})
    List<Transaction> findByWalletIdAndStatus(Long walletId, Transaction.TransactionStatus status);
    
    @EntityGraph(attributePaths = {"wallet", "wallet.customer"})
    Optional<Transaction> findById(Long id);
} 