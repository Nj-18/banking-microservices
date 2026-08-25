package com.banking.demo.repositories;

import com.banking.demo.entities.User;
import com.banking.demo.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

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
    private UserRepository userRepository;

    @Test
    void testFindByUsername_whenUsernameExists_thenReturnUser() {

        // Arrange

        User user = new User();

        user.setUsername("nikunj");
        user.setPassword("password");
        user.setEmail("nikunj@gmail.com");
        user.setRole(Role.CUSTOMER);
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());

        userRepository.save(user);

        // Act

        Optional<User> result =
                userRepository.findByUsername("nikunj");

        // Assert

        assertThat(result).isPresent();

        assertThat(result.get().getUsername())
                .isEqualTo("nikunj");

        assertThat(result.get().getEmail())
                .isEqualTo("nikunj@gmail.com");
    }

    @Test
    void testFindByUsername_whenUsernameDoesNotExist_thenReturnEmpty() {

        // Act

        Optional<User> result =
                userRepository.findByUsername("notExistingUser");

        // Assert

        assertThat(result).isEmpty();
    }

    @Test
    void testFindByEmail_whenEmailExists_thenReturnUser() {

        // Arrange

        User user = new User();

        user.setUsername("nikunj");
        user.setPassword("password");
        user.setEmail("nikunj@gmail.com");
        user.setRole(Role.CUSTOMER);
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());

        userRepository.save(user);

        // Act

        Optional<User> result =
                userRepository.findByEmail("nikunj@gmail.com");

        // Assert

        assertThat(result).isPresent();

        assertThat(result.get().getEmail())
                .isEqualTo("nikunj@gmail.com");
    }

    @Test
    void testFindByEmail_whenEmailDoesNotExist_thenReturnEmpty() {

        // Act

        Optional<User> result =
                userRepository.findByEmail("notexisting@gmail.com");

        // Assert

        assertThat(result).isEmpty();
    }
}