package com.banking.demo.repositories;

import com.banking.demo.entities.BankAccount;
import com.banking.demo.entities.Customer;
import com.banking.demo.entities.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TransactionRepositoryTest {

    static MySQLContainer<?> mysql =
            new MySQLContainer<>("mysql:8.0")
                    .withDatabaseName("banking_test")
                    .withUsername("test")
                    .withPassword("test");

    static {
        mysql.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {

        registry.add(
                "spring.datasource.url",
                mysql::getJdbcUrl
        );

        registry.add(
                "spring.datasource.username",
                mysql::getUsername
        );

        registry.add(
                "spring.datasource.password",
                mysql::getPassword
        );
    }

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private BankAccount bankAccount;

    @BeforeEach
    void setUp() {

        Customer customer = new Customer();

        customer.setFirstName("Nikunj");
        customer.setLastName("Jain");
        customer.setEmail("nikunj@gmail.com");
        customer.setMobileNumber("9876543211");
        customer.setCustomerStatus("ACTIVE");

        customer = customerRepository.save(customer);

        bankAccount = new BankAccount();

        bankAccount.setAccountNumber("ACC100001");
        bankAccount.setAccountType("SAVINGS");
        bankAccount.setBalance(10000.0);
        bankAccount.setAccountStatus("ACTIVE");
        bankAccount.setCustomer(customer);

        bankAccount =
                bankAccountRepository.save(bankAccount);
    }

    @Test
    void testFindByBankAccount_whenTransactionsExist_thenReturnTransactions() {

        // Arrange

        Transaction transaction = new Transaction();

        transaction.setTransactionReference("TXN001");
        transaction.setTransactionType("DEPOSIT");
        transaction.setAmount(5000.0);
        transaction.setStatus("SUCCESS");
        transaction.setRemarks("Cash deposit");
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setBankAccount(bankAccount);

        transactionRepository.save(transaction);

        // Act

        List<Transaction> result =
                transactionRepository
                        .findByBankAccount(bankAccount);

        // Assert

        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);

        assertThat(result.get(0).getTransactionReference())
                .isEqualTo("TXN001");

        assertThat(result.get(0).getAmount())
                .isEqualTo(5000.0);
    }

    @Test
    void testFindByBankAccount_whenNoTransactions_thenReturnEmptyList() {

        // Act

        List<Transaction> result =
                transactionRepository
                        .findByBankAccount(bankAccount);

        // Assert

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void testFindByBankAccountAndTransactionDateBetween_whenTransactionExists_thenReturnTransaction() {

        // Arrange

        LocalDateTime transactionDate =
                LocalDateTime.of(2026, 8, 11, 10, 0);

        Transaction transaction = new Transaction();

        transaction.setTransactionReference("TXN002");
        transaction.setTransactionType("WITHDRAW");
        transaction.setAmount(1000.0);
        transaction.setStatus("SUCCESS");
        transaction.setRemarks("ATM withdrawal");
        transaction.setTransactionDate(transactionDate);
        transaction.setBankAccount(bankAccount);

        transactionRepository.save(transaction);

        Pageable pageable = PageRequest.of(
                0,
                10,
                Sort.by("transactionDate").descending()
        );
        // Act
        Page<Transaction> result =
                transactionRepository
                        .findByBankAccountAndTransactionDateBetween(
                                bankAccount,
                                LocalDateTime.of(2026, 8, 1, 0, 0),
                                LocalDateTime.of(2026, 8, 31, 23, 59, 59),
                                pageable
                        );

        // Assert

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);

        assertThat(result.getContent().get(0).getTransactionReference())
                .isEqualTo("TXN002");
    }
}