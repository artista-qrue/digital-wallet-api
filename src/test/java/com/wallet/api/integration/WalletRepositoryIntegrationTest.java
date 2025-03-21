package com.wallet.api.integration;

import com.wallet.api.entity.Customer;
import com.wallet.api.entity.Wallet;
import com.wallet.api.repository.CustomerRepository;
import com.wallet.api.repository.WalletRepository;
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
public class WalletRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private Customer customer;

    @BeforeEach
    void setUp() {
        // Create and persist a customer for wallet tests
        customer = new Customer();
        customer.setTckn("12345678901");
        customer.setName("John");
        customer.setSurname("Doe");
        customer.setEmployee(false);
        entityManager.persist(customer);
        entityManager.flush();
    }

    @Test
    void findByCustomerId_ShouldReturnWallets_WhenCustomerExists() {
        // Arrange
        Wallet wallet1 = new Wallet();
        wallet1.setWalletName("TRY Wallet");
        wallet1.setCurrency(Wallet.Currency.TRY);
        wallet1.setActiveForShopping(true);
        wallet1.setActiveForWithdraw(true);
        wallet1.setBalance(BigDecimal.ZERO);
        wallet1.setUsableBalance(BigDecimal.ZERO);
        wallet1.setCustomer(customer);
        entityManager.persist(wallet1);

        Wallet wallet2 = new Wallet();
        wallet2.setWalletName("USD Wallet");
        wallet2.setCurrency(Wallet.Currency.USD);
        wallet2.setActiveForShopping(true);
        wallet2.setActiveForWithdraw(false);
        wallet2.setBalance(BigDecimal.valueOf(100));
        wallet2.setUsableBalance(BigDecimal.valueOf(100));
        wallet2.setCustomer(customer);
        entityManager.persist(wallet2);
        
        entityManager.flush();

        // Act
        List<Wallet> wallets = walletRepository.findByCustomerId(customer.getId());

        // Assert
        assertThat(wallets).hasSize(2);
        assertThat(wallets).extracting(Wallet::getWalletName)
                .containsExactlyInAnyOrder("TRY Wallet", "USD Wallet");
    }

    @Test
    void findByCustomerIdAndCurrency_ShouldReturnWallets_WhenCustomerAndCurrencyMatch() {
        // Arrange
        Wallet wallet1 = new Wallet();
        wallet1.setWalletName("TRY Wallet");
        wallet1.setCurrency(Wallet.Currency.TRY);
        wallet1.setActiveForShopping(true);
        wallet1.setActiveForWithdraw(true);
        wallet1.setBalance(BigDecimal.ZERO);
        wallet1.setUsableBalance(BigDecimal.ZERO);
        wallet1.setCustomer(customer);
        entityManager.persist(wallet1);

        Wallet wallet2 = new Wallet();
        wallet2.setWalletName("USD Wallet");
        wallet2.setCurrency(Wallet.Currency.USD);
        wallet2.setActiveForShopping(true);
        wallet2.setActiveForWithdraw(false);
        wallet2.setBalance(BigDecimal.valueOf(100));
        wallet2.setUsableBalance(BigDecimal.valueOf(100));
        wallet2.setCustomer(customer);
        entityManager.persist(wallet2);
        
        entityManager.flush();

        // Act
        List<Wallet> wallets = walletRepository.findByCustomerIdAndCurrency(customer.getId(), Wallet.Currency.USD);

        // Assert
        assertThat(wallets).hasSize(1);
        assertThat(wallets.get(0).getWalletName()).isEqualTo("USD Wallet");
        assertThat(wallets.get(0).getCurrency()).isEqualTo(Wallet.Currency.USD);
    }

    @Test
    void findById_ShouldReturnWallet_WhenWalletExists() {
        // Arrange
        Wallet wallet = new Wallet();
        wallet.setWalletName("TRY Wallet");
        wallet.setCurrency(Wallet.Currency.TRY);
        wallet.setActiveForShopping(true);
        wallet.setActiveForWithdraw(true);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setUsableBalance(BigDecimal.ZERO);
        wallet.setCustomer(customer);
        Wallet savedWallet = entityManager.persist(wallet);
        entityManager.flush();

        // Act
        Optional<Wallet> found = walletRepository.findById(savedWallet.getId());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getWalletName()).isEqualTo("TRY Wallet");
        assertThat(found.get().getCurrency()).isEqualTo(Wallet.Currency.TRY);
        assertThat(found.get().getCustomer().getId()).isEqualTo(customer.getId());
    }

    @Test
    void findByIdAndCustomerId_ShouldReturnWallet_WhenWalletBelongsToCustomer() {
        // Arrange
        Wallet wallet = new Wallet();
        wallet.setWalletName("TRY Wallet");
        wallet.setCurrency(Wallet.Currency.TRY);
        wallet.setActiveForShopping(true);
        wallet.setActiveForWithdraw(true);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setUsableBalance(BigDecimal.ZERO);
        wallet.setCustomer(customer);
        Wallet savedWallet = entityManager.persist(wallet);
        entityManager.flush();

        // Act
        Optional<Wallet> found = walletRepository.findByIdAndCustomerId(savedWallet.getId(), customer.getId());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getWalletName()).isEqualTo("TRY Wallet");
    }

    @Test
    void findByIdAndCustomerId_ShouldReturnEmpty_WhenWalletDoesNotBelongToCustomer() {
        // Arrange
        Wallet wallet = new Wallet();
        wallet.setWalletName("TRY Wallet");
        wallet.setCurrency(Wallet.Currency.TRY);
        wallet.setActiveForShopping(true);
        wallet.setActiveForWithdraw(true);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setUsableBalance(BigDecimal.ZERO);
        wallet.setCustomer(customer);
        Wallet savedWallet = entityManager.persist(wallet);
        entityManager.flush();

        // Create another customer
        Customer otherCustomer = new Customer();
        otherCustomer.setTckn("98765432109");
        otherCustomer.setName("Jane");
        otherCustomer.setSurname("Smith");
        otherCustomer.setEmployee(false);
        entityManager.persist(otherCustomer);
        entityManager.flush();

        // Act
        Optional<Wallet> found = walletRepository.findByIdAndCustomerId(savedWallet.getId(), otherCustomer.getId());

        // Assert
        assertThat(found).isEmpty();
    }
} 