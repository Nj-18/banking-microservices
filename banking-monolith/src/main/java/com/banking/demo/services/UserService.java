package com.banking.demo.services;

import com.banking.demo.dtos.LoginRequestDTO;
import com.banking.demo.dtos.LoginResponseDTO;
import com.banking.demo.dtos.RegisterUserRequestDTO;
import com.banking.demo.dtos.RegisterUserResponseDTO;
import com.banking.demo.exceptions.InvalidCredentialsException;

public interface UserService {
    RegisterUserResponseDTO registerUser(RegisterUserRequestDTO request);
    LoginResponseDTO login(LoginRequestDTO request) throws InvalidCredentialsException;
}
