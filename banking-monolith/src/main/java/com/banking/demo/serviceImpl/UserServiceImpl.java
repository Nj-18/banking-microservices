package com.banking.demo.serviceImpl;

import com.banking.demo.dtos.LoginRequestDTO;
import com.banking.demo.dtos.LoginResponseDTO;
import com.banking.demo.enums.Role;
import com.banking.demo.dtos.RegisterUserRequestDTO;
import com.banking.demo.dtos.RegisterUserResponseDTO;
import com.banking.demo.entities.Customer;
import com.banking.demo.entities.User;
import com.banking.demo.exceptions.*;
import com.banking.demo.repositories.CustomerRepository;
import com.banking.demo.repositories.UserRepository;
import com.banking.demo.security.CustomUserDetailsService;
import com.banking.demo.services.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.banking.demo.security.JwtService;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;



    public UserServiceImpl(UserRepository userRepository,
                           CustomerRepository customerRepository,
                           PasswordEncoder passwordEncoder, JwtService jwtService, CustomUserDetailsService customUserDetailsService) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Override
    public RegisterUserResponseDTO registerUser(RegisterUserRequestDTO request) {

        // Check username
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new UsernameAlreadyExistsException(
                    "Username already exists : " + request.getUsername());
        }

        // Check email
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException(
                    "Email already exists : " + request.getEmail());
        }

        // Find customer
        Customer customer = new Customer();

        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setEmail(request.getEmail());
        customer.setMobileNumber(request.getMobileNumber());
        customer.setCustomerStatus("ACTIVE");

        Customer savedCustomer = customerRepository.save(customer);
        // Create User
        User user = new User();

        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());

        user.setRole(Role.CUSTOMER);
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());

        user.setCustomer(customer);

        // Save User
        User savedUser = userRepository.save(user);

        // Prepare Response
        RegisterUserResponseDTO response = new RegisterUserResponseDTO();

        response.setUserId(savedUser.getId());
        response.setUsername(savedUser.getUsername());
        response.setEmail(savedUser.getEmail());
        response.setRole(savedUser.getRole().name());
        response.setMessage("User registered successfully.");

        return response;
    }

    @Override
    public LoginResponseDTO login(LoginRequestDTO request)
            throws InvalidCredentialsException, UserNotEnabledException {

        // 1. Find User by Username
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));

        // 2. Check if user is enabled
        if (!user.getEnabled()) {
            throw new UserNotEnabledException("User is not enabled.");
        }

        // 3. Verify Password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException(
                    "Invalid username or password.");
        }

        // 4. Generate JWT Token
        String token = jwtService.generateToken(user);

        // Load UserDetails
        UserDetails userDetails =
                customUserDetailsService.loadUserByUsername(user.getUsername());
        // ---------- TEMPORARY TESTING ----------
        System.out.println("====================================");
        System.out.println("Generated Token : " + token);
        System.out.println("Username : " + jwtService.extractUsername(token));
        System.out.println("Expiration : " + jwtService.extractExpiration(token));
        System.out.println("Is Token Valid : " + jwtService.isTokenValid(token,userDetails));
        System.out.println("====================================");
        // ---------------------------------------

        // 5. Prepare Response
        LoginResponseDTO dto = new LoginResponseDTO();

        dto.setAuthenticated(true);
        dto.setUsername(user.getUsername());
        dto.setRole(user.getRole().name());
        dto.setToken(token);
        dto.setMessage("User logged in successfully.");
        dto.setCustomerId(user.getCustomer().getId());
        return dto;
    }
}