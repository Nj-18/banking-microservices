package com.banking.demo.serviceImpl;

import com.banking.demo.dtos.LoginRequestDTO;
import com.banking.demo.dtos.LoginResponseDTO;
import com.banking.demo.dtos.RegisterUserRequestDTO;
import com.banking.demo.dtos.RegisterUserResponseDTO;
import com.banking.demo.entities.Customer;
import com.banking.demo.entities.User;
import com.banking.demo.enums.Role;
import com.banking.demo.exceptions.*;
import com.banking.demo.repositories.CustomerRepository;
import com.banking.demo.repositories.UserRepository;
import com.banking.demo.security.CustomUserDetailsService;
import com.banking.demo.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @InjectMocks
    private UserServiceImpl userService;


    // =========================================================
    // registerUser()
    // =========================================================

    @Test
    void registerUser_shouldRegisterSuccessfully() {

        // ARRANGE

        RegisterUserRequestDTO request =
                new RegisterUserRequestDTO();

        request.setUsername("nikunj");
        request.setPassword("Nikunj@123");
        request.setEmail("nikunj@gmail.com");
        request.setFirstName("Nikunj");
        request.setLastName("Jain");
        request.setMobileNumber("9876543211");

        when(userRepository.findByUsername("nikunj"))
                .thenReturn(Optional.empty());

        when(userRepository.findByEmail("nikunj@gmail.com"))
                .thenReturn(Optional.empty());

        Customer savedCustomer =
                new Customer();

        savedCustomer.setId(1L);
        savedCustomer.setFirstName("Nikunj");
        savedCustomer.setLastName("Jain");
        savedCustomer.setEmail("nikunj@gmail.com");

        when(customerRepository.save(any(Customer.class)))
                .thenReturn(savedCustomer);

        when(passwordEncoder.encode("Nikunj@123"))
                .thenReturn("ENCODED_PASSWORD");

        User savedUser =
                new User();

        savedUser.setId(100L);
        savedUser.setUsername("nikunj");
        savedUser.setEmail("nikunj@gmail.com");
        savedUser.setRole(Role.CUSTOMER);
        savedUser.setEnabled(true);
        savedUser.setCustomer(savedCustomer);

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        // ACT

        RegisterUserResponseDTO response =
                userService.registerUser(request);

        // ASSERT

        assertNotNull(response);

        assertEquals(
                100L,
                response.getUserId());

        assertEquals(
                "nikunj",
                response.getUsername());

        assertEquals(
                "nikunj@gmail.com",
                response.getEmail());

        assertEquals(
                "CUSTOMER",
                response.getRole());

        assertEquals(
                "User registered successfully.",
                response.getMessage());

        verify(userRepository)
                .findByUsername("nikunj");

        verify(userRepository)
                .findByEmail("nikunj@gmail.com");

        verify(customerRepository)
                .save(any(Customer.class));

        verify(passwordEncoder)
                .encode("Nikunj@123");

        verify(userRepository)
                .save(any(User.class));
    }


    @Test
    void registerUser_shouldThrowException_whenUsernameAlreadyExists() {

        // ARRANGE

        RegisterUserRequestDTO request =
                new RegisterUserRequestDTO();

        request.setUsername("nikunj");
        request.setEmail("nikunj@gmail.com");

        User existingUser =
                new User();

        existingUser.setUsername("nikunj");

        when(userRepository.findByUsername("nikunj"))
                .thenReturn(Optional.of(existingUser));

        // ACT + ASSERT

        assertThrows(
                UsernameAlreadyExistsException.class,
                () -> userService.registerUser(request)
        );

        verify(userRepository)
                .findByUsername("nikunj");

        verify(userRepository, never())
                .findByEmail(anyString());

        verifyNoInteractions(customerRepository);
        verifyNoInteractions(passwordEncoder);
    }


    @Test
    void registerUser_shouldThrowException_whenEmailAlreadyExists() {

        // ARRANGE

        RegisterUserRequestDTO request =
                new RegisterUserRequestDTO();

        request.setUsername("nikunj");
        request.setEmail("nikunj@gmail.com");

        when(userRepository.findByUsername("nikunj"))
                .thenReturn(Optional.empty());

        User existingUser =
                new User();

        existingUser.setEmail("nikunj@gmail.com");

        when(userRepository.findByEmail("nikunj@gmail.com"))
                .thenReturn(Optional.of(existingUser));

        // ACT + ASSERT

        assertThrows(
                EmailAlreadyExistsException.class,
                () -> userService.registerUser(request)
        );

        verify(userRepository)
                .findByUsername("nikunj");

        verify(userRepository)
                .findByEmail("nikunj@gmail.com");

        verifyNoInteractions(customerRepository);
        verifyNoInteractions(passwordEncoder);
    }


    // =========================================================
    // login()
    // =========================================================

    @Test
    void login_shouldLoginSuccessfully() {

        // ARRANGE

        LoginRequestDTO request =
                new LoginRequestDTO();

        request.setUsername("nikunj");
        request.setPassword("Nikunj@123");

        Customer customer =
                new Customer();

        customer.setId(1L);

        User user =
                new User();

        user.setId(100L);
        user.setUsername("nikunj");
        user.setPassword("ENCODED_PASSWORD");
        user.setEmail("nikunj@gmail.com");
        user.setRole(Role.CUSTOMER);
        user.setEnabled(true);
        user.setCustomer(customer);

        when(userRepository.findByUsername("nikunj"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "Nikunj@123",
                "ENCODED_PASSWORD"))
                .thenReturn(true);

        when(jwtService.generateToken(user))
                .thenReturn("JWT_TOKEN");

        UserDetails userDetails =
                mock(UserDetails.class);

        when(customUserDetailsService
                .loadUserByUsername("nikunj"))
                .thenReturn(userDetails);

        when(jwtService.extractUsername("JWT_TOKEN"))
                .thenReturn("nikunj");

        when(jwtService.extractExpiration("JWT_TOKEN"))
                .thenReturn(null);

        when(jwtService.isTokenValid(
                "JWT_TOKEN",
                userDetails))
                .thenReturn(true);

        // ACT

        LoginResponseDTO response =
                userService.login(request);

        // ASSERT

        assertNotNull(response);

        assertTrue(response.getAuthenticated());

        assertEquals(
                "nikunj",
                response.getUsername());

        assertEquals(
                "CUSTOMER",
                response.getRole());

        assertEquals(
                "JWT_TOKEN",
                response.getToken());

        assertEquals(
                "User logged in successfully.",
                response.getMessage());

        assertEquals(
                1L,
                response.getCustomerId());

        verify(userRepository)
                .findByUsername("nikunj");

        verify(passwordEncoder)
                .matches(
                        "Nikunj@123",
                        "ENCODED_PASSWORD");

        verify(jwtService)
                .generateToken(user);

        verify(customUserDetailsService)
                .loadUserByUsername("nikunj");
    }


    @Test
    void login_shouldThrowException_whenUserNotFound() {

        // ARRANGE

        LoginRequestDTO request =
                new LoginRequestDTO();

        request.setUsername("unknown");
        request.setPassword("password");

        when(userRepository.findByUsername("unknown"))
                .thenReturn(Optional.empty());

        // ACT + ASSERT

        assertThrows(
                UsernameNotFoundException.class,
                () -> userService.login(request)
        );

        verify(userRepository)
                .findByUsername("unknown");

        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(jwtService);
    }


    @Test
    void login_shouldThrowException_whenUserDisabled() {

        // ARRANGE

        LoginRequestDTO request =
                new LoginRequestDTO();

        request.setUsername("nikunj");
        request.setPassword("Nikunj@123");

        User user =
                new User();

        user.setUsername("nikunj");
        user.setPassword("ENCODED_PASSWORD");
        user.setEnabled(false);

        when(userRepository.findByUsername("nikunj"))
                .thenReturn(Optional.of(user));

        // ACT + ASSERT

        assertThrows(
                UserNotEnabledException.class,
                () -> userService.login(request)
        );

        verify(userRepository)
                .findByUsername("nikunj");

        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(jwtService);
    }


    @Test
    void login_shouldThrowException_whenPasswordIsIncorrect() {

        // ARRANGE

        LoginRequestDTO request =
                new LoginRequestDTO();

        request.setUsername("nikunj");
        request.setPassword("WrongPassword");

        User user =
                new User();

        user.setUsername("nikunj");
        user.setPassword("ENCODED_PASSWORD");
        user.setEnabled(true);

        when(userRepository.findByUsername("nikunj"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "WrongPassword",
                "ENCODED_PASSWORD"))
                .thenReturn(false);

        // ACT + ASSERT

        assertThrows(
                InvalidCredentialsException.class,
                () -> userService.login(request)
        );

        verify(passwordEncoder)
                .matches(
                        "WrongPassword",
                        "ENCODED_PASSWORD");

        verifyNoInteractions(jwtService);
        verifyNoInteractions(customUserDetailsService);
    }
}