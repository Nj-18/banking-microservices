package com.banking.demo.controllers;

import com.banking.demo.dtos.LoginRequestDTO;
import com.banking.demo.dtos.LoginResponseDTO;
import com.banking.demo.dtos.RegisterUserRequestDTO;
import com.banking.demo.dtos.RegisterUserResponseDTO;
import com.banking.demo.services.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public RegisterUserResponseDTO register(@RequestBody RegisterUserRequestDTO dto)
    {
       return userService.registerUser(dto);
    }

    @PostMapping("/login")
    public LoginResponseDTO login(@RequestBody LoginRequestDTO dto)
    {
        return userService.login(dto);
    }



}
