package com.banking.demo.dtos;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterUserResponseDTO {

    private Long userId;
    private String username;
    private String email;
    private String role;
    private String message;

}