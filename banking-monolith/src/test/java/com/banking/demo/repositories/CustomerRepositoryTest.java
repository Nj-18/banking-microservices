package com.banking.demo.repositories;

import com.banking.demo.TestContainerConfiguration;
import com.banking.demo.entities.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestContainerConfiguration.class)
@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class CustomerRepositoryTest {

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
    private CustomerRepository customerRepository;

    private Customer customer;

    @BeforeEach
    void setUp() {

        customer = new Customer();

        customer.setFirstName("Nikunj");
        customer.setLastName("Jain");
        customer.setEmail("nikunj@test.com");
        customer.setMobileNumber("9876543211");
        customer.setCustomerStatus("ACTIVE");
    }

    @Test
    void testSaveCustomer_whenCustomerIsValid_thenSaveCustomer() {

        // Arrange
        Customer savedCustomer =
                customerRepository.save(customer);

        // Assert
        assertThat(savedCustomer).isNotNull();
        assertThat(savedCustomer.getId()).isNotNull();

        assertThat(savedCustomer.getFirstName())
                .isEqualTo("Nikunj");

        assertThat(savedCustomer.getLastName())
                .isEqualTo("Jain");

        assertThat(savedCustomer.getEmail())
                .isEqualTo("nikunj@test.com");
    }

    @Test
    void testFindById_whenCustomerExists_thenReturnCustomer() {

        // Arrange
        Customer savedCustomer =
                customerRepository.save(customer);

        // Act
        Optional<Customer> result =
                customerRepository.findById(
                        savedCustomer.getId()
                );

        // Assert
        assertThat(result).isPresent();

        assertThat(result.get().getFirstName())
                .isEqualTo("Nikunj");

        assertThat(result.get().getLastName())
                .isEqualTo("Jain");
    }

    @Test
    void testFindById_whenCustomerDoesNotExist_thenReturnEmpty() {

        // Act
        Optional<Customer> result =
                customerRepository.findById(99999L);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void testFindAll_whenCustomersExist_thenReturnCustomers() {

        // Arrange

        Customer customer2 = new Customer();

        customer2.setFirstName("Rahul");
        customer2.setLastName("Sharma");
        customer2.setEmail("rahul@test.com");
        customer2.setMobileNumber("9876543212");
        customer2.setCustomerStatus("ACTIVE");

        customerRepository.save(customer);
        customerRepository.save(customer2);

        // Act

        List<Customer> customers =
                customerRepository.findAll();

        // Assert

        assertThat(customers).isNotNull();
        assertThat(customers).hasSize(2);

        assertThat(customers)
                .extracting(Customer::getFirstName)
                .contains("Nikunj", "Rahul");
    }

    @Test
    void testFindAll_whenNoCustomersExist_thenReturnEmptyList() {

        // Act

        List<Customer> customers =
                customerRepository.findAll();

        // Assert

        assertThat(customers).isNotNull();
        assertThat(customers).isEmpty();
    }
}