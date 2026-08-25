package com.banking.demo.repositories;

import com.banking.demo.entities.BankAccount;
import com.banking.demo.entities.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BankAccountRepositoryTest {


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
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private Customer customer;
    private BankAccount bankAccount;

    @BeforeEach
    void setUp() {

        customer = new Customer();

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
    }

    @Test
    void testSaveAccount_whenAccountIsValid_thenSaveAccount() {

        // Arrange + Act
        BankAccount savedAccount =
                bankAccountRepository.save(bankAccount);

        // Assert
        assertThat(savedAccount).isNotNull();
        assertThat(savedAccount.getId()).isNotNull();
        assertThat(savedAccount.getAccountNumber())
                .isEqualTo("ACC100001");
        assertThat(savedAccount.getBalance())
                .isEqualTo(10000.0);
    }

    @Test
    void testFindByAccountNumber_whenAccountExists_thenReturnAccount() {

        // Arrange
        bankAccountRepository.save(bankAccount);

        // Act
        Optional<BankAccount> result =
                bankAccountRepository
                        .findByAccountNumber("ACC100001");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getAccountNumber())
                .isEqualTo("ACC100001");
        assertThat(result.get().getBalance())
                .isEqualTo(10000.0);
    }

    @Test
    void testFindByAccountNumber_whenAccountDoesNotExist_thenReturnEmpty() {

        // Act
        Optional<BankAccount> result =
                bankAccountRepository
                        .findByAccountNumber("INVALID");

        // Assert
        assertThat(result).isEmpty();
    }
}