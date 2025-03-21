package com.wallet.api.repository;

import com.wallet.api.entity.Wallet;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    
    @EntityGraph(attributePaths = {"customer"})
    List<Wallet> findByCustomerId(Long customerId);
    
    @EntityGraph(attributePaths = {"customer"})
    List<Wallet> findByCustomerIdAndCurrency(Long customerId, Wallet.Currency currency);
    
    @EntityGraph(attributePaths = {"customer"})
    Optional<Wallet> findByIdAndCustomerId(Long id, Long customerId);
} 