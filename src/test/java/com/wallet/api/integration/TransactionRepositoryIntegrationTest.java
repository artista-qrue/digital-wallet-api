package com.wallet.api.integration;

import com.wallet.api.entity.Customer;
import com.wallet.api.entity.Transaction;
import com.wallet.api.entity.Wallet;
import com.wallet.api.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class TransactionRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TransactionRepository transactionRepository;

    private Customer customer;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        // Create and persist a customer
        customer = new Customer();
        customer.setTckn("12345678901");
        customer.setName("John");
        customer.setSurname("Doe");
        entityManager.persist(customer);

        // Create and persist a wallet
        wallet = new Wallet();
        wallet.setWalletName("Test Wallet");
        wallet.setCurrency(Wallet.Currency.TRY);
        wallet.setActiveForShopping(true);
        wallet.setActiveForWithdraw(true);
        wallet.setBalance(BigDecimal.valueOf(1000));
        wallet.setUsableBalance(BigDecimal.valueOf(1000));
        wallet.setCustomer(customer);
        entityManager.persist(wallet);
        
        entityManager.flush();
    }

    @Test
    void findByWalletId_ShouldReturnTransactions_WhenWalletExists() {
        // Arrange
        Transaction deposit = new Transaction();
        deposit.setAmount(BigDecimal.valueOf(500));
        deposit.setType(Transaction.TransactionType.DEPOSIT);
        deposit.setStatus(Transaction.TransactionStatus.APPROVED);
        deposit.setOppositeParty("Test deposit");
        deposit.setOppositePartyType(Transaction.OppositePartyType.IBAN);
        deposit.setWallet(wallet);
        entityManager.persist(deposit);

        Transaction withdraw = new Transaction();
        withdraw.setAmount(BigDecimal.valueOf(200));
        withdraw.setType(Transaction.TransactionType.WITHDRAW);
        withdraw.setStatus(Transaction.TransactionStatus.APPROVED);
        withdraw.setOppositeParty("Test withdrawal");
        withdraw.setOppositePartyType(Transaction.OppositePartyType.IBAN);
        withdraw.setWallet(wallet);
        entityManager.persist(withdraw);
        
        entityManager.flush();

        // Act
        List<Transaction> transactions = transactionRepository.findByWalletId(wallet.getId());

        // Assert
        assertThat(transactions).hasSize(2);
        assertThat(transactions).extracting(Transaction::getAmount)
                .containsExactlyInAnyOrder(BigDecimal.valueOf(500), BigDecimal.valueOf(200));
        assertThat(transactions).extracting(Transaction::getType)
                .containsExactlyInAnyOrder(Transaction.TransactionType.DEPOSIT, Transaction.TransactionType.WITHDRAW);
    }

    @Test
    void findById_ShouldReturnTransaction_WhenTransactionExists() {
        // Arrange
        Transaction transaction = new Transaction();
        transaction.setAmount(BigDecimal.valueOf(500));
        transaction.setType(Transaction.TransactionType.DEPOSIT);
        transaction.setStatus(Transaction.TransactionStatus.APPROVED);
        transaction.setOppositeParty("Test transaction");
        transaction.setOppositePartyType(Transaction.OppositePartyType.IBAN);
        transaction.setWallet(wallet);
        Transaction savedTransaction = entityManager.persist(transaction);
        entityManager.flush();

        // Act
        Optional<Transaction> found = transactionRepository.findById(savedTransaction.getId());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getAmount()).isEqualByComparingTo(BigDecimal.valueOf(500));
        assertThat(found.get().getType()).isEqualTo(Transaction.TransactionType.DEPOSIT);
        assertThat(found.get().getStatus()).isEqualTo(Transaction.TransactionStatus.APPROVED);
        assertThat(found.get().getOppositeParty()).isEqualTo("Test transaction");
        assertThat(found.get().getWallet().getId()).isEqualTo(wallet.getId());
    }

    @Test
    void findById_ShouldReturnEmpty_WhenTransactionDoesNotExist() {
        // Act
        Optional<Transaction> found = transactionRepository.findById(999L);

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    void findByWalletIdAndStatus_ShouldReturnTransactions_WhenMatchingCriteria() {
        // Arrange
        Transaction smallDeposit = new Transaction();
        smallDeposit.setAmount(BigDecimal.valueOf(100));
        smallDeposit.setType(Transaction.TransactionType.DEPOSIT);
        smallDeposit.setStatus(Transaction.TransactionStatus.APPROVED);
        smallDeposit.setOppositeParty("Small deposit");
        smallDeposit.setOppositePartyType(Transaction.OppositePartyType.IBAN);
        smallDeposit.setWallet(wallet);
        entityManager.persist(smallDeposit);

        Transaction largeDeposit = new Transaction();
        largeDeposit.setAmount(BigDecimal.valueOf(1000));
        largeDeposit.setType(Transaction.TransactionType.DEPOSIT);
        largeDeposit.setStatus(Transaction.TransactionStatus.PENDING);
        largeDeposit.setOppositeParty("Large deposit");
        largeDeposit.setOppositePartyType(Transaction.OppositePartyType.IBAN);
        largeDeposit.setWallet(wallet);
        entityManager.persist(largeDeposit);

        Transaction largeWithdrawal = new Transaction();
        largeWithdrawal.setAmount(BigDecimal.valueOf(800));
        largeWithdrawal.setType(Transaction.TransactionType.WITHDRAW);
        largeWithdrawal.setStatus(Transaction.TransactionStatus.PENDING);
        largeWithdrawal.setOppositeParty("Large withdrawal");
        largeWithdrawal.setOppositePartyType(Transaction.OppositePartyType.IBAN);
        largeWithdrawal.setWallet(wallet);
        entityManager.persist(largeWithdrawal);
        
        entityManager.flush();

        // Act
        List<Transaction> pendingTransactions = transactionRepository
                .findByWalletIdAndStatus(wallet.getId(), Transaction.TransactionStatus.PENDING);

        // Assert
        assertThat(pendingTransactions).hasSize(2);
        assertThat(pendingTransactions).extracting(Transaction::getOppositeParty)
                .containsExactlyInAnyOrder("Large deposit", "Large withdrawal");
    }
} 