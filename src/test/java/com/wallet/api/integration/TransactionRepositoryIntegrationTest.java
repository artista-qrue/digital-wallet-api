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
        deposit.setType(Transaction.Type.DEPOSIT);
        deposit.setStatus(Transaction.Status.COMPLETED);
        deposit.setDescription("Test deposit");
        deposit.setWallet(wallet);
        entityManager.persist(deposit);

        Transaction withdraw = new Transaction();
        withdraw.setAmount(BigDecimal.valueOf(200));
        withdraw.setType(Transaction.Type.WITHDRAW);
        withdraw.setStatus(Transaction.Status.COMPLETED);
        withdraw.setDescription("Test withdrawal");
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
                .containsExactlyInAnyOrder(Transaction.Type.DEPOSIT, Transaction.Type.WITHDRAW);
    }

    @Test
    void findById_ShouldReturnTransaction_WhenTransactionExists() {
        // Arrange
        Transaction transaction = new Transaction();
        transaction.setAmount(BigDecimal.valueOf(500));
        transaction.setType(Transaction.Type.DEPOSIT);
        transaction.setStatus(Transaction.Status.COMPLETED);
        transaction.setDescription("Test transaction");
        transaction.setWallet(wallet);
        Transaction savedTransaction = entityManager.persist(transaction);
        entityManager.flush();

        // Act
        Optional<Transaction> found = transactionRepository.findById(savedTransaction.getId());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getAmount()).isEqualByComparingTo(BigDecimal.valueOf(500));
        assertThat(found.get().getType()).isEqualTo(Transaction.Type.DEPOSIT);
        assertThat(found.get().getStatus()).isEqualTo(Transaction.Status.COMPLETED);
        assertThat(found.get().getDescription()).isEqualTo("Test transaction");
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
    void findByStatusAndAmountGreaterThan_ShouldReturnTransactions_WhenMatchingCriteria() {
        // Arrange
        Transaction smallDeposit = new Transaction();
        smallDeposit.setAmount(BigDecimal.valueOf(100));
        smallDeposit.setType(Transaction.Type.DEPOSIT);
        smallDeposit.setStatus(Transaction.Status.COMPLETED);
        smallDeposit.setDescription("Small deposit");
        smallDeposit.setWallet(wallet);
        entityManager.persist(smallDeposit);

        Transaction largeDeposit = new Transaction();
        largeDeposit.setAmount(BigDecimal.valueOf(1000));
        largeDeposit.setType(Transaction.Type.DEPOSIT);
        largeDeposit.setStatus(Transaction.Status.PENDING);
        largeDeposit.setDescription("Large deposit");
        largeDeposit.setWallet(wallet);
        entityManager.persist(largeDeposit);

        Transaction largeWithdrawal = new Transaction();
        largeWithdrawal.setAmount(BigDecimal.valueOf(800));
        largeWithdrawal.setType(Transaction.Type.WITHDRAW);
        largeWithdrawal.setStatus(Transaction.Status.PENDING);
        largeWithdrawal.setDescription("Large withdrawal");
        largeWithdrawal.setWallet(wallet);
        entityManager.persist(largeWithdrawal);
        
        entityManager.flush();

        // Act
        List<Transaction> pendingLargeTransactions = transactionRepository
                .findByStatusAndAmountGreaterThan(Transaction.Status.PENDING, BigDecimal.valueOf(500));

        // Assert
        assertThat(pendingLargeTransactions).hasSize(2);
        assertThat(pendingLargeTransactions).extracting(Transaction::getDescription)
                .containsExactlyInAnyOrder("Large deposit", "Large withdrawal");
    }
} 